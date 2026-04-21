package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.dto.controller.ControllerParticipantListItemResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerParticipantResultsResponse
import com.example.adaptivetestingbackend.dto.testsession.ResultProfileResponse
import com.example.adaptivetestingbackend.entity.RoleName
import com.example.adaptivetestingbackend.entity.TestSessionStatus
import com.example.adaptivetestingbackend.entity.UserEntity
import com.example.adaptivetestingbackend.repository.ResultProfileRepository
import com.example.adaptivetestingbackend.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class ControllerResultsService(
    private val userRepository: UserRepository,
    private val resultProfileRepository: ResultProfileRepository,
    private val resultProfileMapper: ResultProfileMapper,
) {
    private data class ParticipantAccumulator(
        val participantId: String,
        val participantType: String,
        val displayName: String,
        val email: String?,
        var completedSessionsCount: Long = 0,
        var lastCompletedAt: java.time.OffsetDateTime? = null,
    )

    @Transactional(readOnly = true)
    fun getCandidates(controllerEmail: String): List<ControllerParticipantListItemResponse> {
        val controller = getControllerUser(controllerEmail)

        val profiles = resultProfileRepository.findCompletedByControllerIdOrderByCompletedAtDesc(
            controllerId = controller.id,
            status = TestSessionStatus.COMPLETED,
        )

        val participants = linkedMapOf<String, ParticipantAccumulator>()
        profiles.forEach { profile ->
            val session = profile.session
            val candidate = session.candidate
            val guestIdentifier = session.guestIdentifier ?: session.accessToken?.usedByGuestDisplayName

            val participant = if (candidate != null) {
                ParticipantAccumulator(
                    participantId = "candidate:${candidate.id}",
                    participantType = "candidate",
                    displayName = candidate.fullName,
                    email = candidate.email,
                )
            } else {
                ParticipantAccumulator(
                    participantId = "guest:${guestIdentifier ?: "unknown"}",
                    participantType = "guest",
                    displayName = guestIdentifier ?: "Гость",
                    email = null,
                )
            }

            val existing = participants.getOrPut(participant.participantId) { participant }
            existing.completedSessionsCount += 1
            val completedAt = session.completedAt
            if (completedAt != null && (existing.lastCompletedAt == null || completedAt.isAfter(existing.lastCompletedAt))) {
                existing.lastCompletedAt = completedAt
            }
        }

        return participants.values
            .map { participant ->
                ControllerParticipantListItemResponse(
                    participantId = participant.participantId,
                    participantType = participant.participantType,
                    displayName = participant.displayName,
                    email = participant.email,
                    completedSessionsCount = participant.completedSessionsCount,
                    lastCompletedAt = participant.lastCompletedAt,
                )
            }
            .sortedByDescending { it.lastCompletedAt }
    }

    @Transactional(readOnly = true)
    fun getCandidateResults(
        participantType: String,
        participantKey: String,
        controllerEmail: String,
    ): ControllerParticipantResultsResponse {
        val controller = getControllerUser(controllerEmail)
        val normalizedType = participantType.trim().lowercase()

        val profiles = when (normalizedType) {
            "candidate" -> {
                val candidateId = UUID.fromString(participantKey)
                resultProfileRepository.findCompletedByControllerIdAndCandidateIdOrderByCompletedAtDesc(
                    controllerId = controller.id,
                    candidateId = candidateId,
                    status = TestSessionStatus.COMPLETED,
                )
            }
            "guest" -> {
                resultProfileRepository.findCompletedByControllerIdAndGuestIdentifierOrderByCompletedAtDesc(
                    controllerId = controller.id,
                    guestIdentifier = participantKey,
                    status = TestSessionStatus.COMPLETED,
                )
            }
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown participantType: $participantType")
        }

        if (profiles.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Participant results not found")
        }

        val firstSession = profiles.first().session
        val displayName = if (normalizedType == "candidate") {
            firstSession.candidate?.fullName ?: "Кандидат"
        } else {
            firstSession.guestIdentifier ?: firstSession.accessToken?.usedByGuestDisplayName ?: "Гость"
        }
        val email = if (normalizedType == "candidate") firstSession.candidate?.email else null

        return ControllerParticipantResultsResponse(
            participantId = "$normalizedType:$participantKey",
            participantType = normalizedType,
            displayName = displayName,
            email = email,
            sessions = profiles.map { resultProfileMapper.toResultListItem(it) },
        )
    }

    @Transactional(readOnly = true)
    fun getCandidateResults(candidateId: UUID, controllerEmail: String): ControllerParticipantResultsResponse {
        return getCandidateResults(
            participantType = "candidate",
            participantKey = candidateId.toString(),
            controllerEmail = controllerEmail,
        )
    }

    @Transactional(readOnly = true)
    fun getResultDetails(sessionId: UUID, controllerEmail: String): ResultProfileResponse {
        val controller = getControllerUser(controllerEmail)

        val profile = resultProfileRepository.findCompletedBySessionIdAndControllerId(
            sessionId = sessionId,
            controllerId = controller.id,
            status = TestSessionStatus.COMPLETED,
        ).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Completed result not found for controller/session pair: $sessionId",
            )
        }

        return resultProfileMapper.toResultProfile(profile)
    }

    private fun getControllerUser(userEmail: String): UserEntity {
        val user = userRepository.findByEmail(userEmail)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }

        if (user.role.name != RoleName.CONTROLLER) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only controller can use controller endpoints")
        }

        return user
    }

    @Suppress("unused")
    private fun getCandidate(candidateId: UUID): UserEntity {
        val candidate = userRepository.findById(candidateId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Candidate not found: $candidateId",
                )
            }

        if (candidate.role.name != RoleName.CANDIDATE) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a candidate")
        }

        return candidate
    }
}
