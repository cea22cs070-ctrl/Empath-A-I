package com.empathai.app

object TherapistDatabase {

    val therapists = listOf(

        Therapist(
            "Dr. Sreerag Nair",
            "psychologist",
            8.5241,
            76.9366,
            "Mental Wellness Clinic, Trivandrum",
            "9876543210"
        ),

        Therapist(
            "Dr. Sidharth Menon",
            "therapist",
            8.5200,
            76.9400,
            "MindCare Therapy Center, Trivandrum",
            "9876543211"
        ),

        Therapist(
            "Dr. Jishnu Varma",
            "psychologist",
            8.5300,
            76.9300,
            "City Mental Health Hospital",
            "9876543212"
        ),

        Therapist(
            "Dr. Danish Rahman",
            "therapist",
            8.5220,
            76.9350,
            "Hope Counseling Center",
            "9876543213"
        )

    )

    // Psychologists
    val psychologists = listOf(

        Therapist(
            name = "Dr. John Carter",
            type = "psychologist",
            latitude = 8.5260,
            longitude = 76.9380,
            address = "City Psychology Clinic",
            phone = "9876543212"
        ),

        Therapist(
            name = "Dr. Marie Wilson",
            type = "psychologist",
            latitude = 9.4981,
            longitude = 76.3388,
            address = "Mental Wellness Center",
            phone = "9876543213"
        )

    )

}