package com.example.adaptivetestingbackend.service.testcontent

/**
 * Extension point for future LLM-driven generation (YandexGPT integration).
 * Currently backed by stub implementation.
 */
interface TestContentGenerator {
    fun generate(request: QuestionGenerationRequest): List<GeneratedQuestionDraft>
}

data class QuestionGenerationRequest(
    val categoryCode: String,
    val amount: Int,
)

data class GeneratedQuestionDraft(
    val text: String,
    val difficulty: Short,
    val options: List<String>,
)
