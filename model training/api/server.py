import torch
import sqlite3
from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForCausalLM, pipeline
from peft import PeftModel
from collections import defaultdict, deque
from datetime import date
import re

# =====================
# APP INIT
# =====================

app = FastAPI(title="Empath AI Therapist API")

# =====================
# DATABASE
# =====================

SESSION_DB = "sessions.db"

def init_session_db():
    conn = sqlite3.connect(SESSION_DB)
    cursor = conn.cursor()

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS sessions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id TEXT,
            date TEXT,
            summary TEXT,
            dominant_mood TEXT,
            total_turns INTEGER
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS user_profile (
            user_id TEXT PRIMARY KEY,
            name TEXT,
            age INTEGER,
            total_messages INTEGER DEFAULT 0,
            created_at TEXT
        )
    """)

    # UPDATED TABLE
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS user_analysis (
            user_id TEXT PRIMARY KEY,
            analysis_words TEXT,
            support_level TEXT,
            last_updated TEXT
        )
    """)

    conn.commit()
    conn.close()

init_session_db()

# =====================
# MODEL CONFIG
# =====================

BASE_MODEL = "microsoft/phi-2"
LORA_PATH = "../lora_final_output"

tokenizer = AutoTokenizer.from_pretrained(BASE_MODEL)
tokenizer.pad_token = tokenizer.eos_token

model = AutoModelForCausalLM.from_pretrained(
    BASE_MODEL,
    torch_dtype=torch.float16,
    device_map="auto"
)

model = PeftModel.from_pretrained(model, LORA_PATH)
model.eval()

emotion_classifier = pipeline(
    "text-classification",
    model="j-hartmann/emotion-english-distilroberta-base",
    top_k=1,
    device=-1
)

# =====================
# MEMORY
# =====================

user_memory = defaultdict(lambda: deque(maxlen=12))
user_mood = defaultdict(list)
daily_sessions = defaultdict(list)

# =====================
# REQUEST MODEL
# =====================

class ChatRequest(BaseModel):
    user_id: str
    message: str

# =====================
# HELPERS
# =====================

def clean_text(text: str) -> str:
    return re.sub(r"\s+", " ", text).strip()

def detect_emotion(text: str) -> str:
    try:
        return emotion_classifier(text)[0]["label"].lower()
    except:
        return "neutral"

def extract_reply(text: str) -> str:
    sentences = re.split(r'(?<=[.!?])\s+', text.strip())
    reply = " ".join(sentences[:2]).strip()
    return reply if reply else "I'm here with you."

def clean_summary_for_analysis(raw: str) -> str:
    text = raw.replace("Assistant:", "")
    text = text.replace("Advice:", "")
    text = text.replace("\n", " ").strip()
    return " ".join(text.split())

# =====================
# CHAT ENDPOINT
# =====================

@app.post("/chat")
def chat(req: ChatRequest):

    user_id = req.user_id
    message = clean_text(req.message)
    today = date.today().isoformat()

    emotion = detect_emotion(message)
    user_mood[user_id].append(emotion)

    user_memory[user_id].append(f"User: {message}")

    daily_sessions[(user_id, today)].append({
        "user": message,
        "assistant": None
    })

    prompt = (
        "You are a calm emotional support companion.\n"
        "Do not assume events are positive or negative.\n"
        "If emotion is unclear, ask a short clarification.\n"
        "Respond only based on what user explicitly states.\n\n"
    )

    for turn in user_memory[user_id]:
        prompt += turn + "\n"

    prompt += "Assistant:"

    inputs = tokenizer(
        prompt,
        return_tensors="pt",
        truncation=True,
        max_length=1600
    ).to(model.device)

    with torch.no_grad():
        output = model.generate(
            **inputs,
            max_new_tokens=60,
            temperature=0.4,
            top_p=0.9,
            repetition_penalty=1.15,
            do_sample=True,
            pad_token_id=tokenizer.eos_token_id
        )

    decoded = tokenizer.decode(output[0], skip_special_tokens=True)
    reply = extract_reply(decoded.split("Assistant:")[-1])

    user_memory[user_id].append(f"Assistant: {reply}")
    daily_sessions[(user_id, today)][-1]["assistant"] = reply

    return {
        "reply": reply,
        "emotion_detected": emotion,
        "mood_history": user_mood[user_id][-10:],
        "user_id": user_id
    }

# =====================
# END SESSION
# =====================

