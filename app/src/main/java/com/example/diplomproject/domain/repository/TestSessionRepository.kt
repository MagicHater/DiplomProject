package com.example.diplomproject.domain.repository

import com.example.diplomproject.domain.model.NextQuestionPayload

interface TestSessionRepository {
    suspend fun createSession(): String
    suspend fun getNextQuestion(sessionId: String): NextQuestionPayload
}
