package com.example.adaptivetestingbackend.controller.token

import com.example.adaptivetestingbackend.dto.testsession.TestCategoryResponse
import com.example.adaptivetestingbackend.dto.token.ControllerTokenListItemResponse
import com.example.adaptivetestingbackend.dto.token.CreateControllerTokenRequest
import com.example.adaptivetestingbackend.dto.token.CreateControllerTokenResponse
import com.example.adaptivetestingbackend.service.token.ControllerTokenService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/controller/test-management")
class ControllerTestManagementController(
    private val controllerTokenService: ControllerTokenService,
) {
    @GetMapping("/categories")
    fun categories(): List<TestCategoryResponse> = controllerTokenService.getActiveCategories()

    @PostMapping("/tokens")
    fun createToken(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: CreateControllerTokenRequest,
    ): CreateControllerTokenResponse =
        controllerTokenService.createToken(userDetails.username, request.categoryId!!)

    @GetMapping("/tokens")
    fun myTokens(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): List<ControllerTokenListItemResponse> =
        controllerTokenService.getControllerTokenHistory(userDetails.username)
}
