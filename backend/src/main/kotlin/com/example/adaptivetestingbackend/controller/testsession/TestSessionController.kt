package com.example.adaptivetestingbackend.controller.testsession

import com.example.adaptivetestingbackend.dto.testsession.CreateTestSessionResponse
import com.example.adaptivetestingbackend.dto.testsession.NextQuestionResponse
import com.example.adaptivetestingbackend.service.testsession.TestSessionService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/test-sessions")
class TestSessionController(
    private val testSessionService: TestSessionService,
) {
    @PostMapping
    fun createSession(@AuthenticationPrincipal userDetails: UserDetails): CreateTestSessionResponse {
        return testSessionService.createSession(userDetails.username)
    }

    @GetMapping("/{id}/next-question")
    fun getNextQuestion(
        @PathVariable id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): NextQuestionResponse {
        return testSessionService.getNextQuestion(id, userDetails.username)
    }
}
