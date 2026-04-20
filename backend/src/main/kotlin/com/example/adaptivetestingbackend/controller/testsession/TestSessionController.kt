package com.example.adaptivetestingbackend.controller.testsession

import com.example.adaptivetestingbackend.dto.testsession.CreateTestSessionRequest
import com.example.adaptivetestingbackend.dto.testsession.CreateTestSessionResponse
import com.example.adaptivetestingbackend.dto.testsession.NextQuestionResponse
import com.example.adaptivetestingbackend.dto.testsession.ResultProfileResponse
import com.example.adaptivetestingbackend.dto.testsession.SubmitAnswerRequest
import com.example.adaptivetestingbackend.dto.testsession.SubmitAnswerResponse
import com.example.adaptivetestingbackend.service.testsession.SessionActor
import com.example.adaptivetestingbackend.service.testsession.TestSessionService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/test-sessions")
class TestSessionController(
    private val testSessionService: TestSessionService,
) {
    @PostMapping
    fun createSession(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody(required = false) request: CreateTestSessionRequest?,
    ): CreateTestSessionResponse {
        return testSessionService.createSession(
            userEmail = userDetails.username,
            categoryId = request?.categoryId,
        )
    }

    @GetMapping("/{id}/next-question")
    fun getNextQuestion(
        @PathVariable id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestHeader(name = "X-Guest-Session-Key", required = false) guestSessionKey: String?,
    ): NextQuestionResponse {
        return testSessionService.getNextQuestion(id, resolveActor(id, userDetails, guestSessionKey))
    }

    @PostMapping("/{id}/answers")
    fun submitAnswer(
        @PathVariable id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestHeader(name = "X-Guest-Session-Key", required = false) guestSessionKey: String?,
        @Valid @RequestBody request: SubmitAnswerRequest,
    ): SubmitAnswerResponse {
        return testSessionService.submitAnswer(id, resolveActor(id, userDetails, guestSessionKey), request)
    }

    @PostMapping("/{id}/finish")
    fun finishSession(
        @PathVariable id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestHeader(name = "X-Guest-Session-Key", required = false) guestSessionKey: String?,
    ): ResultProfileResponse {
        return testSessionService.finishSession(id, resolveActor(id, userDetails, guestSessionKey))
    }

    private fun resolveActor(sessionId: UUID, userDetails: UserDetails?, guestSessionKey: String?): SessionActor {
        if (userDetails != null) {
            return testSessionService.resolveCandidateActor(userDetails.username)
        }
        if (!guestSessionKey.isNullOrBlank()) {
            return SessionActor.GuestActor(sessionId = sessionId, guestKey = guestSessionKey)
        }
        throw org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Authentication required")
    }
}
