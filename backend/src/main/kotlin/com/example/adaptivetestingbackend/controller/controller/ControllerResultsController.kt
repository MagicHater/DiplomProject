package com.example.adaptivetestingbackend.controller.controller

import com.example.adaptivetestingbackend.dto.controller.ControllerCandidateListItemResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerCandidateResultsResponse
import com.example.adaptivetestingbackend.dto.testsession.ResultProfileResponse
import com.example.adaptivetestingbackend.service.testsession.ControllerResultsService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/controller")
class ControllerResultsController(
    private val controllerResultsService: ControllerResultsService,
) {
    @GetMapping("/candidates")
    fun getCandidates(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): List<ControllerCandidateListItemResponse> {
        return controllerResultsService.getCandidates(userDetails.username)
    }

    @GetMapping("/candidates/{candidateId}/results")
    fun getCandidateResults(
        @PathVariable candidateId: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ControllerCandidateResultsResponse {
        return controllerResultsService.getCandidateResults(candidateId, userDetails.username)
    }

    @GetMapping("/results/{sessionId}")
    fun getResult(
        @PathVariable sessionId: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResultProfileResponse {
        return controllerResultsService.getResultDetails(sessionId, userDetails.username)
    }
}
