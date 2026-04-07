package com.empathai.app

import com.google.gson.annotations.SerializedName

data class ForgotEmailRequest(
    val email: String
)

data class ForgotVerifyRequest(
    val email: String,
    @SerializedName("security_answer") val securityAnswer: String
)

data class ForgotResetRequest(
    val email: String,
    @SerializedName("new_password") val newPassword: String
)
data class ForgotEmailResponse(
    @SerializedName("security_question")
    val securityQuestion: String
)