package com.example.diplomproject.domain.usecase

import com.example.diplomproject.domain.model.NextQuestionPayload
import com.example.diplomproject.domain.repository.TestSessionRepository
import javax.inject.Inject

class GetNextCandidateQuestionUseCase @Inject constructor(
    private val testSessionRepository: TestSessionRepository,
) {
    suspend operator fun invoke(sessionId: String): NextQuestionPayload {
        require(sessionId.isNotBlank()) { "Пустой идентификатор сессии" }
        return testSessionRepository.getNextQuestion(sessionId)
    }
}
