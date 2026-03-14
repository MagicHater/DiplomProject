package com.example.diplomproject.domain.model

import java.io.Serializable

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
    val firstQuestion: TestQuestion,
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
