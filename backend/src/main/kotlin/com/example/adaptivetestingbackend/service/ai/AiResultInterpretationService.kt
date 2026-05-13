package com.example.adaptivetestingbackend.service.ai

import com.example.adaptivetestingbackend.ai.client.AiGenerateTextRequest
import com.example.adaptivetestingbackend.ai.client.AiTextGenerationClient
import com.example.adaptivetestingbackend.service.testsession.CandidateRiskAssessment
import com.example.adaptivetestingbackend.service.testsession.ResultCalculationService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AiResultInterpretationService(
    private val aiClient: AiTextGenerationClient,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun interpret(
        categoryName: String,
        profile: ResultCalculationService.CalculatedProfile,
        riskAssessment: CandidateRiskAssessment,
        fallbackSummary: String,
    ): String {
        val correlationId = UUID.randomUUID().toString()
        val prompt = buildPrompt(categoryName, profile, riskAssessment, fallbackSummary)

        return runCatching {
            val response = aiClient.generateText(
                AiGenerateTextRequest(
                    operation = "adaptive-test-result-interpretation",
                    prompt = prompt,
                    correlationId = correlationId,
                ),
            )
            val payload = objectMapper.readTree(cleanJson(response.content))
            val summary = payload.path("summary").asText().trim()
            val behavior = payload.path("behavior").asText().trim()
            val riskProfile = payload.path("riskProfile").asText().trim()
            val risks = payload.path("risks").map { it.asText().trim() }.filter { it.isNotBlank() }
            val recommendations = payload.path("recommendations").map { it.asText().trim() }.filter { it.isNotBlank() }

            val aiText = buildString {
                if (summary.isNotBlank()) appendLine(summary)
                if (behavior.isNotBlank()) appendLine("\nПоведенческий вывод: $behavior")
                if (riskProfile.isNotBlank()) appendLine("\nРиски сессии: $riskProfile") else appendLine("\n${riskAssessment.toSummaryBlock()}")
                if (risks.isNotEmpty()) appendLine("\nРиски: ${risks.joinToString("; ")}")
                if (recommendations.isNotEmpty()) appendLine("\nРекомендации: ${recommendations.joinToString("; ")}")
            }.trim()

            if (aiText.isBlank()) fallbackSummary else aiText
        }.getOrElse { error ->
            logger.warn(
                "AI result interpretation fallback correlationId={} providerMode={} reason={}",
                correlationId,
                aiClient.providerMode(),
                error.javaClass.simpleName,
            )
            fallbackSummary
        }
    }

    private fun buildPrompt(
        categoryName: String,
        profile: ResultCalculationService.CalculatedProfile,
        riskAssessment: CandidateRiskAssessment,
        fallbackSummary: String,
    ): String {
        val explanations = profile.scaleExplanations.entries.joinToString("\n") { (scale, explanation) ->
            "- ${scaleTitle(scale)}: $explanation"
        }
        val reliability = if (profile.reliabilityFlags.isEmpty()) {
            "Флаги достоверности не выявлены."
        } else {
            profile.reliabilityFlags.joinToString("; ")
        }
        val riskFactors = if (riskAssessment.factors.isEmpty()) {
            "Существенных факторов риска не выявлено."
        } else {
            riskAssessment.factors.joinToString("\n") { factor ->
                "- ${factor.title}: ${factor.description}"
            }
        }

        return """
            Ты анализируешь результат адаптивного психологического/поведенческого теста кандидата.
            Нужно дать краткое профессиональное объяснение поведения кандидата по итогам ответов и описать риски конкретной сессии.

            Категория теста: $categoryName

            Баллы:
            - Внимание: ${profile.attention}
            - Стрессоустойчивость: ${profile.stressResistance}
            - Ответственность: ${profile.responsibility}
            - Адаптивность: ${profile.adaptability}
            - Скорость и точность решений: ${profile.decisionSpeedAccuracy}

            Математическое объяснение по метрикам:
            $explanations

            Достоверность результата: ${profile.reliabilityFactor}
            Флаги достоверности: $reliability

            Строгая оценка риска системы:
            Уровень риска: ${riskAssessment.title}
            Факторы риска:
            $riskFactors
            Рекомендация системы: ${riskAssessment.recommendation}

            Базовое резюме системы:
            $fallbackSummary

            Требования:
            - верни только валидный JSON;
            - не используй markdown;
            - не ставь медицинские диагнозы;
            - не делай категоричных выводов о личности;
            - формулируй как профессиональную интерпретацию поведения в рамках теста;
            - обязательно укажи конкретные поведенческие склонности, например: "кандидат склонен выбирать решения без анализа изменений среды";
            - объясняй вывод через данные: слабые ответы, положительные ответы, вариативность, достоверность;
            - riskProfile должен содержать риски конкретной сессии и причины;
            - текст должен быть на русском языке;
            - максимум 5 коротких предложений в summary;
            - behavior должен быть одним конкретным поведенческим выводом.

            Формат:
            {
              "summary": "краткое объяснение результата",
              "behavior": "конкретная поведенческая склонность кандидата",
              "riskProfile": "уровень риска и причины",
              "risks": ["риск 1", "риск 2"],
              "recommendations": ["рекомендация 1", "рекомендация 2"]
            }
        """.trimIndent()
    }

    private fun scaleTitle(scale: String): String = when (scale) {
        "attention" -> "Внимание"
        "stress_resistance" -> "Стрессоустойчивость"
        "responsibility" -> "Ответственность"
        "adaptability" -> "Адаптивность"
        "decision_speed_accuracy" -> "Скорость и точность решений"
        else -> scale
    }

    private fun cleanJson(raw: String): String = raw
        .trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()
}
