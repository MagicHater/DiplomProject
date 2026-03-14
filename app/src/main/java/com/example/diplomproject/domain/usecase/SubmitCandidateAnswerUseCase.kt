package com.example.diplomproject.domain.usecase

import com.example.diplomproject.domain.model.SubmitAnswerResult
import com.example.diplomproject.domain.repository.TestSessionRepository
import javax.inject.Inject

class SubmitCandidateAnswerUseCase @Inject constructor(
    private val testSessionRepository: TestSessionRepository,
) {
    suspend operator fun invoke(
        sessionId: String,
        snapshotId: String,
        selectedOptionId: String,
    ): SubmitAnswerResult {
        require(sessionId.isNotBlank()) { "Пустой идентификатор сессии" }
        require(snapshotId.isNotBlank()) { "Пустой идентификатор вопроса" }
        require(selectedOptionId.isNotBlank()) { "Пустой идентификатор ответа" }
        return testSessionRepository.submitAnswer(sessionId, snapshotId, selectedOptionId)
    }
}
