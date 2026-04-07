from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, EmailStr
import sqlite3
import uuid
import hashlib

# APP INIT
app = FastAPI(title="Empath AI Auth Server")

DB_NAME = "users.db"

# DATABASE INIT

def init_db():
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS users (
            id TEXT PRIMARY KEY,
            name TEXT,
            email TEXT UNIQUE,
            password TEXT,
            age INTEGER,
            gender TEXT,
            place TEXT,
            security_question TEXT,
            security_answer TEXT
        )
    """)
    conn.commit()
    conn.close()

init_db()


# HELPERS

def hash_password(password: str):
    return hashlib.sha256(password.encode()).hexdigest()


# REQUEST MODELS

class RegisterRequest(BaseModel):
    name: str
    email: EmailStr
    password: str
    age: int
    gender: str
    place: str
    security_question: str
    security_answer: str

class LoginRequest(BaseModel):
    email: EmailStr
    password: str


# REGISTER

@app.post("/register")
def register(req: RegisterRequest):
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()

    try:
        cursor.execute("""
            INSERT INTO users VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            str(uuid.uuid4()),
            req.name,
            req.email,
            hash_password(req.password),
            req.age,
            req.gender,
            req.place,
            req.security_question,
            req.security_answer.lower()
        ))
        conn.commit()
    except sqlite3.IntegrityError:
        raise HTTPException(status_code=400, detail="Email already registered")
    finally:
        conn.close()

    return {"message": "Registration successful"}


# LOGIN

@app.post("/login")
def login(req: LoginRequest):
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()

    cursor.execute(
        "SELECT id, password FROM users WHERE email = ?",
        (req.email,)
    )
    user = cursor.fetchone()
    conn.close()

    if not user or user[1] != hash_password(req.password):
        raise HTTPException(status_code=401, detail="Invalid credentials")

    return {
        "message": "Login successful",
        "user_id": user[0],
        "session_id": str(uuid.uuid4())
    }


# FORGOT PASSWORD

class ForgotPasswordEmailRequest(BaseModel):
    email: EmailStr

@app.post("/forgot-password/email")
def forgot_password_email(req: ForgotPasswordEmailRequest):
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()

    cursor.execute("""
        SELECT security_question FROM users WHERE email = ?
    """, (req.email,))
    user = cursor.fetchone()
    conn.close()

    if not user:
        raise HTTPException(status_code=404, detail="Email not found")

    return {
        "security_question": user[0]
    }


class ForgotPasswordVerifyRequest(BaseModel):
    email: EmailStr
    security_answer: str

@app.post("/forgot-password/verify")
def forgot_password_verify(req: ForgotPasswordVerifyRequest):
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()

    cursor.execute("""
        SELECT security_answer FROM users WHERE email = ?
    """, (req.email,))
    user = cursor.fetchone()
    conn.close()

    if not user or user[0] != req.security_answer.lower():
        raise HTTPException(status_code=401, detail="Incorrect security answer")

    return {
        "message": "Security answer verified"
    }


class ForgotPasswordResetRequest(BaseModel):
    email: EmailStr
    new_password: str

@app.post("/forgot-password/reset")
def forgot_password_reset(req: ForgotPasswordResetRequest):
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()

    cursor.execute("""
        SELECT id FROM users WHERE email = ?
    """, (req.email,))
    user = cursor.fetchone()

    if not user:
        conn.close()
        raise HTTPException(status_code=404, detail="User not found")

    cursor.execute("""
        UPDATE users SET password = ? WHERE email = ?
    """, (hash_password(req.new_password), req.email))

    conn.commit()
    conn.close()

    return {
        "message": "Password reset successful"
    }