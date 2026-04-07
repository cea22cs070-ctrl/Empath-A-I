import torch
from transformers import AutoTokenizer, AutoModelForCausalLM, BitsAndBytesConfig

# =========================
# CONFIG
# =========================
BASE_MODEL = "microsoft/phi-2"
LORA_MODEL_PATH = "lora_final_output"   # <-- your trained LoRA folder
MAX_CONTEXT_TURNS = 6
MAX_NEW_TOKENS = 80

# =========================
# SYSTEM PROMPT
# =========================
SYSTEM_PROMPT = (
    "You are a friendly psychologist.\n"
    "You listen carefully before responding.\n"
    "Respond in ONE short sentence only.\n"
    "Do not change the topic unless the user does.\n"
    "Do not say goodbye unless the user says goodbye first.\n"
    "Ask gentle follow-up questions.\n"
)

# =========================
# LOAD MODEL
# =========================
print("Loading model...")

bnb_config = BitsAndBytesConfig(
    load_in_4bit=True,
    bnb_4bit_quant_type="nf4",
    bnb_4bit_compute_dtype=torch.float16
)

tokenizer = AutoTokenizer.from_pretrained(BASE_MODEL)
tokenizer.pad_token = tokenizer.eos_token

model = AutoModelForCausalLM.from_pretrained(
    BASE_MODEL,
    quantization_config=bnb_config,
    device_map="auto"
)

# Load LoRA
from peft import PeftModel
model = PeftModel.from_pretrained(model, LORA_MODEL_PATH)

model.eval()

print("\nChat started. Type 'bye' to exit.\n")

# =========================
# MEMORY BUFFER
# =========================
conversation = [
    {"role": "system", "content": SYSTEM_PROMPT}
]

# Assistant starts first
print("Assistant: Hi, I’m here — what’s been on your mind lately?")

while True:
    user_input = input("\nYou: ").strip()

    if user_input.lower() == "bye":
        print("Assistant: Take care, I’m here whenever you want to talk.")
        break

    conversation.append({"role": "user", "content": user_input})

    # Keep last N turns
    if len(conversation) > MAX_CONTEXT_TURNS * 2:
        conversation = [conversation[0]] + conversation[-MAX_CONTEXT_TURNS*2:]

    # Build prompt
    prompt = ""
    for msg in conversation:
        if msg["role"] == "system":
            prompt += msg["content"] + "\n\n"
        elif msg["role"] == "user":
            prompt += f"User: {msg['content']}\n"
        else:
            prompt += f"Assistant: {msg['content']}\n"

    prompt += "Assistant:"

    inputs = tokenizer(prompt, return_tensors="pt").to(model.device)

    with torch.no_grad():
        output = model.generate(
            **inputs,
            max_new_tokens=MAX_NEW_TOKENS,
            do_sample=True,
            temperature=0.6,
            top_p=0.9,
            repetition_penalty=1.2,
            pad_token_id=tokenizer.eos_token_id
        )

    response = tokenizer.decode(
        output[0][inputs["input_ids"].shape[-1]:],
        skip_special_tokens=True
    ).strip()

    # Cut to first sentence only
    response = response.split(".")[0].strip() + "."

    print(f"\nAssistant: {response}")

    conversation.append({"role": "assistant", "content": response})