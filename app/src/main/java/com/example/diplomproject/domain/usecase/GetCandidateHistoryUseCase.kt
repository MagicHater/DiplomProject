package com.example.diplomproject.domain.usecase

import com.example.diplomproject.domain.model.CandidateResultHistoryItem
import com.example.diplomproject.domain.repository.TestSessionRepository
import javax.inject.Inject

class GetCandidateHistoryUseCase @Inject constructor(
    private val testSessionRepository: TestSessionRepository,
) {
    suspend operator fun invoke(): List<CandidateResultHistoryItem> = testSessionRepository.getMyResults()
}
