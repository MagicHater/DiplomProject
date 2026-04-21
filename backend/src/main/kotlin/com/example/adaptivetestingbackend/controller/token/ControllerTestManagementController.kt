package com.example.adaptivetestingbackend.controller.token

import com.example.adaptivetestingbackend.dto.testsession.TestCategoryResponse
import com.example.adaptivetestingbackend.dto.token.ControllerTokenListItemResponse
import com.example.adaptivetestingbackend.dto.token.CreateControllerTokenResponse
import com.example.adaptivetestingbackend.security.JwtService
import com.example.adaptivetestingbackend.service.token.ControllerTokenService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
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
    private val jwtService: JwtService,
) {
    @GetMapping("/categories")
    fun categories(): List<TestCategoryResponse> =
        controllerTokenService.getActiveCategories()

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