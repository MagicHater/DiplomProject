package com.example.adaptivetestingbackend.controller.testsession

import com.example.adaptivetestingbackend.dto.testsession.ResultProfileResponse
import com.example.adaptivetestingbackend.service.testsession.TestSessionService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/me/results")
class MyResultsController(
    private val testSessionService: TestSessionService,
) {
    @GetMapping("/{sessionId}")
    fun getResult(
        @PathVariable sessionId: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResultProfileResponse {
        return testSessionService.getMyResult(sessionId, userDetails.username)
    }
}
