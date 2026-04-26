package com.example.adaptivetestingbackend.service.ai

import com.example.adaptivetestingbackend.ai.client.AiGenerateTextRequest
import com.example.adaptivetestingbackend.ai.client.AiTextGenerationClient
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AiAdaptiveQuestionService(
    private val aiClient: AiTextGenerationClient,
    private val objectMapper: ObjectMapper,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun generateQuestion(
        categoryName: String,
        targetScale: String,
        sourceQuestionText: String,
        difficulty: Int,
        options: List<String>,
    ): GeneratedAdaptiveQuestion {
        if (options.isEmpty()) {
            return GeneratedAdaptiveQuestion(sourceQuestionText, options)
        }

        val correlationId = UUID.randomUUID().toString()
        val prompt = buildPrompt(
            categoryName = categoryName,
            targetScale = targetScale,
            sourceQuestionText = sourceQuestionText,
            difficulty = difficulty,
            options = options,
        )

        return runCatching {
            val response = aiClient.generateText(
                AiGenerateTextRequest(
                    operation = "adaptive-test-question",
                    prompt = prompt,
                    correlationId = correlationId,
                ),
            )

            val payload = objectMapper.readTree(cleanJson(response.content))
            val generatedText = payload.path("text").asText().trim()
            val generatedOptions = payload.path("options")
                .map { it.asText().trim() }
                .filter { it.isNotBlank() }

            if (generatedText.isBlank() || generatedOptions.size < options.size) {
                logger.warn(
                    "AI adaptive question fallback correlationId={} providerMode={} reason=invalid_payload category={} scale={}",
                    correlationId,
                    aiClient.providerMode(),
                    categoryName,
                    targetScale,
                )
                fallbackQuestion(categoryName, targetScale, difficulty, options)
            } else {
                logger.info(
                    "AI adaptive question generated correlationId={} providerMode={} result=success category={} scale={} difficulty={}",
                    correlationId,
                    aiClient.providerMode(),
                    categoryName,
                    targetScale,
                    difficulty,
                )
                GeneratedAdaptiveQuestion(
                    text = generatedText,
                    options = generatedOptions.take(options.size),
                )
            }
        }.getOrElse { error ->
            logger.warn(
                "AI adaptive question fallback correlationId={} providerMode={} reason={} category={} scale={}",
                correlationId,
                aiClient.providerMode(),
                error.javaClass.simpleName,
                categoryName,
                targetScale,
            )
            fallbackQuestion(categoryName, targetScale, difficulty, options)
        }
    }

    private fun buildPrompt(
        categoryName: String,
        targetScale: String,
        sourceQuestionText: String,
        difficulty: Int,
        options: List<String>,
    ): String {
        val optionsText = options.mapIndexed { index, option ->
            "${index + 1}. $option"
        }.joinToString("\n")
        val context = categoryContext(categoryName)
        val scaleDescription = scaleDescription(targetScale)
        val difficultyDescription = difficultyDescription(difficulty)

        return """
            Ты генерируешь НОВЫЙ вопрос для адаптивного психологического и поведенческого тестирования кандидата.

            Важно: НЕ переформулируй базовый вопрос. Базовый вопрос используется только как внутренний психометрический шаблон, чтобы сохранить логику оценки.
            Нужно создать новую реалистичную рабочую ситуацию с нуля.

            Категория теста: $categoryName
            Тематический контекст категории: $context
            Проверяемая шкала: $targetScale — $scaleDescription
            Сложность: $difficulty — $difficultyDescription

            Психометрический шаблон, который нельзя копировать дословно и нельзя просто перефразировать:
            $sourceQuestionText

            Исходные уровни поведения. Порядок менять нельзя, потому что порядок соответствует оценочным весам от слабого поведения к сильному:
            $optionsText

            Требования:
            - верни только валидный JSON;
            - не используй markdown и блоки ```;
            - не добавляй пояснения до или после JSON;
            - текст должен быть на русском языке;
            - вопрос должен быть новой конкретной ситуацией именно из категории "$categoryName";
            - вопрос должен проверять шкалу "$targetScale";
            - нельзя копировать формулировку базового вопроса;
            - нельзя оставлять универсальную формулировку без связи с категорией;
            - варианты должны быть новыми формулировками поведения кандидата;
            - варианты должны сохранять оценочный порядок: 1 — самое слабое поведение, последний — самое сильное поведение;
            - количество вариантов должно быть ровно ${options.size};
            - не используй шаблонные значения: "Текст вопроса", "Вариант 1", "Вариант 2", "string".

            Формат ответа:
            {
              "text": "новый ситуационный вопрос по категории $categoryName и шкале $targetScale",
              "options": ["самое слабое поведение", "...", "самое сильное поведение"]
            }
        """.trimIndent()
    }

    private fun categoryContext(categoryName: String): String {
        val normalized = categoryName.trim().lowercase()
        return when {
            "smm" in normalized -> "работа SMM-специалиста: контент-план, публикации, комментарии, дедлайны, охваты, реакция аудитории, репутационные риски"
            "дизайн" in normalized || "дизайнер" in normalized || "design" in normalized -> "работа дизайнера: макеты, визуальные материалы, правки заказчика, брендбук, дедлайны, согласование дизайна"
            "маркет" in normalized || "marketing" in normalized -> "работа маркетолога: рекламные кампании, аналитика, целевая аудитория, гипотезы, бюджет, эффективность каналов"
            "безопас" in normalized || "safety" in normalized -> "техника безопасности: соблюдение инструкций, СИЗ, опасные ситуации, нарушения регламента, действия при риске"
            else -> "рабочая ситуация, соответствующая названию категории теста"
        }
    }

    private fun scaleDescription(scale: String): String = when (scale) {
        "attention" -> "внимательность, концентрация, аккуратность при работе с деталями"
        "stress_resistance" -> "устойчивость к стрессу, сохранение качества работы под давлением"
        "responsibility" -> "ответственность, соблюдение обязательств, признание и исправление ошибок"
        "adaptability" -> "адаптивность, способность быстро перестраиваться при изменении условий"
        "decision_speed_accuracy" -> "скорость и точность принятия решений при ограниченном времени"
        else -> "профессионально значимое поведение кандидата"
    }

    private fun difficultyDescription(difficulty: Int): String = when {
        difficulty <= 1 -> "простая ситуация с очевидным правильным поведением"
        difficulty == 2 -> "ситуация средней сложности с несколькими рабочими ограничениями"
        else -> "сложная ситуация с конфликтом приоритетов, рисками и ограниченным временем"
    }

    private fun fallbackQuestion(
        categoryName: String,
        targetScale: String,
        difficulty: Int,
        options: List<String>,
    ): GeneratedAdaptiveQuestion {
        val context = when {
            categoryName.contains("SMM", ignoreCase = true) -> "в работе с контент-планом, публикациями и реакцией аудитории"
            categoryName.contains("дизайн", ignoreCase = true) || categoryName.contains("дизайнер", ignoreCase = true) -> "при подготовке дизайн-макета и согласовании правок"
            categoryName.contains("маркет", ignoreCase = true) -> "при работе над маркетинговой кампанией и анализом результатов"
            categoryName.contains("безопас", ignoreCase = true) -> "в ситуации, связанной с соблюдением техники безопасности"
            else -> "в рабочей ситуации по выбранной категории теста"
        }
        val scale = scaleDescription(targetScale)
        val difficultyText = difficultyDescription(difficulty)
        return GeneratedAdaptiveQuestion(
            text = "Как вы поступите $context, если возникает $difficultyText и требуется проявить $scale?",
            options = options,
        )
    }

    private fun cleanJson(raw: String): String = raw
        .trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()
}

data class GeneratedAdaptiveQuestion(
    val text: String,
    val options: List<String>,
)
