package com.example.adaptivetestingbackend.dto.testsession

import java.util.UUID

data class NextQuestionResponse(
    val sessionId: UUID,
    val status: String,
    val hasNextQuestion: Boolean,
    val question: SessionQuestionDto?,
)

data class SessionQuestionDto(
    val snapshotId: UUID,
    val order: Int,
    val text: String,
    val difficulty: Short,
    val options: List<SessionQuestionOptionDto>,
)

data class SessionQuestionOptionDto(
    val optionId: UUID,
    val order: Short,
    val text: String,
)
