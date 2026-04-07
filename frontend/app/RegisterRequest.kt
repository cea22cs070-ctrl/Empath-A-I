package com.empathai.app

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val age: Int,
    val gender: String,
    val place: String,
    val security_question: String,
    val security_answer: String
)