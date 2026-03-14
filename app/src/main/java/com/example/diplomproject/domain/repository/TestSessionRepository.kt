package com.example.diplomproject.domain.repository

import com.example.diplomproject.domain.model.NextQuestionPayload
import com.example.diplomproject.domain.model.CandidateResultHistoryItem

interface TestSessionRepository {
    suspend fun createSession(): String
    suspend fun getNextQuestion(sessionId: String): NextQuestionPayload
    suspend fun getMyResults(): List<CandidateResultHistoryItem>
    suspend fun finishSession(sessionId: String)
}
