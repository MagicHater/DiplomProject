package com.example.diplomproject.domain.usecase

import com.example.diplomproject.domain.repository.TestSessionRepository
import javax.inject.Inject

class FinishCandidateTestUseCase @Inject constructor(
    private val testSessionRepository: TestSessionRepository,
) {
    suspend operator fun invoke(sessionId: String) {
        require(sessionId.isNotBlank()) { "Пустой идентификатор сессии" }
        testSessionRepository.finishSession(sessionId)
    }
}
