package com.example.adaptivetestingbackend.controller.testsession

import com.example.adaptivetestingbackend.dto.testsession.TestCategoryResponse
import com.example.adaptivetestingbackend.service.token.ControllerTokenService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test-categories")
class TestCategoryController(
    private val controllerTokenService: ControllerTokenService,
) {
    @GetMapping
    fun getActiveCategories(): List<TestCategoryResponse> = controllerTokenService.getActiveCategories()
}
