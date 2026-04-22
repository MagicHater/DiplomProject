package com.example.adaptivetestingbackend.service.ai.prompt

import com.example.adaptivetestingbackend.dto.ai.GenerateCustomTestDraftRequest
import org.springframework.stereotype.Component

@Component
class CustomTestDraftPromptBuilder {

    fun build(request: GenerateCustomTestDraftRequest): String {
        val audienceLine = request.audience?.let { "Audience: $it" } ?: "Audience: not specified"
        val questionsLine = request.desiredQuestionCount?.let { "Desired question count: $it" }
            ?: "Desired question count: choose optimal amount"
        val languageLine = request.language?.let { "Language: $it" } ?: "Language: same as prompt"

        return """
            You are an assistant for test creation.
            Generate a structured JSON response with fields: title, description, questions[].
            Each question must contain text and options[] (minimum 2 options).
            Do not include markdown.

            User intent:
            ${request.prompt.trim()}

            Constraints:
            - $audienceLine
            - $questionsLine
            - $languageLine
        """.trimIndent()
    }
}
