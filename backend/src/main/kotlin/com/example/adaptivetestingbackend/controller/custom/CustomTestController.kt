package com.example.adaptivetestingbackend.controller.custom

import com.example.adaptivetestingbackend.dto.custom.CreateCustomTestRequest
import com.example.adaptivetestingbackend.dto.custom.CreateCustomTestResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestDetailsResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestListItemResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestResultItemResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestStatisticsResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestSubmissionRequest
import com.example.adaptivetestingbackend.dto.custom.CustomTestSubmissionResponse
import com.example.adaptivetestingbackend.service.custom.CustomTestService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/custom-tests")
class CustomTestController(
    private val customTestService: CustomTestService,
) {
    @PostMapping
    fun createCustomTest(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: CreateCustomTestRequest,
    ): CreateCustomTestResponse = customTestService.createTest(userDetails.username, request)

    @GetMapping("/my")
    fun myCustomTests(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): List<CustomTestListItemResponse> = customTestService.getControllerTests(userDetails.username)

    @GetMapping("/{testId}")
    fun customTestDetails(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable testId: UUID,
    ): CustomTestDetailsResponse = customTestService.getControllerTestDetails(userDetails.username, testId)

    @GetMapping("/available")
    fun availableCustomTests(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): List<CustomTestListItemResponse> = customTestService.getAvailableTestsForUser(userDetails.username)

    @PostMapping("/{testId}/submissions")
    fun submitCustomTest(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable testId: UUID,
        @RequestBody request: CustomTestSubmissionRequest,
    ): CustomTestSubmissionResponse = customTestService.submitTest(userDetails.username, testId, request)

    @GetMapping("/{testId}/results")
    fun customTestResults(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable testId: UUID,
    ): List<CustomTestResultItemResponse> = customTestService.getControllerResults(userDetails.username, testId)

    @GetMapping("/{testId}/statistics")
    fun customTestStatistics(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable testId: UUID,
    ): CustomTestStatisticsResponse = customTestService.getControllerStatistics(userDetails.username, testId)
}
