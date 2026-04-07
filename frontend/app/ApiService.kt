package com.empathai.app
import com.empathai.app.AnalysisResponse

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Body


interface ApiService {

    @POST("/chat")
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): ChatResponse

    @GET("/sessions/{user_id}")
    suspend fun getSessions(
        @Path("user_id") userId: String
    ): List<SessionResponse>

    @POST("/end-session/{user_id}")
    suspend fun endSession(
        @Path("user_id") userId: String
    ): Map<String, String>

    @GET("/user-analysis/{user_id}")
    suspend fun getUserAnalysis(
        @Path("user_id") userId: String
    ): AnalysisResponse
}

