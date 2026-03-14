package com.example.diplomproject.data.remote

import retrofit2.http.Body
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

    @POST("test-sessions/{sessionId}/answers")
    suspend fun submitAnswer(
        @Path("sessionId") sessionId: String,
        @Body request: SubmitAnswerRequestDto,
    ): SubmitAnswerResponseDto

    @GET("me/results")
    suspend fun getMyResults(): List<MyResultListItemResponseDto>

    @GET("me/results/{sessionId}")
    suspend fun getResult(@Path("sessionId") sessionId: String): FinishSessionResponseDto

    @POST("test-sessions/{sessionId}/finish")
    suspend fun finishSession(@Path("sessionId") sessionId: String): FinishSessionResponseDto
}
