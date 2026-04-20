package com.example.adaptivetestingbackend.service.token

import com.example.adaptivetestingbackend.dto.testsession.TestCategoryResponse
import com.example.adaptivetestingbackend.dto.token.TokenPreviewResponse
import com.example.adaptivetestingbackend.dto.token.TokenSessionStartResponse
import com.example.adaptivetestingbackend.entity.RoleName
import com.example.adaptivetestingbackend.repository.TestAccessTokenRepository
import com.example.adaptivetestingbackend.repository.UserRepository
import com.example.adaptivetestingbackend.service.testsession.SessionActor
import com.example.adaptivetestingbackend.service.testsession.TestSessionService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class TokenAuthOrSessionService(
    private val testAccessTokenRepository: TestAccessTokenRepository,
    private val userRepository: UserRepository,
    private val testSessionService: TestSessionService,
) {
    @Transactional(readOnly = true)
    fun preview(token: String): TokenPreviewResponse {
        if (token.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is blank")
        }

        val entity = testAccessTokenRepository.findByToken(token).orElse(null)
            ?: return TokenPreviewResponse(valid = false, used = false, category = null, requiresAuth = false)

        return TokenPreviewResponse(
            valid = true,
            used = entity.isUsed,
            category = entity.category.toResponse(),
            requiresAuth = false,
        )
    }

    @Transactional
    fun startGuest(token: String, guestName: String): TokenSessionStartResponse {
        if (token.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is blank")
        if (guestName.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "guestName is blank")

        val accessToken = testAccessTokenRepository.findByToken(token)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found") }

        if (accessToken.isUsed) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Token already used")
        }

        val created = testSessionService.createSessionByActor(
            SessionActor.GuestActor(
                sessionId = java.util.UUID.randomUUID(),
                guestKey = java.util.UUID.randomUUID().toString().replace("-", ""),
                guestName = guestName,
            ),
            accessToken.category.id,
            accessToken,
        )

        return TokenSessionStartResponse(
            sessionId = created.sessionId,
            status = created.status,
            createdAt = created.createdAt,
            startedAt = created.startedAt,
            category = created.category,
            guestSession = true,
            guestSessionKey = created.guestSessionKey,
        )
    }

    @Transactional
    fun startCandidate(token: String, candidateEmail: String): TokenSessionStartResponse {
        if (token.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is blank")

        val user = userRepository.findByEmail(candidateEmail)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }

        if (user.role.name != RoleName.CANDIDATE) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Controller cannot start candidate test session")
        }

        val accessToken = testAccessTokenRepository.findByToken(token)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found") }

        if (accessToken.isUsed) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Token already used")
        }

        val created = testSessionService.createSessionByActor(
            SessionActor.CandidateActor(user),
            accessToken.category.id,
            accessToken,
        )

        return TokenSessionStartResponse(
            sessionId = created.sessionId,
            status = created.status,
            createdAt = created.createdAt,
            startedAt = created.startedAt,
            category = created.category,
            guestSession = false,
            guestSessionKey = null,
        )
    }
}

private fun com.example.adaptivetestingbackend.entity.TestCategoryEntity.toResponse() = TestCategoryResponse(
    id = id,
    code = code,
    name = name,
    description = description,
)
