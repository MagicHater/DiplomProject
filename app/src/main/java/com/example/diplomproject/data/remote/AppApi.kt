package com.example.diplomproject.data.remote

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AppApi {
    @GET("health")
    suspend fun getHealth(): HealthResponse

    @POST("test-sessions")
    suspend fun createTestSession(): CreateTestSessionResponseDto

    @GET("test-sessions/{sessionId}/next-question")
    suspend fun getNextQuestion(@Path("sessionId") sessionId: String): NextQuestionResponseDto

    @GET("me/results")
    suspend fun getMyResults(): List<MyResultListItemResponseDto>

    @POST("test-sessions/{sessionId}/finish")
    suspend fun finishSession(@Path("sessionId") sessionId: String): FinishSessionResponseDto
}
