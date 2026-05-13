package com.example.adaptivetestingbackend.controller.controller

import com.example.adaptivetestingbackend.dto.controller.ControllerDashboardResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerParticipantListItemResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerParticipantResultsResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerSessionAnswerResponse
import com.example.adaptivetestingbackend.dto.testsession.ResultProfileResponse
import com.example.adaptivetestingbackend.service.testsession.ControllerResultsService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/controller")
class ControllerResultsController(
    private val controllerResultsService: ControllerResultsService,
) {
    @GetMapping("/dashboard")
    fun getDashboard(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ControllerDashboardResponse {
        return controllerResultsService.getDashboard(userDetails.username)
    }

    @GetMapping("/candidates")
    fun getCandidates(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): List<ControllerParticipantListItemResponse> {
        return controllerResultsService.getCandidates(userDetails.username)
    }

    @GetMapping("/candidates/{candidateId}/results")
    fun getCandidateResults(
        @PathVariable candidateId: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ControllerParticipantResultsResponse {
        return controllerResultsService.getCandidateResults(candidateId, userDetails.username)
    }

    @GetMapping("/candidates/results")
    fun getParticipantResults(
        @RequestParam participantType: String,
        @RequestParam participantKey: String,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ControllerParticipantResultsResponse {
        return controllerResultsService.getCandidateResults(
            participantType = participantType,
            participantKey = participantKey,
            controllerEmail = userDetails.username,
        )
    }

    @GetMapping("/results/{sessionId}")
    fun getResult(
        @PathVariable sessionId: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResultProfileResponse {
        return controllerResultsService.getResultDetails(sessionId, userDetails.username)
    }

    @GetMapping("/results/{sessionId}/answers")
    fun getResultAnswers(
        @PathVariable sessionId: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): List<ControllerSessionAnswerResponse> {
        return controllerResultsService.getSessionAnswers(sessionId, userDetails.username)
    }
}
