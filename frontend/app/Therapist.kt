package com.empathai.app

data class Therapist(

    val name: String,
    val type: String,   // therapist OR psychologist
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val phone: String

)