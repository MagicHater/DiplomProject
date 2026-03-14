package com.example.diplomproject.domain.model

data class TestQuestionOption(
    val optionId: String,
    val order: Int,
    val text: String,
)

data class TestQuestion(
    val snapshotId: String,
    val order: Int,
    val text: String,
    val difficulty: Int,
    val options: List<TestQuestionOption>,
)

data class StartedTestSession(
    val sessionId: String,
    val firstQuestion: TestQuestion,
)

data class NextQuestionPayload(
    val hasNextQuestion: Boolean,
    val question: TestQuestion?,
)

data class ScaleScores(
    val attention: Int,
    val stressResistance: Int,
    val responsibility: Int,
    val adaptability: Int,
    val decisionSpeedAccuracy: Int,
)

data class CandidateResultHistoryItem(
    val sessionId: String,
    val completedAt: String,
    val summary: String,
    val scores: ScaleScores,
)
