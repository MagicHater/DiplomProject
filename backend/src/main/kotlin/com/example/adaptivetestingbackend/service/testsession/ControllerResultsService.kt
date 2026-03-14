package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.dto.controller.ControllerCandidateListItemResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerCandidateResultsResponse
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
    @Transactional(readOnly = true)
    fun getCandidates(controllerEmail: String): List<ControllerCandidateListItemResponse> {
        getControllerUser(controllerEmail)

        return userRepository.findCandidatesWithAccountOrResults(RoleName.CANDIDATE)
            .map { candidate ->
                ControllerCandidateListItemResponse(
                    candidateId = candidate.id,
                    fullName = candidate.fullName,
                    email = candidate.email,
                    completedSessionsCount = resultProfileRepository.countCompletedByCandidateId(
                        candidateId = candidate.id,
                        status = TestSessionStatus.COMPLETED,
                    ),
                    lastCompletedAt = resultProfileRepository.findLastCompletedAtByCandidateId(
                        candidateId = candidate.id,
                        status = TestSessionStatus.COMPLETED,
                    ),
                )
            }
    }

    @Transactional(readOnly = true)
    fun getCandidateResults(candidateId: UUID, controllerEmail: String): ControllerCandidateResultsResponse {
        getControllerUser(controllerEmail)
        val candidate = getCandidate(candidateId)

        val sessions = resultProfileRepository.findCompletedByCandidateIdOrderByCompletedAtDesc(
            candidateId = candidate.id,
            status = TestSessionStatus.COMPLETED,
        ).map { resultProfileMapper.toResultListItem(it) }

        return ControllerCandidateResultsResponse(
            candidateId = candidate.id,
            fullName = candidate.fullName,
            sessions = sessions,
        )
    }

    @Transactional(readOnly = true)
    fun getResultDetails(sessionId: UUID, controllerEmail: String): ResultProfileResponse {
        getControllerUser(controllerEmail)

        val profile = resultProfileRepository.findBySessionIdWithCandidate(sessionId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Completed result not found for session: $sessionId",
                )
            }

        if (profile.session.status != TestSessionStatus.COMPLETED) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Session is not completed yet")
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
