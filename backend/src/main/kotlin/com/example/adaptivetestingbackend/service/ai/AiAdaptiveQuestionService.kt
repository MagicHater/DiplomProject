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
                    "AI adaptive question fallback correlationId={} providerMode={} reason=invalid_payload",
                    correlationId,
                    aiClient.providerMode(),
                )
                GeneratedAdaptiveQuestion(sourceQuestionText, options)
            } else {
                logger.info(
                    "AI adaptive question generated correlationId={} providerMode={} result=success",
                    correlationId,
                    aiClient.providerMode(),
                )
                GeneratedAdaptiveQuestion(
                    text = generatedText,
                    options = generatedOptions.take(options.size),
                )
            }
        }.getOrElse { error ->
            logger.warn(
                "AI adaptive question fallback correlationId={} providerMode={} reason={}",
                correlationId,
                aiClient.providerMode(),
                error.javaClass.simpleName,
            )
            GeneratedAdaptiveQuestion(sourceQuestionText, options)
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

        return """
            Ты генерируешь вопрос для адаптивного психологического тестирования кандидата.

            Нужно переформулировать базовый вопрос и варианты ответов так, чтобы они выглядели как реалистичная рабочая ситуация.
            При этом смысл вариантов ответа и их порядок должны сохраниться, потому что за каждым вариантом уже закреплены веса оценки.

            Категория теста: $categoryName
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
            - вопрос должен быть конкретной ситуацией из работы оператора автоматизированной системы;
            - варианты должны быть осмысленными моделями поведения кандидата;
            - количество вариантов должно быть ровно ${options.size};
            - порядок вариантов должен соответствовать исходному порядку;
            - не используй шаблонные значения: "Текст вопроса", "Вариант 1", "Вариант 2", "string".

            Формат ответа:
            {
              "text": "конкретный ситуационный вопрос",
              "options": ["вариант поведения 1", "вариант поведения 2"]
            }
        """.trimIndent()
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
