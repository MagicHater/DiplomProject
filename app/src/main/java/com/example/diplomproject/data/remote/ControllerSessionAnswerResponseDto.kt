package com.example.diplomproject.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ControllerSessionAnswerResponseDto(
    @SerialName("questionOrder") val questionOrder: Int,
    @SerialName("questionText") val questionText: String,
    @SerialName("selectedAnswerText") val selectedAnswerText: String,
    @SerialName("answerValue") val answerValue: Double? = null,
    @SerialName("responseTimeMs") val responseTimeMs: Long? = null,
    @SerialName("difficulty") val difficulty: Int,
)
