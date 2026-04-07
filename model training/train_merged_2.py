import torch
from transformers import AutoTokenizer, AutoModelForCausalLM

# =========================
# PATHS
# =========================
BASE_MODEL = "microsoft/phi-2"
LORA_PATH = "E:/project/model training/lora_final_output"

# =========================
# LOAD MODEL
# =========================
print("Loading model...")

tokenizer = AutoTokenizer.from_pretrained(BASE_MODEL)
model = AutoModelForCausalLM.from_pretrained(
    BASE_MODEL,
    torch_dtype=torch.float16 if torch.cuda.is_available() else torch.float32,
    device_map="auto"
)

model.load_adapter(LORA_PATH)
model.eval()

print("Model loaded successfully.\n")

# =========================
# STATE
# =========================
history = []
topic = None

# =========================
# ASSISTANT START
# =========================
print("Assistant: Hi, I’m here with you — what’s been weighing on you lately?\n")

# =========================
# CHAT LOOP
# =========================
while True:
    user_input = input("You: ").strip()

    if user_input.lower() in ["bye", "exit", "quit"]:
        print("\nAssistant: Take care — I’m glad you shared this with me.")
        break

    if topic is None and len(user_input) > 6:
        topic = user_input

    history.append(f"User: {user_input}")

    # Keep last turn only (prevents drift)
    context = "\n".join(history[-1:])

    # =========================
    # CLEAN PROMPT (NO INSTRUCTIONS INSIDE OUTPUT ZONE)
    # =========================
    prompt = (
        f"You are a calm, friendly listener.\n"
        f"The conversation topic is: {topic}\n\n"
        f"{context}\n"
        f"Assistant:"
    )

    inputs = tokenizer(prompt, return_tensors="pt").to(model.device)

    with torch.no_grad():
        output = model.generate(
            **inputs,
            max_new_tokens=35,
            temperature=0.5,
            top_p=0.9,
            repetition_penalty=1.5,
            do_sample=True,
            eos_token_id=tokenizer.eos_token_id,
            pad_token_id=tokenizer.eos_token_id
        )

    decoded = tokenizer.decode(output[0], skip_special_tokens=True)

    # =========================
    # HARD CLEAN RESPONSE
    # =========================
    reply = decoded.split("Assistant:")[-1].strip()
    reply = reply.split("User:")[0].strip()
    reply = reply.split(".")[0].strip() + "."

    print(f"\nAssistant: {reply}\n")

    history.append(f"Assistant: {reply}")