@app.post("/end-session/{user_id}")
def end_session(user_id: str):

    today = date.today().isoformat()
    session = daily_sessions.get((user_id, today), [])

    if not session:
        return {"message": "No active session"}

    user_text = " ".join([turn["user"] for turn in session if turn["user"]])
    user_text = re.sub(r"\s+", " ", user_text).strip()

    # SUMMARY 
    summary_prompt = f"""
Rewrite these user statements into one clear paragraph describing their emotional concerns.
Use second person.
Do not give advice.
User statements:
{user_text}
Summary:
"""

    inputs = tokenizer(summary_prompt, return_tensors="pt",
                       truncation=True, max_length=900).to(model.device)

    with torch.no_grad():
        output = model.generate(
            **inputs,
            max_new_tokens=90,
            do_sample=False,
            temperature=0.2,
            repetition_penalty=1.1,
            pad_token_id=tokenizer.eos_token_id
        )

    decoded = tokenizer.decode(output[0], skip_special_tokens=True)
    summary = decoded.replace(summary_prompt, "").strip().split("\n")[0]

    moods = user_mood[user_id]
    dominant = max(set(moods), key=moods.count) if moods else "neutral"

    conn = sqlite3.connect(SESSION_DB)
    cursor = conn.cursor()

    cursor.execute("""
        INSERT INTO sessions (user_id, date, summary, dominant_mood, total_turns)
        VALUES (?, ?, ?, ?, ?)
    """, (user_id, today, summary, dominant, len(session)))

   
    # ANALYSIS SYSTEM
    

    analysis_summary = clean_summary_for_analysis(summary).lower()

    allowed_words = [
        "stress", "nervous", "confusion", "tired",
        "anxiety", "burnout", "loneliness", "fear",
        "suicidal", "hopeless", "self_harm", "psychotic"
    ]

    emotion_prompt = f"""
Choose ONE word from this list that best describes the emotional state.

stress, nervous, confusion, tired,
anxiety, burnout, loneliness, fear,
suicidal, hopeless, self_harm, psychotic

Return ONLY the word.

Summary:
{analysis_summary}

Emotion:
"""

    inputs = tokenizer(
        emotion_prompt,
        return_tensors="pt",
        truncation=True,
        max_length=600
    ).to(model.device)

    with torch.no_grad():
        output = model.generate(
            **inputs,
            max_new_tokens=5,
            do_sample=False,
            temperature=0.1,
            pad_token_id=tokenizer.eos_token_id
        )

    decoded = tokenizer.decode(output[0], skip_special_tokens=True)
    model_text = decoded.replace(emotion_prompt, "").lower()

    # extract allowed emotion safely
    emotion_word = None
    for word in allowed_words:
        if word in model_text:
            emotion_word = word
            break

    # fallback if model returns garbage like "a"
    if emotion_word is None:
        emotion_word = "stress"

    analysis_words = emotion_word

    # =====================
    # CATEGORY MAPPING
    # =====================

    grade1_words = ["stress", "nervous", "confusion", "tired"]
    grade2_words = ["anxiety", "burnout", "loneliness", "fear"]
    grade3_words = ["suicidal", "hopeless", "self_harm", "psychotic"]

    if emotion_word in grade3_words:
        new_support = "psychologist_required"
    elif emotion_word in grade2_words:
        new_support = "therapist_required"
    else:
        new_support = "self_support"

    # =====================
    # PREVENT DOWNGRADE
    # =====================

    severity_rank = {
        "self_support": 1,
        "therapist_required": 2,
        "psychologist_required": 3
    }

    cursor.execute(
        "SELECT support_level, analysis_words FROM user_analysis WHERE user_id=?",
        (user_id,)
    )

    existing = cursor.fetchone()

    if existing:

        previous_support = existing[0]
        previous_words = existing[1] if existing[1] else ""

        if severity_rank[new_support] < severity_rank.get(previous_support, 1):
            final_support = previous_support
        else:
            final_support = new_support

        combined_words = set(previous_words.split(",")) if previous_words else set()
        combined_words.add(analysis_words)

        final_words = ", ".join([w.strip() for w in combined_words if w.strip()])

        cursor.execute(
            "UPDATE user_analysis SET analysis_words=?, support_level=?, last_updated=? WHERE user_id=?",
            (final_words, final_support, today, user_id)
        )

    else:

        cursor.execute(
            "INSERT INTO user_analysis(user_id, analysis_words, support_level, last_updated) VALUES (?, ?, ?, ?)",
            (user_id, analysis_words, new_support, today)
        )

    conn.commit()
    conn.close()

    daily_sessions.pop((user_id, today), None)
    user_memory.pop(user_id, None)
    user_mood.pop(user_id, None)

    return {"message": "Session stored successfully"}

# =====================
# GET SESSIONS
# =====================

@app.get("/sessions/{user_id}")
def get_sessions(user_id:str):

    conn=sqlite3.connect(SESSION_DB)
    cursor=conn.cursor()

    cursor.execute("""
    SELECT date,summary,dominant_mood,total_turns
    FROM sessions
    WHERE user_id=?
    ORDER BY id DESC
    """,(user_id,))

    rows=cursor.fetchall()
    conn.close()

    return[
        {
            "date":r[0],
            "summary":r[1],
            "dominant_mood":r[2],
            "total_turns":r[3]
        }
        for r in rows
    ]

# =====================
# GET ANALYSIS
# =====================

@app.get("/user-analysis/{user_id}")
def get_analysis(user_id:str):

    conn=sqlite3.connect(SESSION_DB)
    cursor=conn.cursor()

    cursor.execute("""
    SELECT user_id,analysis_words,support_level,last_updated
    FROM user_analysis
    WHERE user_id=?
    """,(user_id,))

    row=cursor.fetchone()
    conn.close()

    if not row:
        return {
            "user_id":user_id,
            "analysis_words":"",
            "support_level":"",
            "last_updated":""
        }

    return{
        "user_id":row[0],
        "analysis_words":row[1],
        "support_level":row[2],
        "last_updated":row[3]
    }