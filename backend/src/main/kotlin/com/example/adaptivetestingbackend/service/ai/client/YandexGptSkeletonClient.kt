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

        val endpoint = properties.baseUrl.trimEnd('/') + "/foundationModels/v1/completion"

        val body = mapOf(
            "modelUri" to resolveModelUri(),
            "completionOptions" to mapOf(
                "stream" to false,
                "temperature" to 0.3
            ),
            "messages" to listOf(
                mapOf("role" to "system", "text" to "Return only valid JSON"),
                mapOf("role" to "user", "text" to request.prompt)
            ),
            "jsonObject" to true
        )

        val requestJson = objectMapper.writeValueAsString(body)

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .timeout(Duration.ofMillis(properties.timeoutMs))
            .header("Content-Type", "application/json")
            .header("Authorization", buildAuthHeader())
            .POST(HttpRequest.BodyPublishers.ofString(requestJson))
            .build()

        return try {
            val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() !in 200..299) {
                throw AiProviderUnavailableException("YandexGPT returned status ${response.statusCode()}: ${response.body()}")
            }

            val json = objectMapper.readTree(response.body())
            val content = json
                .path("result")
                .path("alternatives")
                .firstOrNull()
                ?.path("message")
                ?.path("text")
                ?.asText()
                ?: "{}"

            AiGenerateTextResponse(
                content = content,
                provider = providerMode(),
                modelUri = resolveModelUri(),
                requestId = "yandex-${UUID.randomUUID()}",
                stub = false,
            )
        } catch (ex: Exception) {
            logger.warn("YandexGPT call failed", ex)
            throw AiProviderUnavailableException("YandexGPT request failed", ex)
        }
    }

    override fun providerMode(): String = "yandex"

    private fun resolveModelUri(): String {
        return if (properties.modelUri.contains("<folder-id>")) {
            val folder = properties.folderId ?: throw AiClientNotConfiguredException("folderId is not configured")
            "gpt://$folder/yandexgpt-lite/latest"
        } else properties.modelUri
    }

    private fun buildAuthHeader(): String {
        return when {
            !properties.apiKey.isNullOrBlank() -> "Api-Key ${properties.apiKey}"
            !properties.iamToken.isNullOrBlank() -> "Bearer ${properties.iamToken}"
            else -> throw AiClientNotConfiguredException("No auth configured")
        }
    }

    private fun ensureAuthenticationConfigured() {
        if (properties.apiKey.isNullOrBlank() && properties.iamToken.isNullOrBlank()) {
            throw AiClientNotConfiguredException("AI provider is enabled, but no credentials provided")
        }
    }
}
