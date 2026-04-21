package com.example.adaptivetestingbackend.controller.token

import com.example.adaptivetestingbackend.dto.controller.CreateControllerTestRequest
import com.example.adaptivetestingbackend.dto.controller.CreateControllerTestResponse
import com.example.adaptivetestingbackend.dto.testsession.TestCategoryResponse
import com.example.adaptivetestingbackend.dto.testsession.ResultProfileResponse
import com.example.adaptivetestingbackend.dto.token.ControllerTokenListItemResponse
import com.example.adaptivetestingbackend.dto.token.ControllerTokenResultListItemResponse
import com.example.adaptivetestingbackend.dto.token.CreateControllerTokenResponse
import com.example.adaptivetestingbackend.security.JwtService
import com.example.adaptivetestingbackend.service.token.ControllerTestCreationService
import com.example.adaptivetestingbackend.service.token.ControllerTokenService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/token-management")
class ControllerTestManagementController(
    private val controllerTokenService: ControllerTokenService,
    private val controllerTestCreationService: ControllerTestCreationService,
    private val jwtService: JwtService,
) {
    @GetMapping("/categories")
    fun categories(): List<TestCategoryResponse> =
        controllerTokenService.getActiveCategories()


    @PostMapping("/tests")
    fun createTest(
        @RequestHeader(name = "Authorization", required = false) authorization: String?,
        @Valid @RequestBody request: CreateControllerTestRequest,
    ): CreateControllerTestResponse {
        val email = extractEmailFromAuthorization(authorization)
        return controllerTestCreationService.createTest(email, request)
    }

    @PostMapping("/tokens")
    fun createToken(
        @RequestHeader(name = "Authorization", required = false) authorization: String?,
        @RequestBody body: Map<String, String?>,
    ): CreateControllerTokenResponse {
        val email = extractEmailFromAuthorization(authorization)

        val categoryIdRaw = body["categoryId"]?.trim()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId is required")

        val categoryId = runCatching { UUID.fromString(categoryIdRaw) }
            .getOrElse {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId must be a valid UUID")
            }

        return controllerTokenService.createToken(email, categoryId)
    }

    @GetMapping("/tokens")
    fun myTokens(
        @RequestHeader(name = "Authorization", required = false) authorization: String?,
    ): List<ControllerTokenListItemResponse> {
        val email = extractEmailFromAuthorization(authorization)
        return controllerTokenService.getControllerTokenHistory(email)
    }



    @GetMapping("/results")
    fun myTokenResults(
        @RequestHeader(name = "Authorization", required = false) authorization: String?,
    ): List<ControllerTokenResultListItemResponse> {
        val email = extractEmailFromAuthorization(authorization)
        return controllerTokenService.getControllerTokenResults(email)
    }

    @GetMapping("/results/{sessionId}")
    fun myTokenResultBySessionId(
        @PathVariable sessionId: UUID,
        @RequestHeader(name = "Authorization", required = false) authorization: String?,
    ): ResultProfileResponse {
        val email = extractEmailFromAuthorization(authorization)
        return controllerTokenService.getControllerTokenResultBySessionId(email, sessionId)
    }

    private fun extractEmailFromAuthorization(authorization: String?): String {
        if (authorization.isNullOrBlank() || !authorization.startsWith("Bearer ")) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token")
        }

        val token = authorization.removePrefix("Bearer ").trim()
        if (token.isBlank()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token")
        }

        return runCatching { jwtService.parseClaims(token).subject.trim().lowercase() }
            .getOrElse {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid bearer token")
            }
    }
}