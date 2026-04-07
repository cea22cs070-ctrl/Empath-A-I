package com.empathai.app

data class SessionResponse(
    val date: String,
    val summary: String,
    val dominant_mood: String,
    val total_turns: Int
)