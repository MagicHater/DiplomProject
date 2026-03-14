package com.example.diplomproject.domain.usecase

import com.example.diplomproject.domain.model.FinishedSessionResult
import com.example.diplomproject.domain.repository.TestSessionRepository
import javax.inject.Inject

class GetCandidateResultUseCase @Inject constructor(
    private val testSessionRepository: TestSessionRepository,
) {
    suspend operator fun invoke(sessionId: String): FinishedSessionResult {
        require(sessionId.isNotBlank()) { "Пустой идентификатор сессии" }
        return testSessionRepository.getResult(sessionId)
    }
}
