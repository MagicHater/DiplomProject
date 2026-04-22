package com.example.adaptivetestingbackend.config

import com.example.adaptivetestingbackend.ai.client.AiTextGenerationClient
import com.example.adaptivetestingbackend.service.ai.client.StubAiTextGenerationClient
import com.example.adaptivetestingbackend.service.ai.client.YandexGptSkeletonClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiClientConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "ai.yandex", name = ["enabled"], havingValue = "true")
    fun yandexGptClient(properties: AiYandexProperties): AiTextGenerationClient = YandexGptSkeletonClient(properties)

    @Bean
    @ConditionalOnProperty(prefix = "ai.yandex", name = ["enabled"], havingValue = "false", matchIfMissing = true)
    fun stubAiClient(): AiTextGenerationClient = StubAiTextGenerationClient()
}
