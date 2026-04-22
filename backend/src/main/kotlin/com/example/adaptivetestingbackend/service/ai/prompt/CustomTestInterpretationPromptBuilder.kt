package com.example.adaptivetestingbackend.service.ai.prompt

import com.example.adaptivetestingbackend.dto.ai.InterpretCustomTestResultRequest
import org.springframework.stereotype.Component

@Component
class CustomTestInterpretationPromptBuilder {

    fun build(request: InterpretCustomTestResultRequest): String {
        val answersBlock = request.answers.joinToString(separator = "\n") { answer ->
            "- Q: ${answer.question.trim()} | A: ${answer.answer.trim()}"
        }

        val customPrompt = request.analysisPrompt?.trim()?.takeIf { it.isNotBlank() }
            ?: "Focus on neutral, non-diagnostic observations."

        return """
            You analyze custom test responses.
            Return strict JSON with fields: summary, observations[], recommendations[], disclaimer.
            Keep tone professional and safe.

            Test title: ${request.testTitle.trim()}
            Test description: ${request.testDescription?.trim() ?: "not provided"}
            Analysis hint: $customPrompt

            Answers:
            $answersBlock
        """.trimIndent()
    }
}
