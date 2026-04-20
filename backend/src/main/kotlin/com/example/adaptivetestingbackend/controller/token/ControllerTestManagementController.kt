package com.example.adaptivetestingbackend.controller.token

import com.example.adaptivetestingbackend.dto.testsession.TestCategoryResponse
import com.example.adaptivetestingbackend.dto.token.ControllerTokenListItemResponse
import com.example.adaptivetestingbackend.dto.token.CreateControllerTokenRequest
import com.example.adaptivetestingbackend.dto.token.CreateControllerTokenResponse
import com.example.adaptivetestingbackend.security.JwtService
import com.example.adaptivetestingbackend.service.token.ControllerTokenService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/controller/test-management")
class ControllerTestManagementController(
    private val controllerTokenService: ControllerTokenService,
    private val jwtService: JwtService,
) {
    @GetMapping("/categories")
    fun categories(): List<TestCategoryResponse> = controllerTokenService.getActiveCategories()

    @PostMapping("/tokens")
    fun createToken(
        @RequestHeader(name = "Authorization", required = false) authorization: String?,
        @RequestHeader(name = "X-App-Jwt", required = false) appJwt: String?,
        @Valid @RequestBody request: CreateControllerTokenRequest,
    ): CreateControllerTokenResponse =
        controllerTokenService.createToken(extractEmail(authorization, appJwt), request.categoryId!!)

    @GetMapping("/tokens")
    fun myTokens(
        @RequestHeader(name = "Authorization", required = false) authorization: String?,
        @RequestHeader(name = "X-App-Jwt", required = false) appJwt: String?,
    ): List<ControllerTokenListItemResponse> =
        controllerTokenService.getControllerTokenHistory(extractEmail(authorization, appJwt))

    private fun extractEmail(authorization: String?, appJwt: String?): String {
        val token = when {
            !appJwt.isNullOrBlank() -> appJwt.trim()
            !authorization.isNullOrBlank() && authorization.startsWith("Bearer ") -> authorization.removePrefix("Bearer ").trim()
            else -> throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token")
        }

        return runCatching { jwtService.parseClaims(token).subject }
            .getOrElse {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
            }
    }
}
