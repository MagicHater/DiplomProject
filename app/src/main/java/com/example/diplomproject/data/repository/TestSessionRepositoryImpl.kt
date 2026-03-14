package com.example.diplomproject.data.repository

import com.example.diplomproject.data.remote.AppApi
import com.example.diplomproject.data.remote.SessionQuestionDto
import com.example.diplomproject.domain.model.NextQuestionPayload
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
