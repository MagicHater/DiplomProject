package com.example.adaptivetestingbackend.controller.error

import com.example.adaptivetestingbackend.ai.exception.AiClientNotConfiguredException
import com.example.adaptivetestingbackend.ai.exception.AiDisabledException
import com.example.adaptivetestingbackend.ai.exception.AiProviderUnavailableException
import com.example.adaptivetestingbackend.ai.exception.AiResponseParsingException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ApiErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
)

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        val message = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            ?: "Validation failed"
        return buildResponse(HttpStatus.BAD_REQUEST, message, request)
    }

    @ExceptionHandler(AiDisabledException::class)
    fun handleAiDisabled(ex: AiDisabledException, request: HttpServletRequest): ResponseEntity<ApiErrorResponse> =
        buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.message ?: "AI module is disabled", request)

    @ExceptionHandler(AiClientNotConfiguredException::class)
    fun handleAiNotConfigured(
        ex: AiClientNotConfiguredException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(HttpStatus.PRECONDITION_FAILED, ex.message ?: "AI client is not configured", request)

    @ExceptionHandler(AiProviderUnavailableException::class)
    fun handleProviderUnavailable(
        ex: AiProviderUnavailableException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(HttpStatus.BAD_GATEWAY, ex.message ?: "AI provider unavailable", request)

    @ExceptionHandler(AiResponseParsingException::class)
    fun handleResponseParsing(
        ex: AiResponseParsingException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.message ?: "AI response parsing failed", request)

    private fun buildResponse(
        status: HttpStatus,
        message: String,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(status).body(
            ApiErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = message,
                path = request.requestURI,
            ),
        )
    }
}
