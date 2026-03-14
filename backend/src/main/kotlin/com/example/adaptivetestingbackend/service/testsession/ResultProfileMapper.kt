package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.dto.testsession.MyResultListItemResponse
import com.example.adaptivetestingbackend.dto.testsession.ResultProfileResponse
import com.example.adaptivetestingbackend.dto.testsession.ScaleInterpretationsDto
import com.example.adaptivetestingbackend.dto.testsession.ScaleScoresDto
import com.example.adaptivetestingbackend.entity.ResultProfileEntity
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@Component
class ResultProfileMapper {
    fun toResultProfile(profile: ResultProfileEntity): ResultProfileResponse {
        val completedAt = profile.session.completedAt
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Session completedAt is missing")

        val scores = ScaleScoresDto(
            attention = profile.attentionScore,
            stressResistance = profile.stressResistanceScore,
            responsibility = profile.responsibilityScore,
            adaptability = profile.adaptabilityScore,
            decisionSpeedAccuracy = profile.decisionSpeedAccuracyScore,
        )

        return ResultProfileResponse(
            sessionId = profile.session.id,
            completedAt = completedAt,
            scores = scores,
            interpretations = ScaleInterpretationsDto(
                attention = interpretScale("Внимательность", profile.attentionScore),
                stressResistance = interpretScale("Стрессоустойчивость", profile.stressResistanceScore),
                responsibility = interpretScale("Ответственность", profile.responsibilityScore),
                adaptability = interpretScale("Адаптивность", profile.adaptabilityScore),
                decisionSpeedAccuracy = interpretScale("Скорость/точность решений", profile.decisionSpeedAccuracyScore),
            ),
            overallSummary = profile.summary ?: buildOverallSummary(
                ResultCalculationService.CalculatedProfile(
                    attention = profile.attentionScore,
                    stressResistance = profile.stressResistanceScore,
                    responsibility = profile.responsibilityScore,
                    adaptability = profile.adaptabilityScore,
                    decisionSpeedAccuracy = profile.decisionSpeedAccuracyScore,
                ),
            ),
        )
    }

    fun toResultListItem(profile: ResultProfileEntity): MyResultListItemResponse {
        val detailedResult = toResultProfile(profile)
        return MyResultListItemResponse(
            sessionId = detailedResult.sessionId,
            completedAt = detailedResult.completedAt,
            summary = detailedResult.overallSummary,
            scores = detailedResult.scores,
        )
    }

    fun buildOverallSummary(profile: ResultCalculationService.CalculatedProfile): String {
        val scales = listOf(
            "внимательность" to profile.attention,
            "стрессоустойчивость" to profile.stressResistance,
            "ответственность" to profile.responsibility,
            "адаптивность" to profile.adaptability,
            "скорость/точность решений" to profile.decisionSpeedAccuracy,
        )
        val topScale = scales.maxBy { it.second }
        val growthScale = scales.minBy { it.second }
        return "Сильная сторона: ${topScale.first}. Зона роста: ${growthScale.first}."
    }

    private fun interpretScale(scaleTitle: String, score: BigDecimal): String {
        return when {
            score < BigDecimal("34") -> "$scaleTitle: зона развития"
            score < BigDecimal("67") -> "$scaleTitle: стабильный средний уровень"
            else -> "$scaleTitle: выраженная сильная сторона"
        }
    }
}
