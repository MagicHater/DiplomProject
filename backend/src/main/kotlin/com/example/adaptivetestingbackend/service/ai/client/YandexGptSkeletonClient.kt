package com.example.adaptivetestingbackend.service.ai.client

import com.example.adaptivetestingbackend.ai.client.AiGenerateTextRequest
import com.example.adaptivetestingbackend.ai.client.AiGenerateTextResponse
import com.example.adaptivetestingbackend.ai.client.AiTextGenerationClient
import com.example.adaptivetestingbackend.ai.exception.AiClientNotConfiguredException
import com.example.adaptivetestingbackend.ai.exception.AiProviderUnavailableException
import com.example.adaptivetestingbackend.config.AiYandexProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID

class YandexGptSkeletonClient(
    private val properties: AiYandexProperties,
    private val objectMapper: ObjectMapper,
) : AiTextGenerationClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(properties.timeoutMs))
        .build()

    override fun generateText(request: AiGenerateTextRequest): AiGenerateTextResponse {
        ensureAuthenticationConfigured()

        val endpoint = properties.baseUrl.trim().trimEnd('/') + "/foundationModels/v1/completion"
        val modelUri = resolveModelUri()

        val body = mapOf(
            "modelUri" to modelUri,
            "completionOptions" to mapOf(
                "stream" to false,
                "temperature" to 0.4,
                "maxTokens" to "2500",
            ),
            "messages" to listOf(
                mapOf(
                    "role" to "system",
                    "text" to """
                        Ты — модуль генерации пользовательских психологических и поведенческих тестов.

                        Твоя задача — вернуть только валидный JSON.
                        Нельзя возвращать markdown, пояснения, комментарии, текст до JSON или текст после JSON.
                        Нельзя использовать блоки ```json или ```.
                        Нельзя использовать шаблонные значения:
                        "Название теста", "Описание теста", "Текст вопроса", "Вариант 1", "Вариант 2", "string".

                        Формат ответа:
                        {
                          "title": строка,
                          "description": строка,
                          "questions": [
                            {
                              "text": строка,
                              "options": [строка, строка, строка]
                            }
                          ]
                        }

                        Требования:
                        - title должен быть конкретным названием теста;
                        - description должен кратко объяснять, что оценивает тест;
                        - questions должен содержать реальные ситуационные вопросы;
                        - каждый вопрос должен описывать конкретную рабочую или поведенческую ситуацию;
                        - options должны быть осмысленными вариантами поведения кандидата;
                        - каждый вопрос должен иметь 3 или 4 варианта ответа;
                        - если пользователь просит русский язык, весь контент должен быть на русском языке;
                        - JSON должен начинаться с символа { и заканчиваться символом }.
                    """.trimIndent(),
                ),
                mapOf(
                    "role" to "user",
                    "text" to request.prompt,
                ),
            ),
        )

        val requestJson = objectMapper.writeValueAsString(body)

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .timeout(Duration.ofMillis(properties.timeoutMs))
            .header("Content-Type", "application/json")
            .header("Authorization", buildAuthHeader())
            .apply {
                if (properties.disableLogging) {
                    header("x-data-logging-enabled", "false")
                }
            }
            .POST(HttpRequest.BodyPublishers.ofString(requestJson))
            .build()

        return try {
            val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() !in 200..299) {
                throw AiProviderUnavailableException(
                    "YandexGPT returned status ${response.statusCode()}: ${response.body()}",
                )
            }

            val json = objectMapper.readTree(response.body())
            val content = json
                .path("result")
                .path("alternatives")
                .firstOrNull()
                ?.path("message")
                ?.path("text")
                ?.asText()
                ?.trim()
                ?: "{}"

            AiGenerateTextResponse(
                content = content,
                provider = providerMode(),
                modelUri = modelUri,
                requestId = response.headers().firstValue("x-request-id")
                    .orElse("yandex-${UUID.randomUUID()}"),
                stub = false,
            )
        } catch (ex: AiProviderUnavailableException) {
            throw ex
        } catch (ex: Exception) {
            logger.warn(
                "YandexGPT call failed operation={} endpoint={} modelUri={}",
                request.operation,
                endpoint,
                modelUri,
                ex,
            )
            throw AiProviderUnavailableException("YandexGPT request failed", ex)
        }
    }

    override fun providerMode(): String = "yandex"

    private fun resolveModelUri(): String {
        val configured = properties.modelUri.trim()

        return if (configured.contains("<folder-id>")) {
            val folder = normalizedFolderId()
            "gpt://$folder/yandexgpt-lite/latest"
        } else {
            configured
        }
    }

    private fun normalizedFolderId(): String {
        val folder = properties.folderId?.trim().orEmpty()

        if (folder.isBlank()) {
            throw AiClientNotConfiguredException("folderId is not configured")
        }

        return folder
    }

    private fun buildAuthHeader(): String {
        val apiKey = properties.apiKey?.trim().orEmpty()
        val iamToken = properties.iamToken?.trim().orEmpty()

        return when {
            apiKey.isNotBlank() -> "Api-Key $apiKey"
            iamToken.isNotBlank() -> "Bearer $iamToken"
            else -> throw AiClientNotConfiguredException("No auth configured")
        }
    }

    private fun ensureAuthenticationConfigured() {
        val apiKey = properties.apiKey?.trim().orEmpty()
        val iamToken = properties.iamToken?.trim().orEmpty()

        if (apiKey.isBlank() && iamToken.isBlank()) {
            throw AiClientNotConfiguredException("AI provider is enabled, but no credentials provided")
        }
    }
}