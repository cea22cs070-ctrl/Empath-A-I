package com.empathai.app

data class ChatResponse(
    val reply: String,
    val emotion_detected: String,
    val mood_history: List<String>,
    val user_id: String
)