package com.example.diplomproject.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AppApi {
    @GET("health")
    suspend fun getHealth(): HealthResponse

    @GET("test-categories")
    suspend fun getTestCategories(): List<TestCategoryDto>

    @POST("test-sessions")
    suspend fun createTestSession(@Body request: CreateTestSessionRequestDto): CreateTestSessionResponseDto

    @POST("token-access/preview")
    suspend fun previewToken(@Body request: TokenPreviewRequestDto): TokenPreviewResponseDto

    @POST("token-access/start-guest")
    suspend fun startGuestByToken(@Body request: StartGuestByTokenRequestDto): CreateTestSessionResponseDto

    @POST("token-access/start-candidate")
    suspend fun startCandidateByToken(@Body request: StartCandidateByTokenRequestDto): CreateTestSessionResponseDto

    @GET("controller/test-management/categories")
    suspend fun getControllerCategories(): List<TestCategoryDto>

    @POST("controller/test-management/tokens")
    suspend fun createControllerToken(@Body request: ControllerTokenRequestDto): ControllerTokenResponseDto

    @GET("controller/test-management/tokens")
    suspend fun getControllerTokens(): List<ControllerTokenResponseDto>

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
