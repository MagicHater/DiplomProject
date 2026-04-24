package com.example.diplomproject.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AppApi {
    @GET("health")
    suspend fun getHealth(): HealthResponse

    @GET("controller/dashboard")
    suspend fun getControllerDashboard(): ControllerDashboardResponseDto

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

    @GET("token-management/categories")
    suspend fun getControllerCategories(): List<TestCategoryDto>

    @POST("token-management/tokens")
    suspend fun createControllerToken(
        @Body request: ControllerTokenRequestDto
    ): ControllerTokenResponseDto

    @POST("token-management/tests")
    suspend fun createControllerTest(
        @Body request: CreateControllerTestRequestDto
    ): CreateControllerTestResponseDto

    @GET("token-management/tokens")
    suspend fun getControllerTokens(): List<ControllerTokenResponseDto>

    @GET("token-management/results")
    suspend fun getControllerTokenResults(): List<ControllerTokenResultListItemResponseDto>

    @GET("token-management/results/{sessionId}")
    suspend fun getControllerTokenResult(@Path("sessionId") sessionId: String): FinishSessionResponseDto

    @GET("custom-tests/available")
    suspend fun getAvailableCustomTests(): List<CustomTestListItemDto>

    @POST("custom-tests")
    suspend fun createCustomTest(@Body request: CreateCustomTestRequestDto): CreateCustomTestResponseDto

    @GET("custom-tests/my")
    suspend fun getMyCustomTests(): List<CustomTestListItemDto>

    @GET("custom-tests/{testId}")
    suspend fun getCustomTestDetails(@Path("testId") testId: String): CustomTestDetailsDto

    @POST("custom-tests/{testId}/submissions")
    suspend fun submitCustomTest(
        @Path("testId") testId: String,
        @Body request: CustomTestSubmissionRequestDto,
    )

    @GET("custom-tests/{testId}/results")
    suspend fun getCustomTestResults(@Path("testId") testId: String): List<CustomTestResultItemDto>

    @GET("custom-tests/{testId}/statistics")
    suspend fun getCustomTestStatistics(@Path("testId") testId: String): CustomTestStatisticsDto

    @GET("controller/candidates")
    suspend fun getControllerParticipants(): List<ControllerParticipantListItemResponseDto>

    @GET("controller/candidates/results")
    suspend fun getControllerParticipantResults(
        @Query("participantType") participantType: String,
        @Query("participantKey") participantKey: String,
    ): ControllerParticipantResultsResponseDto

    @GET("test-sessions/{sessionId}/next-question")
    suspend fun getNextQuestion(
        @Path("sessionId") sessionId: String,
        @Header("X-Guest-Session-Key") guestSessionKey: String? = null,
    ): NextQuestionResponseDto

    @POST("test-sessions/{sessionId}/answers")
    suspend fun submitAnswer(
        @Path("sessionId") sessionId: String,
        @Header("X-Guest-Session-Key") guestSessionKey: String? = null,
        @Body request: SubmitAnswerRequestDto,
    ): SubmitAnswerResponseDto

    @GET("me/results")
    suspend fun getMyResults(): List<MyResultListItemResponseDto>

    @GET("me/results/{sessionId}")
    suspend fun getResult(@Path("sessionId") sessionId: String): FinishSessionResponseDto

    @POST("test-sessions/{sessionId}/finish")
    suspend fun finishSession(
        @Path("sessionId") sessionId: String,
        @Header("X-Guest-Session-Key") guestSessionKey: String? = null,
    ): FinishSessionResponseDto
}
