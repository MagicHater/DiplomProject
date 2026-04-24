package com.example.adaptivetestingbackend.config

import com.example.adaptivetestingbackend.ai.client.AiTextGenerationClient
import com.example.adaptivetestingbackend.service.ai.client.StubAiTextGenerationClient
import com.example.adaptivetestingbackend.service.ai.client.YandexGptSkeletonClient
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiClientConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "ai.yandex", name = ["enabled"], havingValue = "true")
    fun yandexGptClient(
        properties: AiYandexProperties,
        objectMapper: ObjectMapper,
    ): AiTextGenerationClient {
        val hasAuth = !properties.apiKey.isNullOrBlank() || !properties.iamToken.isNullOrBlank()
        val hasFolder = !properties.folderId.isNullOrBlank() || !properties.modelUri.contains("<folder-id>")

        require(hasAuth) {
            "AI_YANDEX_ENABLED=true, but AI_YANDEX_API_KEY or AI_YANDEX_IAM_TOKEN is not set"
        }

        require(hasFolder) {
            "AI_YANDEX_ENABLED=true, but AI_YANDEX_FOLDER_ID or full AI_YANDEX_MODEL_URI is not set"
        }

        return YandexGptSkeletonClient(properties, objectMapper)
    }

    @Bean
    @ConditionalOnProperty(prefix = "ai.yandex", name = ["enabled"], havingValue = "false", matchIfMissing = true)
    fun stubAiClient(): AiTextGenerationClient = StubAiTextGenerationClient()
}
