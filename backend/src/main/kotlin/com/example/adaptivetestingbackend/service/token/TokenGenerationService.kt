package com.example.adaptivetestingbackend.service.token

import com.example.adaptivetestingbackend.repository.TestAccessTokenRepository
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDate
import java.util.Base64
import java.util.UUID

@Service
class TokenGenerationService(
    private val tokenRepository: TestAccessTokenRepository,
) {
    private val secureRandom = SecureRandom()

    fun generateUniqueToken(controllerId: UUID, categoryCode: String): String {
        repeat(10) {
            val candidate = generateToken(controllerId = controllerId, categoryCode = categoryCode)
            if (!tokenRepository.existsByToken(candidate)) {
                return candidate
            }
        }
        throw IllegalStateException("Unable to generate unique token")
    }

    fun generateToken(controllerId: UUID, categoryCode: String): String {
        val currentDate = LocalDate.now()
        val raw = "${controllerId}_${currentDate}_$categoryCode"

        // Nonce is required: without it, tokens generated on the same day by the same controller
        // for the same category would be identical and therefore predictable.
        val nonce = ByteArray(16).also(secureRandom::nextBytes)
        return hashToBase64Url(raw, nonce)
    }

    fun hashToBase64Url(raw: String, nonce: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashed = digest.digest(raw.toByteArray() + nonce)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed)
    }
}
