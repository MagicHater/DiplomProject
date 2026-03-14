package com.example.diplomproject.domain.usecase

import com.example.diplomproject.domain.model.StartedTestSession
import com.example.diplomproject.domain.repository.TestSessionRepository
import javax.inject.Inject

class StartCandidateTestUseCase @Inject constructor(
    private val testSessionRepository: TestSessionRepository,
) {
    suspend operator fun invoke(): StartedTestSession {
        val sessionId = testSessionRepository.createSession()
        require(sessionId.isNotBlank()) { "Сервер вернул пустой идентификатор сессии" }

        val nextQuestionPayload = testSessionRepository.getNextQuestion(sessionId)
        check(nextQuestionPayload.hasNextQuestion) {
            "Сервер не вернул вопрос для новой сессии"
        }

        val firstQuestion = nextQuestionPayload.question
            ?: error("Сервер вернул пустой вопрос")

        return StartedTestSession(
            sessionId = sessionId,
            firstQuestion = firstQuestion,
        )
    }
}
