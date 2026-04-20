package com.example.diplomproject.data.repository

import com.example.diplomproject.data.local.SessionManager
import com.example.diplomproject.data.remote.AppApi
import com.example.diplomproject.data.remote.ControllerTokenRequestDto
import com.example.diplomproject.data.remote.ControllerTokenResponseDto
import com.example.diplomproject.data.remote.CreateTestSessionRequestDto
import com.example.diplomproject.data.remote.FinishSessionResponseDto
import com.example.diplomproject.data.remote.MyResultListItemResponseDto
import com.example.diplomproject.data.remote.ScaleInterpretationsDto
import com.example.diplomproject.data.remote.ScaleScoresDto
import com.example.diplomproject.data.remote.SessionProgressDto
import com.example.diplomproject.data.remote.SessionQuestionDto
import com.example.diplomproject.data.remote.StartCandidateByTokenRequestDto
import com.example.diplomproject.data.remote.StartGuestByTokenRequestDto
import com.example.diplomproject.data.remote.SubmitAnswerRequestDto
import com.example.diplomproject.data.remote.SubmitAnswerResponseDto
import com.example.diplomproject.data.remote.TestCategoryDto
import com.example.diplomproject.data.remote.TokenPreviewRequestDto
import com.example.diplomproject.domain.model.AnswerProgress
import com.example.diplomproject.domain.model.CandidateResultHistoryItem
import com.example.diplomproject.domain.model.ControllerTokenItem
import com.example.diplomproject.domain.model.FinishedSessionResult
import com.example.diplomproject.domain.model.NextQuestionPayload
import com.example.diplomproject.domain.model.ScaleInterpretations
import com.example.diplomproject.domain.model.ScaleScores
import com.example.diplomproject.domain.model.SubmitAnswerResult
import com.example.diplomproject.domain.model.TestCategory
import com.example.diplomproject.domain.model.TestQuestion
import com.example.diplomproject.domain.model.TestQuestionOption
import com.example.diplomproject.domain.model.TokenPreview
import com.example.diplomproject.domain.model.TokenSessionStartResult
import com.example.diplomproject.domain.repository.TestSessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestSessionRepositoryImpl @Inject constructor(
    private val appApi: AppApi,
    private val sessionManager: SessionManager,
) : TestSessionRepository {

    override suspend fun getCategories(): List<TestCategory> = appApi.getTestCategories().map { it.toDomain() }

    override suspend fun createSession(categoryId: String): String {
        sessionManager.clearGuestSessionKey()
        return appApi.createTestSession(CreateTestSessionRequestDto(categoryId)).sessionId
    }

    override suspend fun previewToken(token: String): TokenPreview =
        appApi.previewToken(TokenPreviewRequestDto(token)).toDomain()

    override suspend fun startGuestByToken(token: String, guestName: String): TokenSessionStartResult {
        val response = appApi.startGuestByToken(StartGuestByTokenRequestDto(token, guestName))
        val guestSessionKey = response.guestSessionKey
        require(!guestSessionKey.isNullOrBlank()) { "Сервер не вернул guestSessionKey" }
        sessionManager.saveGuestSessionKey(guestSessionKey)
        return response.toStartResult()
    }

    override suspend fun startCandidateByToken(token: String): String =
        appApi.startCandidateByToken(StartCandidateByTokenRequestDto(token)).sessionId.also {
            sessionManager.clearGuestSessionKey()
        }

    override suspend fun createControllerToken(categoryId: String): ControllerTokenItem =
        appApi.createControllerToken(ControllerTokenRequestDto(categoryId)).toDomain()

    override suspend fun getControllerTokens(): List<ControllerTokenItem> =
        appApi.getControllerTokens().map { it.toDomain() }

    override suspend fun getNextQuestion(sessionId: String): NextQuestionPayload {
        val response = appApi.getNextQuestion(sessionId)
        return NextQuestionPayload(response.hasNextQuestion, response.question?.toDomain())
    }

    override suspend fun submitAnswer(sessionId: String, snapshotId: String, selectedOptionId: String): SubmitAnswerResult {
        return appApi.submitAnswer(
            sessionId = sessionId,
            request = SubmitAnswerRequestDto(snapshotId, selectedOptionId),
        ).toDomain()
    }

    override suspend fun getMyResults(): List<CandidateResultHistoryItem> = appApi.getMyResults().map { it.toDomain() }

    override suspend fun getResult(sessionId: String): FinishedSessionResult = appApi.getResult(sessionId).toDomain()

    override suspend fun finishSession(sessionId: String): FinishedSessionResult {
        val result = appApi.finishSession(sessionId).toDomain()
        sessionManager.clearGuestSessionKey()
        return result
    }
}

private fun TestCategoryDto.toDomain(): TestCategory = TestCategory(id, code, name, description)

private fun SessionQuestionDto.toDomain(): TestQuestion = TestQuestion(
    snapshotId = snapshotId,
    order = order,
    text = text,
    difficulty = difficulty,
    options = options.map { TestQuestionOption(it.optionId, it.order, it.text) },
)

private fun SubmitAnswerResponseDto.toDomain(): SubmitAnswerResult = SubmitAnswerResult(
    success = success,
    canContinue = canContinue,
    progress = progress.toDomain(),
)

private fun com.example.diplomproject.data.remote.TokenPreviewResponseDto.toDomain(): TokenPreview =
    TokenPreview(valid = valid, used = used, category = category?.toDomain(), requiresAuth = requiresAuth)

private fun com.example.diplomproject.data.remote.CreateTestSessionResponseDto.toStartResult(): TokenSessionStartResult =
    TokenSessionStartResult(
        sessionId = sessionId,
        category = category.toDomain(),
        guestSession = guestSession,
        guestSessionKey = guestSessionKey,
    )

private fun SessionProgressDto.toDomain(): AnswerProgress = AnswerProgress(
    answeredQuestions = answeredQuestions,
    issuedQuestions = issuedQuestions,
    totalAvailableQuestions = totalAvailableQuestions,
    completionPercent = completionPercent,
)

private fun MyResultListItemResponseDto.toDomain(): CandidateResultHistoryItem = CandidateResultHistoryItem(
    sessionId = sessionId,
    completedAt = completedAt,
    summary = summary,
    scores = scores.toDomain(),
)

private fun FinishSessionResponseDto.toDomain(): FinishedSessionResult = FinishedSessionResult(
    sessionId = sessionId,
    completedAt = completedAt,
    scores = scores.toDomain(),
    interpretations = interpretations.toDomain(),
    overallSummary = overallSummary,
)

private fun ScaleScoresDto.toDomain(): ScaleScores = ScaleScores(
    attention = attention,
    stressResistance = stressResistance,
    responsibility = responsibility,
    adaptability = adaptability,
    decisionSpeedAccuracy = decisionSpeedAccuracy,
)

private fun ScaleInterpretationsDto.toDomain(): ScaleInterpretations = ScaleInterpretations(
    attention = attention,
    stressResistance = stressResistance,
    responsibility = responsibility,
    adaptability = adaptability,
    decisionSpeedAccuracy = decisionSpeedAccuracy,
)

private fun ControllerTokenResponseDto.toDomain(): ControllerTokenItem = ControllerTokenItem(
    token = token,
    category = category.toDomain(),
    createdAt = createdAt,
    isUsed = isUsed,
)
