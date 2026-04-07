import torch
from datasets import load_dataset
from transformers import (
    AutoTokenizer,
    AutoModelForCausalLM,
    TrainingArguments,
    Trainer,
    DataCollatorForLanguageModeling,
    BitsAndBytesConfig
)
from peft import LoraConfig, get_peft_model

# =========================
# CONFIG
# =========================
MODEL_NAME = "microsoft/phi-2"
DATASET_PATH = "v1.csv"               # ✅ UPDATED
OUTPUT_DIR = "phi2_lora_v1_output"    # ✅ UPDATED

BATCH_SIZE = 2
GRAD_ACCUM = 4
EPOCHS = 1
LEARNING_RATE = 2e-4
MAX_LENGTH = 512

# =========================
# TOKENIZER
# =========================
print("Loading tokenizer...")
tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
tokenizer.pad_token = tokenizer.eos_token

# =========================
# MODEL (4-BIT)
# =========================
print("Loading model in 4-bit...")
bnb_config = BitsAndBytesConfig(
    load_in_4bit=True,
    bnb_4bit_quant_type="nf4",
    bnb_4bit_compute_dtype=torch.float16
)

model = AutoModelForCausalLM.from_pretrained(
    MODEL_NAME,
    quantization_config=bnb_config,
    device_map="auto"
)

model.config.use_cache = False

# =========================
# LoRA CONFIG
# =========================
lora_config = LoraConfig(
    r=16,
    lora_alpha=32,
    target_modules=["q_proj", "v_proj"],
    lora_dropout=0.05,
    bias="none",
    task_type="CAUSAL_LM"
)

model = get_peft_model(model, lora_config)
model.print_trainable_parameters()

# =========================
# DATASET
# =========================
print("Loading dataset...")
dataset = load_dataset("csv", data_files=DATASET_PATH)["train"]

def format_example(example):
    emotion = example.get("emotion", "neutral")
    return {
        "text": f"[Emotion: {emotion}]\nUser: {example['user_text']}\nAssistant: {example['bot_text']}"
    }

# Step 1: format
dataset = dataset.map(format_example, remove_columns=dataset.column_names)

# Step 2: tokenize
def tokenize(batch):
    return tokenizer(
        batch["text"],
        truncation=True,
        padding="max_length",
        max_length=MAX_LENGTH
    )

dataset = dataset.map(
    tokenize,
    batched=True,
    remove_columns=["text"]
)

print("Dataset tokenized!")

# =========================
# TRAINING ARGS
# =========================
training_args = TrainingArguments(
    output_dir=OUTPUT_DIR,
    per_device_train_batch_size=BATCH_SIZE,
    gradient_accumulation_steps=GRAD_ACCUM,
    learning_rate=LEARNING_RATE,
    num_train_epochs=EPOCHS,
    logging_steps=50,
    save_steps=1000,
    save_total_limit=3,
    fp16=True,
    report_to="none",
)

# =========================
# TRAINER
# =========================
trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=dataset,
    data_collator=DataCollatorForLanguageModeling(
        tokenizer=tokenizer,
        mlm=False
    ),
)

# =========================
# TRAIN
# =========================
print("Starting training...")
trainer.train()

print("Training complete! Saving model...")
trainer.save_model(OUTPUT_DIR)
tokenizer.save_pretrained(OUTPUT_DIR)

print("All done ✅")