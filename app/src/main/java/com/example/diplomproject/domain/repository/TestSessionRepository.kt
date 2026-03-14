package com.example.diplomproject.domain.repository

import com.example.diplomproject.domain.model.CandidateResultHistoryItem
import com.example.diplomproject.domain.model.FinishedSessionResult
import com.example.diplomproject.domain.model.NextQuestionPayload
import com.example.diplomproject.domain.model.SubmitAnswerResult

interface TestSessionRepository {
    suspend fun createSession(): String
    suspend fun getNextQuestion(sessionId: String): NextQuestionPayload
    suspend fun submitAnswer(sessionId: String, snapshotId: String, selectedOptionId: String): SubmitAnswerResult
    suspend fun getMyResults(): List<CandidateResultHistoryItem>
    suspend fun finishSession(sessionId: String): FinishedSessionResult
}
