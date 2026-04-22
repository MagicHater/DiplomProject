package com.example.adaptivetestingbackend.service.ai

import com.example.adaptivetestingbackend.ai.client.AiGenerateTextRequest
import com.example.adaptivetestingbackend.ai.client.AiTextGenerationClient
import com.example.adaptivetestingbackend.ai.exception.AiProviderUnavailableException
import com.example.adaptivetestingbackend.ai.exception.AiResponseParsingException
import com.example.adaptivetestingbackend.dto.ai.InterpretCustomTestResultRequest
import com.example.adaptivetestingbackend.dto.ai.InterpretCustomTestResultResponse
import com.example.adaptivetestingbackend.service.ai.prompt.CustomTestInterpretationPromptBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AiCustomTestInterpretationService(
    private val aiClient: AiTextGenerationClient,
    private val promptBuilder: CustomTestInterpretationPromptBuilder,
    private val objectMapper: ObjectMapper,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun interpret(request: InterpretCustomTestResultRequest): InterpretCustomTestResultResponse {
        val correlationId = UUID.randomUUID().toString()
        val prompt = promptBuilder.build(request)

        return try {
            val aiResponse = aiClient.generateText(
                AiGenerateTextRequest(
                    operation = "custom-test-interpretation",
                    prompt = prompt,
                    correlationId = correlationId,
                ),
            )
            val payload = objectMapper.readTree(aiResponse.content)

            val response = InterpretCustomTestResultResponse(
                summary = payload.path("summary").asText("No summary provided"),
                observations = payload.path("observations").map { it.asText() },
                recommendations = payload.path("recommendations").map { it.asText() },
                disclaimer = payload.path("disclaimer").asText("This is an automated interpretation."),
                providerMode = aiClient.providerMode(),
                requestId = aiResponse.requestId,
            )

            logger.info(
                "AI operation=custom-test-interpretation correlationId={} providerMode={} result=success",
                correlationId,
                aiClient.providerMode(),
            )
            response
        } catch (ex: AiResponseParsingException) {
            throw ex
        } catch (ex: Exception) {
            logger.warn(
                "AI operation=custom-test-interpretation correlationId={} providerMode={} result=failure reason={}",
                correlationId,
                aiClient.providerMode(),
                ex.javaClass.simpleName,
            )
            when (ex) {
                is com.fasterxml.jackson.core.JsonProcessingException -> throw AiResponseParsingException(
                    "Failed to parse AI response for interpretation",
                    ex,
                )

                else -> throw AiProviderUnavailableException("AI interpretation failed", ex)
            }
        }
    }
}
