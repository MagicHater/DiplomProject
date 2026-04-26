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
        sourceQuestionText: String,
        difficulty: Int,
        options: List<String>,
    ): GeneratedAdaptiveQuestion {
        if (sourceQuestionText.isBlank() || options.isEmpty()) {
            return GeneratedAdaptiveQuestion(sourceQuestionText, options)
        }

        val correlationId = UUID.randomUUID().toString()
        val prompt = buildPrompt(
            categoryName = categoryName,
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
                    "AI adaptive question fallback correlationId={} providerMode={} reason=invalid_payload category={}",
                    correlationId,
                    aiClient.providerMode(),
                    categoryName,
                )
                fallbackQuestion(categoryName, sourceQuestionText, options)
            } else {
                logger.info(
                    "AI adaptive question generated correlationId={} providerMode={} result=success category={}",
                    correlationId,
                    aiClient.providerMode(),
                    categoryName,
                )
                GeneratedAdaptiveQuestion(
                    text = generatedText,
                    options = generatedOptions.take(options.size),
                )
            }
        }.getOrElse { error ->
            logger.warn(
                "AI adaptive question fallback correlationId={} providerMode={} reason={} category={}",
                correlationId,
                aiClient.providerMode(),
                error.javaClass.simpleName,
                categoryName,
            )
            fallbackQuestion(categoryName, sourceQuestionText, options)
        }
    }

    private fun buildPrompt(
        categoryName: String,
        sourceQuestionText: String,
        difficulty: Int,
        options: List<String>,
    ): String {
        val optionsText = options.mapIndexed { index, option ->
            "${index + 1}. $option"
        }.joinToString("\n")
        val context = categoryContext(categoryName)

        return """
            Ты генерируешь вопрос для адаптивного психологического и поведенческого тестирования кандидата.

            Нужно переформулировать базовый вопрос и варианты ответов так, чтобы они выглядели как реалистичная рабочая ситуация строго по теме выбранной категории.
            При этом психологический смысл вариантов ответа и их порядок должны сохраниться, потому что за каждым вариантом уже закреплены веса оценки.

            Категория теста: $categoryName
            Тематический контекст категории: $context
            Сложность вопроса: $difficulty

            Базовый вопрос:
            $sourceQuestionText

            Базовые варианты ответов, порядок менять нельзя:
            $optionsText

            Требования:
            - верни только валидный JSON;
            - не используй markdown и блоки ```;
            - не добавляй пояснения до или после JSON;
            - текст должен быть на русском языке;
            - вопрос должен быть конкретной рабочей ситуацией именно из категории "$categoryName";
            - нельзя переносить ситуацию в другую профессию или предметную область;
            - если категория SMM, используй темы контент-плана, публикаций, комментариев, дедлайнов, охватов, репутационных рисков;
            - если категория Дизайнер, используй темы макетов, правок, брендбука, визуальных материалов, согласований;
            - если категория Маркетолог, используй темы кампаний, аналитики, сегментов аудитории, гипотез, бюджета;
            - если категория Техника безопасности, используй темы инструктажей, СИЗ, нарушений регламента, опасных ситуаций, производственной безопасности;
            - варианты должны быть осмысленными моделями поведения кандидата;
            - количество вариантов должно быть ровно ${options.size};
            - порядок вариантов должен соответствовать исходному порядку: от наименее эффективного поведения к наиболее эффективному;
            - не используй шаблонные значения: "Текст вопроса", "Вариант 1", "Вариант 2", "string".

            Формат ответа:
            {
              "text": "конкретный ситуационный вопрос по категории $categoryName",
              "options": ["вариант поведения 1", "вариант поведения 2"]
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

    private fun fallbackQuestion(
        categoryName: String,
        sourceQuestionText: String,
        options: List<String>,
    ): GeneratedAdaptiveQuestion {
        val prefix = when {
            categoryName.contains("SMM", ignoreCase = true) -> "В рамках работы с контент-планом и публикациями:"
            categoryName.contains("дизайн", ignoreCase = true) || categoryName.contains("дизайнер", ignoreCase = true) -> "В рамках подготовки дизайн-макета:"
            categoryName.contains("маркет", ignoreCase = true) -> "В рамках работы над маркетинговой кампанией:"
            categoryName.contains("безопас", ignoreCase = true) -> "В ситуации, связанной с техникой безопасности:"
            else -> "В рамках выбранной категории теста:"
        }
        return GeneratedAdaptiveQuestion(
            text = "$prefix $sourceQuestionText",
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
