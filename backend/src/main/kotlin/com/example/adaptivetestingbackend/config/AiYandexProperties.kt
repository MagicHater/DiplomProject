package com.example.adaptivetestingbackend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ai.yandex")
data class AiYandexProperties(
    val enabled: Boolean = false,
    val baseUrl: String = "https://llm.api.cloud.yandex.net",
    val apiKey: String? = null,
    val iamToken: String? = null,
    val folderId: String? = null,
    val modelUri: String = "gpt://<folder-id>/yandexgpt-lite/latest",
    val disableLogging: Boolean = true,
    val timeoutMs: Long = 5_000,
)
