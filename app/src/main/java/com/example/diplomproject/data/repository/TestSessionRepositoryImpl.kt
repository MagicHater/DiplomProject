package com.example.diplomproject.data.repository

import com.example.diplomproject.data.remote.AppApi
import com.example.diplomproject.data.remote.FinishSessionResponseDto
import com.example.diplomproject.data.remote.MyResultListItemResponseDto
import com.example.diplomproject.data.remote.ScaleInterpretationsDto
import com.example.diplomproject.data.remote.ScaleScoresDto
import com.example.diplomproject.data.remote.SessionProgressDto
import com.example.diplomproject.data.remote.SessionQuestionDto
import com.example.diplomproject.data.remote.SubmitAnswerRequestDto
import com.example.diplomproject.data.remote.SubmitAnswerResponseDto
import com.example.diplomproject.domain.model.AnswerProgress
import com.example.diplomproject.domain.model.CandidateResultHistoryItem
import com.example.diplomproject.domain.model.FinishedSessionResult
import com.example.diplomproject.domain.model.NextQuestionPayload
import com.example.diplomproject.domain.model.ScaleInterpretations
import com.example.diplomproject.domain.model.ScaleScores
import com.example.diplomproject.domain.model.SubmitAnswerResult
import com.example.diplomproject.domain.model.TestQuestion
import com.example.diplomproject.domain.model.TestQuestionOption
import com.example.diplomproject.domain.repository.TestSessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestSessionRepositoryImpl @Inject constructor(
    private val appApi: AppApi,
) : TestSessionRepository {
    override suspend fun createSession(): String {
        return appApi.createTestSession().sessionId
    }

    override suspend fun getNextQuestion(sessionId: String): NextQuestionPayload {
        val response = appApi.getNextQuestion(sessionId)
        return NextQuestionPayload(
            hasNextQuestion = response.hasNextQuestion,
            question = response.question?.toDomain(),
        )
    }

    override suspend fun submitAnswer(
        sessionId: String,
        snapshotId: String,
        selectedOptionId: String,
    ): SubmitAnswerResult {
        return appApi.submitAnswer(
            sessionId = sessionId,
            request = SubmitAnswerRequestDto(
                snapshotId = snapshotId,
                selectedOptionId = selectedOptionId,
            ),
        ).toDomain()
    }

    override suspend fun getMyResults(): List<CandidateResultHistoryItem> {
        return appApi.getMyResults().map { it.toDomain() }
    }

    override suspend fun finishSession(sessionId: String): FinishedSessionResult {
        return appApi.finishSession(sessionId).toDomain()
    }
}

private fun SessionQuestionDto.toDomain(): TestQuestion = TestQuestion(
    snapshotId = snapshotId,
    order = order,
    text = text,
    difficulty = difficulty,
    options = options.map {
        TestQuestionOption(
            optionId = it.optionId,
            order = it.order,
            text = it.text,
        )
    },
)

private fun SubmitAnswerResponseDto.toDomain(): SubmitAnswerResult = SubmitAnswerResult(
    success = success,
    canContinue = canContinue,
    progress = progress.toDomain(),
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
