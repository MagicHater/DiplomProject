package com.example.diplomproject.domain.usecase

import com.example.diplomproject.domain.model.StartedTestSession
import com.example.diplomproject.domain.repository.TestSessionRepository
import javax.inject.Inject

class StartCandidateTestUseCase @Inject constructor(
    private val testSessionRepository: TestSessionRepository,
) {
    suspend operator fun invoke(categoryId: String): StartedTestSession {
        val categories = testSessionRepository.getCategories()
        val category = categories.firstOrNull { it.id == categoryId }
            ?: throw IllegalArgumentException("Категория не найдена")

        val sessionId = testSessionRepository.createSession(categoryId)
        require(sessionId.isNotBlank()) { "Сервер вернул пустой идентификатор сессии" }

        val nextQuestionPayload = testSessionRepository.getNextQuestion(sessionId)
        check(nextQuestionPayload.hasNextQuestion) { "Сервер не вернул вопрос для новой сессии" }

        val firstQuestion = nextQuestionPayload.question ?: error("Сервер вернул пустой вопрос")

        return StartedTestSession(
            sessionId = sessionId,
            category = category,
            firstQuestion = firstQuestion,
        )
    }
}
