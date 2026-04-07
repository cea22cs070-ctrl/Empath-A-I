
# Empath AI

Empath AI is an emotion-aware conversational system designed to generate context-aware and empathetic responses. The system combines a mobile frontend with a backend model pipeline to support real-time interaction and basic emotional analysis.

---

## Project Overview

The project is divided into two main components:

### Frontend
The frontend/ module is an Android application built using Kotlin.  
It handles user interaction, chat interface, authentication, and visualization of mood-related data.

### Model Training and Backend
The model training/ module contains Python-based scripts and APIs responsible for:
- Generating chatbot responses  
- Handling authentication requests  
- Processing conversational data  
- Training and fine-tuning the model  

---

## Key Components

### Backend (model training/)
- api/server.py → Main server handling chatbot requests  
- auth_api/auth.py → Authentication handling  
- train_*.py → Scripts used for model training and fine-tuning  
- .csv files → Datasets used for training and evaluation  

### Frontend (Android)
- MainActivity.kt → Entry point of the app  
- LoginScreen.kt, RegisterScreen.kt → Authentication UI  
- AnalysisScreen.kt, MoodHistoryScreen.kt → Mood tracking and analysis  
- ApiService.kt, ApiClient.kt → API communication  
- ChatRequest.kt, ChatResponse.kt → Data handling for chat  

---

## How to Run

### Backend

1. Install Python (3.x)
2. Install required libraries (such as torch, transformers, etc.)
3. Navigate to the model training folder
4. Run the server:
