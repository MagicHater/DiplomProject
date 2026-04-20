package com.example.adaptivetestingbackend.controller.token

import com.example.adaptivetestingbackend.dto.token.StartCandidateByTokenRequest
import com.example.adaptivetestingbackend.dto.token.StartGuestByTokenRequest
import com.example.adaptivetestingbackend.dto.token.TokenPreviewRequest
import com.example.adaptivetestingbackend.dto.token.TokenPreviewResponse
import com.example.adaptivetestingbackend.dto.token.TokenSessionStartResponse
import com.example.adaptivetestingbackend.service.token.TokenAuthOrSessionService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/token-access")
class TokenAccessController(
    private val tokenAuthOrSessionService: TokenAuthOrSessionService,
) {
    @PostMapping("/preview")
    fun preview(@Valid @RequestBody request: TokenPreviewRequest): TokenPreviewResponse =
        tokenAuthOrSessionService.preview(request.token)

    @PostMapping("/start-guest")
    fun startGuest(@Valid @RequestBody request: StartGuestByTokenRequest): TokenSessionStartResponse =
        tokenAuthOrSessionService.startGuest(request.token, request.guestName)

    @PostMapping("/start-candidate")
    fun startCandidate(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: StartCandidateByTokenRequest,
    ): TokenSessionStartResponse =
        tokenAuthOrSessionService.startCandidate(request.token, userDetails.username)
}
