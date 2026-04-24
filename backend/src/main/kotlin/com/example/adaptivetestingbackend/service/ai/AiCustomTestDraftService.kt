package com.example.adaptivetestingbackend.service.ai

import com.example.adaptivetestingbackend.ai.client.AiGenerateTextRequest
import com.example.adaptivetestingbackend.ai.client.AiTextGenerationClient
import com.example.adaptivetestingbackend.ai.exception.AiProviderUnavailableException
import com.example.adaptivetestingbackend.ai.exception.AiResponseParsingException
import com.example.adaptivetestingbackend.dto.ai.GenerateCustomTestDraftRequest
import com.example.adaptivetestingbackend.dto.ai.GenerateCustomTestDraftResponse
import com.example.adaptivetestingbackend.dto.ai.GeneratedCustomTestQuestionDto
import com.example.adaptivetestingbackend.service.ai.prompt.CustomTestDraftPromptBuilder
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AiCustomTestDraftService(
    private val aiClient: AiTextGenerationClient,
    private val promptBuilder: CustomTestDraftPromptBuilder,
    private val objectMapper: ObjectMapper,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun generateDraft(request: GenerateCustomTestDraftRequest): GenerateCustomTestDraftResponse {
        val correlationId = UUID.randomUUID().toString()
        val prompt = promptBuilder.build(request)

        return try {
            val aiResponse = aiClient.generateText(
                AiGenerateTextRequest(
                    operation = "custom-test-draft",
                    prompt = prompt,
                    correlationId = correlationId,
                ),
            )
            val payload = objectMapper.readTree(cleanJson(aiResponse.content))

            val response = GenerateCustomTestDraftResponse(
                title = payload.path("title").asText("Demo generated test"),
                description = payload.path("description").asText("Demo description"),
                questions = parseQuestions(payload),
                providerMode = aiClient.providerMode(),
                requestId = aiResponse.requestId,
            )

            logger.info(
                "AI operation=custom-test-draft correlationId={} providerMode={} result=success",
                correlationId,
                aiClient.providerMode(),
            )
            response
        } catch (ex: AiResponseParsingException) {
            throw ex
        } catch (ex: Exception) {
            logger.warn(
                "AI operation=custom-test-draft correlationId={} providerMode={} result=failure reason={}",
                correlationId,
                aiClient.providerMode(),
                ex.javaClass.simpleName,
            )
            when (ex) {
                is com.fasterxml.jackson.core.JsonProcessingException -> throw AiResponseParsingException(
                    "Failed to parse AI response for draft generation",
                    ex,
                )

                else -> throw AiProviderUnavailableException("AI draft generation failed", ex)
            }
        }
    }

    private fun parseQuestions(payload: JsonNode): List<GeneratedCustomTestQuestionDto> {
        return payload.path("questions").map { questionNode ->
            GeneratedCustomTestQuestionDto(
                text = questionNode.path("text").asText("Untitled question"),
                options = questionNode.path("options").map { option -> option.asText() }.ifEmpty {
                    listOf("Option A", "Option B")
                },
            )
        }.ifEmpty {
            listOf(
                GeneratedCustomTestQuestionDto(
                    text = "What is your preferred way of receiving feedback?",
                    options = listOf("Written notes", "Short call", "Live workshop"),
                ),
            )
        }
    }
}
private fun cleanJson(raw: String): String {
    return raw
        .trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()
}