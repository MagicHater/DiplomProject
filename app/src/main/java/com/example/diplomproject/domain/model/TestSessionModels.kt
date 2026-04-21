package com.example.diplomproject.domain.model

import java.io.Serializable

data class TestCategory(
    val id: String,
    val code: String,
    val name: String,
    val description: String? = null,
) : Serializable

data class ControllerTokenItem(
    val token: String,
    val category: TestCategory,
    val createdAt: String,
    val isUsed: Boolean,
) : Serializable

data class TokenPreview(
    val valid: Boolean,
    val used: Boolean,
    val category: TestCategory? = null,
    val requiresAuth: Boolean = false,
) : Serializable

data class TokenSessionStartResult(
    val sessionId: String,
    val category: TestCategory,
    val guestSession: Boolean = false,
    val guestSessionKey: String? = null,
) : Serializable

data class TestQuestionOption(
    val optionId: String,
    val order: Int,
    val text: String,
) : Serializable

data class TestQuestion(
    val snapshotId: String,
    val order: Int,
    val text: String,
    val difficulty: Int,
    val options: List<TestQuestionOption>,
) : Serializable

data class StartedTestSession(
    val sessionId: String,
    val category: TestCategory,
    val firstQuestion: TestQuestion,
    val guestSession: Boolean = false,
    val guestSessionKey: String? = null,
) : Serializable

data class NextQuestionPayload(
    val hasNextQuestion: Boolean,
    val question: TestQuestion?,
) : Serializable

data class AnswerProgress(
    val answeredQuestions: Int,
    val issuedQuestions: Int,
    val totalAvailableQuestions: Int,
    val completionPercent: Int,
) : Serializable

data class SubmitAnswerResult(
    val success: Boolean,
    val canContinue: Boolean,
    val progress: AnswerProgress,
) : Serializable

data class ScaleScores(
    val attention: Double,
    val stressResistance: Double,
    val responsibility: Double,
    val adaptability: Double,
    val decisionSpeedAccuracy: Double,
) : Serializable

data class ScaleInterpretations(
    val attention: String,
    val stressResistance: String,
    val responsibility: String,
    val adaptability: String,
    val decisionSpeedAccuracy: String,
) : Serializable

data class FinishedSessionResult(
    val sessionId: String,
    val completedAt: String,
    val scores: ScaleScores,
    val interpretations: ScaleInterpretations,
    val overallSummary: String,
) : Serializable

data class CandidateResultHistoryItem(
    val sessionId: String,
    val completedAt: String,
    val summary: String,
    val scores: ScaleScores,
) : Serializable


data class ControllerTokenResultHistoryItem(
    val sessionId: String,
    val completedAt: String,
    val category: TestCategory,
    val participantType: String,
    val participantDisplayName: String? = null,
    val summary: String,
    val scores: ScaleScores,
) : Serializable
