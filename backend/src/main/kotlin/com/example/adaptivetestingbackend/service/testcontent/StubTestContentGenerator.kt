package com.example.adaptivetestingbackend.service.testcontent

import org.springframework.stereotype.Component

/**
 * Extension point stub for future YandexGPT integration.
 * For now returns an empty list intentionally.
 */
@Component
class StubTestContentGenerator : TestContentGenerator {
    override fun generate(request: QuestionGenerationRequest): List<GeneratedQuestionDraft> = emptyList()
}
