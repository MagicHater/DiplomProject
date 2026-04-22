package com.example.adaptivetestingbackend.service.ai.client

import com.example.adaptivetestingbackend.ai.client.AiGenerateTextRequest
import com.example.adaptivetestingbackend.ai.client.AiGenerateTextResponse
import com.example.adaptivetestingbackend.ai.client.AiTextGenerationClient
import com.example.adaptivetestingbackend.ai.exception.AiClientNotConfiguredException
import com.example.adaptivetestingbackend.config.AiYandexProperties
import org.slf4j.LoggerFactory
import java.util.UUID

class YandexGptSkeletonClient(
    private val properties: AiYandexProperties,
) : AiTextGenerationClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun generateText(request: AiGenerateTextRequest): AiGenerateTextResponse {
        ensureAuthenticationConfigured()

        val headers = buildPlannedHeaders()
        // TODO: Replace with real HTTP call to YandexGPT Completion API.
        // TODO: Use `headers` when wiring RestClient/WebClient implementation.
        logger.info(
            "AI operation={} correlationId={} providerMode={} endpoint={} timeoutMs={} result=skeleton-response",
            request.operation,
            request.correlationId,
            providerMode(),
            properties.baseUrl,
            properties.timeoutMs,
        )
        logger.debug("AI provider={} plannedHeaders={}", providerMode(), headers.keys)

        return AiGenerateTextResponse(
            content = "{\"message\":\"Yandex skeleton client is enabled, but real HTTP integration is not implemented yet.\"}",
            provider = providerMode(),
            modelUri = properties.modelUri,
            requestId = "yandex-skeleton-${UUID.randomUUID()}",
            stub = true,
        )
    }

    override fun providerMode(): String = "yandex-skeleton"

    private fun buildPlannedHeaders(): Map<String, String> {
        val authHeader = when {
            !properties.apiKey.isNullOrBlank() -> "Api-Key ***"
            !properties.iamToken.isNullOrBlank() -> "Bearer ***"
            else -> ""
        }

        val headers = mutableMapOf("Authorization" to authHeader)
        if (properties.disableLogging) {
            headers["x-data-logging-enabled"] = "false"
        }
        properties.folderId?.takeIf { it.isNotBlank() }?.let { headers["x-folder-id"] = it }
        return headers
    }

    private fun ensureAuthenticationConfigured() {
        val hasApiKey = !properties.apiKey.isNullOrBlank()
        val hasIamToken = !properties.iamToken.isNullOrBlank()
        if (!hasApiKey && !hasIamToken) {
            throw AiClientNotConfiguredException(
                "AI provider is enabled, but neither apiKey nor iamToken is configured",
            )
        }
    }
}
