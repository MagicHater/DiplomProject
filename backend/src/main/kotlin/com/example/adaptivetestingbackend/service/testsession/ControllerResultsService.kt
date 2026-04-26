package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.dto.controller.ControllerDashboardAveragesResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerDashboardCandidateRankResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerDashboardCategoryStatisticsResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerDashboardDistributionResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerDashboardResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerDashboardWeakMetricResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerParticipantListItemResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerParticipantResultsResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerParticipantStatisticsResponse
import com.example.adaptivetestingbackend.dto.controller.ControllerParticipantStatisticsSessionResponse
import com.example.adaptivetestingbackend.dto.testsession.ResultProfileResponse
import com.example.adaptivetestingbackend.entity.ResultProfileEntity
import com.example.adaptivetestingbackend.entity.RoleName
import com.example.adaptivetestingbackend.entity.TestSessionStatus
import com.example.adaptivetestingbackend.entity.UserEntity
import com.example.adaptivetestingbackend.repository.ResultProfileRepository
import com.example.adaptivetestingbackend.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.OffsetDateTime
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
        var lastCompletedAt: OffsetDateTime? = null,
    )

    private data class MetricAverage(
        val code: String,
        val title: String,
        val value: Double,
    )

    @Transactional(readOnly = true)
    fun getDashboard(controllerEmail: String): ControllerDashboardResponse {
        val controller = getControllerUser(controllerEmail)
        val profiles = resultProfileRepository.findCompletedByControllerIdOrderByCompletedAtDesc(
            controllerId = controller.id,
            status = TestSessionStatus.COMPLETED,
        )

        if (profiles.isEmpty()) {
            return ControllerDashboardResponse(
                totalCompletedSessions = 0,
                totalParticipants = 0,
                averages = ControllerDashboardAveragesResponse(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                distribution = ControllerDashboardDistributionResponse(0, 0, 0),
                weakMetrics = emptyList(),
                topCandidates = emptyList(),
                categoryStatistics = emptyList(),
            )
        }

        val avgAttention = profiles.averageOf { it.attentionScore }
        val avgStress = profiles.averageOf { it.stressResistanceScore }
        val avgResponsibility = profiles.averageOf { it.responsibilityScore }
        val avgAdaptability = profiles.averageOf { it.adaptabilityScore }
        val avgDecision = profiles.averageOf { it.decisionSpeedAccuracyScore }
        val overall = listOf(avgAttention, avgStress, avgResponsibility, avgAdaptability, avgDecision).average()

        val overallScores = profiles.map { it.overallScore() }
        val low = overallScores.count { it < 50.0 }
        val medium = overallScores.count { it >= 50.0 && it < 75.0 }
        val high = overallScores.count { it >= 75.0 }

        val participants = profiles.groupBy { profile ->
            profile.session.candidate?.id?.toString()
                ?: "guest:${profile.session.guestIdentifier ?: profile.session.id}"
        }

        val topCandidates = participants.map { (participantKey, participantProfiles) ->
            val first = participantProfiles.first()
            val candidate = first.session.candidate
            ControllerDashboardCandidateRankResponse(
                participantId = if (candidate != null) "candidate:${candidate.id}" else participantKey,
                participantType = if (candidate != null) "candidate" else "guest",
                displayName = candidate?.fullName
                    ?: first.session.guestIdentifier
                    ?: first.session.accessToken?.usedByGuestDisplayName
                    ?: "Гость",
                email = candidate?.email,
                sessionsCount = participantProfiles.size,
                averageScore = participantProfiles.map { it.overallScore() }.average().round2(),
                attention = participantProfiles.averageOf { it.attentionScore }.round2(),
                stressResistance = participantProfiles.averageOf { it.stressResistanceScore }.round2(),
                responsibility = participantProfiles.averageOf { it.responsibilityScore }.round2(),
                adaptability = participantProfiles.averageOf { it.adaptabilityScore }.round2(),
                decisionSpeedAccuracy = participantProfiles.averageOf { it.decisionSpeedAccuracyScore }.round2(),
                lastCompletedAt = participantProfiles.mapNotNull { it.session.completedAt }.maxOrNull(),
            )
        }.sortedByDescending { it.averageScore }.take(5)

        val weakMetrics = listOf(
            ControllerDashboardWeakMetricResponse("attention", "Внимание", avgAttention.round2()),
            ControllerDashboardWeakMetricResponse("stressResistance", "Стрессоустойчивость", avgStress.round2()),
            ControllerDashboardWeakMetricResponse("responsibility", "Ответственность", avgResponsibility.round2()),
            ControllerDashboardWeakMetricResponse("adaptability", "Адаптивность", avgAdaptability.round2()),
            ControllerDashboardWeakMetricResponse("decisionSpeedAccuracy", "Скорость и точность решений", avgDecision.round2()),
        ).sortedBy { it.average }.take(3)

        val categoryStatistics = profiles
            .groupBy { it.session.category }
            .map { (category, categoryProfiles) ->
                val metrics = categoryProfiles.metricAverages()
                val weakest = metrics.minByOrNull { it.value }
                ControllerDashboardCategoryStatisticsResponse(
                    categoryId = category.id.toString(),
                    categoryName = category.name,
                    sessionsCount = categoryProfiles.size,
                    averageScore = categoryProfiles.map { it.overallScore() }.average().round2(),
                    attention = metrics.first { it.code == "attention" }.value.round2(),
                    stressResistance = metrics.first { it.code == "stressResistance" }.value.round2(),
                    responsibility = metrics.first { it.code == "responsibility" }.value.round2(),
                    adaptability = metrics.first { it.code == "adaptability" }.value.round2(),
                    decisionSpeedAccuracy = metrics.first { it.code == "decisionSpeedAccuracy" }.value.round2(),
                    weakestMetricTitle = weakest?.title ?: "Нет данных",
                    weakestMetricAverage = weakest?.value?.round2() ?: 0.0,
                )
            }
            .sortedBy { it.averageScore }

        return ControllerDashboardResponse(
            totalCompletedSessions = profiles.size,
            totalParticipants = participants.size,
            averages = ControllerDashboardAveragesResponse(
                attention = avgAttention.round2(),
                stressResistance = avgStress.round2(),
                responsibility = avgResponsibility.round2(),
                adaptability = avgAdaptability.round2(),
                decisionSpeedAccuracy = avgDecision.round2(),
                overall = overall.round2(),
            ),
            distribution = ControllerDashboardDistributionResponse(
                low = low,
                medium = medium,
                high = high,
            ),
            weakMetrics = weakMetrics,
            topCandidates = topCandidates,
            categoryStatistics = categoryStatistics,
        )
    }

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
            statistics = ControllerParticipantStatisticsResponse(
                participantId = "$normalizedType:$participantKey",
                sessions = profiles
                    .sortedBy { it.session.completedAt ?: it.session.createdAt }
                    .mapIndexed { index, profile ->
                        ControllerParticipantStatisticsSessionResponse(
                            sessionOrder = index + 1,
                            sessionId = profile.session.id.toString(),
                            completedAt = profile.session.completedAt ?: profile.session.createdAt,
                            attention = profile.attentionScore.toDouble(),
                            stressResistance = profile.stressResistanceScore.toDouble(),
                            responsibility = profile.responsibilityScore.toDouble(),
                            adaptability = profile.adaptabilityScore.toDouble(),
                            decisionSpeedAccuracy = profile.decisionSpeedAccuracyScore.toDouble(),
                        )
                    },
            ),
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

    private fun List<BigDecimal>.average(): Double =
        if (isEmpty()) 0.0 else map { it.toDouble() }.average()

    private fun Iterable<ResultProfileEntity>.averageOf(selector: (ResultProfileEntity) -> BigDecimal): Double =
        map(selector).average()

    private fun Iterable<ResultProfileEntity>.metricAverages(): List<MetricAverage> = listOf(
        MetricAverage("attention", "Внимание", averageOf { it.attentionScore }),
        MetricAverage("stressResistance", "Стрессоустойчивость", averageOf { it.stressResistanceScore }),
        MetricAverage("responsibility", "Ответственность", averageOf { it.responsibilityScore }),
        MetricAverage("adaptability", "Адаптивность", averageOf { it.adaptabilityScore }),
        MetricAverage("decisionSpeedAccuracy", "Скорость и точность решений", averageOf { it.decisionSpeedAccuracyScore }),
    )

    private fun ResultProfileEntity.overallScore(): Double = listOf(
        attentionScore,
        stressResistanceScore,
        responsibilityScore,
        adaptabilityScore,
        decisionSpeedAccuracyScore,
    ).average()

    private fun Double.round2(): Double = BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP).toDouble()
}
