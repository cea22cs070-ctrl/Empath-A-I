package com.empathai.app

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("/forgot-password/email")
    suspend fun forgotPasswordEmail(
        @Body request: ForgotEmailRequest
    ): Response<ForgotEmailResponse>

    @POST("/forgot-password/verify")
    suspend fun forgotPasswordVerify(
        @Body request: ForgotVerifyRequest
    ): Response<Unit>

    @POST("/forgot-password/reset")
    suspend fun forgotPasswordReset(
        @Body request: ForgotResetRequest
    ): Response<Unit>
}