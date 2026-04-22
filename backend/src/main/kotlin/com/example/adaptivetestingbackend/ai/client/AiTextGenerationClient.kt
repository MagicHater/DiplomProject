package com.example.adaptivetestingbackend.ai.client

data class AiGenerateTextRequest(
    val operation: String,
    val prompt: String,
    val correlationId: String,
)

data class AiGenerateTextResponse(
    val content: String,
    val provider: String,
    val modelUri: String? = null,
    val requestId: String,
    val stub: Boolean,
)

interface AiTextGenerationClient {
    fun generateText(request: AiGenerateTextRequest): AiGenerateTextResponse

    fun providerMode(): String
}
