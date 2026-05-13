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
import com.example.adaptivetestingbackend.dto.controller.ControllerSessionAnswerResponse
import com.example.adaptivetestingbackend.dto.testsession.ResultProfileResponse
import com.example.adaptivetestingbackend.entity.ResultProfileEntity
import com.example.adaptivetestingbackend.entity.RoleName
import com.example.adaptivetestingbackend.entity.TestSessionStatus
import com.example.adaptivetestingbackend.entity.UserEntity
import com.example.adaptivetestingbackend.repository.AnswerRepository
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
    private val answerRepository: AnswerRepository,
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
            return ControllerDashboardResponse(0, 0, ControllerDashboardAveragesResponse(0.0, 0.0, 0.0, 0.0, 0.0, 0.0), ControllerDashboardDistributionResponse(0, 0, 0), emptyList(), emptyList(), emptyList())
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
        val participants = profiles.groupBy { profile -> profile.session.candidate?.id?.toString() ?: "guest:${profile.session.guestIdentifier ?: profile.session.id}" }
        val topCandidates = participants.map { (participantKey, participantProfiles) ->
            val first = participantProfiles.first()
            val candidate = first.session.candidate
            ControllerDashboardCandidateRankResponse(
                participantId = if (candidate != null) "candidate:${candidate.id}" else participantKey,
                participantType = if (candidate != null) "candidate" else "guest",
                displayName = candidate?.fullName ?: first.session.guestIdentifier ?: first.session.accessToken?.usedByGuestDisplayName ?: "Гость",
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
        val categoryStatistics = profiles.groupBy { it.session.category }.map { (category, categoryProfiles) ->
            val metrics = categoryProfiles.metricAverages()
            val weakest = metrics.minByOrNull { it.value }
            ControllerDashboardCategoryStatisticsResponse(
                category.id.toString(), category.name, categoryProfiles.size, categoryProfiles.map { it.overallScore() }.average().round2(),
                metrics.first { it.code == "attention" }.value.round2(), metrics.first { it.code == "stressResistance" }.value.round2(),
                metrics.first { it.code == "responsibility" }.value.round2(), metrics.first { it.code == "adaptability" }.value.round2(),
                metrics.first { it.code == "decisionSpeedAccuracy" }.value.round2(), weakest?.title ?: "Нет данных", weakest?.value?.round2() ?: 0.0,
            )
        }.sortedBy { it.averageScore }
        return ControllerDashboardResponse(profiles.size, participants.size, ControllerDashboardAveragesResponse(avgAttention.round2(), avgStress.round2(), avgResponsibility.round2(), avgAdaptability.round2(), avgDecision.round2(), overall.round2()), ControllerDashboardDistributionResponse(low, medium, high), weakMetrics, topCandidates, categoryStatistics)
    }

    @Transactional(readOnly = true)
    fun getCandidates(controllerEmail: String): List<ControllerParticipantListItemResponse> {
        val controller = getControllerUser(controllerEmail)
        val profiles = resultProfileRepository.findCompletedByControllerIdOrderByCompletedAtDesc(controller.id, TestSessionStatus.COMPLETED)
        val participants = linkedMapOf<String, ParticipantAccumulator>()
        profiles.forEach { profile ->
            val session = profile.session
            val candidate = session.candidate
            val guestIdentifier = session.guestIdentifier ?: session.accessToken?.usedByGuestDisplayName
            val participant = if (candidate != null) ParticipantAccumulator("candidate:${candidate.id}", "candidate", candidate.fullName, candidate.email) else ParticipantAccumulator("guest:${guestIdentifier ?: "unknown"}", "guest", guestIdentifier ?: "Гость", null)
            val existing = participants.getOrPut(participant.participantId) { participant }
            existing.completedSessionsCount += 1
            val completedAt = session.completedAt
            if (completedAt != null && (existing.lastCompletedAt == null || completedAt.isAfter(existing.lastCompletedAt))) existing.lastCompletedAt = completedAt
        }
        return participants.values.map { ControllerParticipantListItemResponse(it.participantId, it.participantType, it.displayName, it.email, it.completedSessionsCount, it.lastCompletedAt) }.sortedByDescending { it.lastCompletedAt }
    }

    @Transactional(readOnly = true)
    fun getCandidateResults(participantType: String, participantKey: String, controllerEmail: String): ControllerParticipantResultsResponse {
        val controller = getControllerUser(controllerEmail)
        val normalizedType = participantType.trim().lowercase()
        val profiles = when (normalizedType) {
            "candidate" -> resultProfileRepository.findCompletedByControllerIdAndCandidateIdOrderByCompletedAtDesc(controller.id, UUID.fromString(participantKey), TestSessionStatus.COMPLETED)
            "guest" -> resultProfileRepository.findCompletedByControllerIdAndGuestIdentifierOrderByCompletedAtDesc(controller.id, participantKey, TestSessionStatus.COMPLETED)
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown participantType: $participantType")
        }
        if (profiles.isEmpty()) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Participant results not found")
        val firstSession = profiles.first().session
        val displayName = if (normalizedType == "candidate") firstSession.candidate?.fullName ?: "Кандидат" else firstSession.guestIdentifier ?: firstSession.accessToken?.usedByGuestDisplayName ?: "Гость"
        val email = if (normalizedType == "candidate") firstSession.candidate?.email else null
        return ControllerParticipantResultsResponse(
            participantId = "$normalizedType:$participantKey", participantType = normalizedType, displayName = displayName, email = email,
            sessions = profiles.map { resultProfileMapper.toResultListItem(it) },
            statistics = ControllerParticipantStatisticsResponse("$normalizedType:$participantKey", profiles.sortedBy { it.session.completedAt ?: it.session.createdAt }.mapIndexed { index, profile ->
                ControllerParticipantStatisticsSessionResponse(index + 1, profile.session.id.toString(), profile.session.completedAt ?: profile.session.createdAt, profile.attentionScore.toDouble(), profile.stressResistanceScore.toDouble(), profile.responsibilityScore.toDouble(), profile.adaptabilityScore.toDouble(), profile.decisionSpeedAccuracyScore.toDouble())
            }),
        )
    }

    @Transactional(readOnly = true)
    fun getCandidateResults(candidateId: UUID, controllerEmail: String): ControllerParticipantResultsResponse = getCandidateResults("candidate", candidateId.toString(), controllerEmail)

    @Transactional(readOnly = true)
    fun getResultDetails(sessionId: UUID, controllerEmail: String): ResultProfileResponse {
        val controller = getControllerUser(controllerEmail)
        val profile = resultProfileRepository.findCompletedBySessionIdAndControllerId(sessionId, controller.id, TestSessionStatus.COMPLETED).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Completed result not found for controller/session pair: $sessionId") }
        return resultProfileMapper.toResultProfile(profile)
    }

    @Transactional(readOnly = true)
    fun getSessionAnswers(sessionId: UUID, controllerEmail: String): List<ControllerSessionAnswerResponse> {
        getResultDetails(sessionId, controllerEmail)
        return answerRepository.findBySessionId(sessionId).sortedBy { it.questionSnapshot.questionOrder }.map { answer ->
            ControllerSessionAnswerResponse(
                questionOrder = answer.questionSnapshot.questionOrder,
                questionText = answer.questionSnapshot.questionText,
                selectedAnswerText = answer.selectedOption.optionText,
                answerValue = answer.answerValue,
                responseTimeMs = answer.responseTimeMs,
                difficulty = answer.questionSnapshot.difficulty.toInt(),
            )
        }
    }

    private fun getControllerUser(userEmail: String): UserEntity {
        val user = userRepository.findByEmail(userEmail).orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }
        if (user.role.name != RoleName.CONTROLLER) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only controller can use controller endpoints")
        return user
    }

    private fun List<BigDecimal>.average(): Double = if (isEmpty()) 0.0 else map { it.toDouble() }.average()
    private fun Iterable<ResultProfileEntity>.averageOf(selector: (ResultProfileEntity) -> BigDecimal): Double = map(selector).average()
    private fun Iterable<ResultProfileEntity>.metricAverages(): List<MetricAverage> = listOf(MetricAverage("attention", "Внимание", averageOf { it.attentionScore }), MetricAverage("stressResistance", "Стрессоустойчивость", averageOf { it.stressResistanceScore }), MetricAverage("responsibility", "Ответственность", averageOf { it.responsibilityScore }), MetricAverage("adaptability", "Адаптивность", averageOf { it.adaptabilityScore }), MetricAverage("decisionSpeedAccuracy", "Скорость и точность решений", averageOf { it.decisionSpeedAccuracyScore }))
    private fun ResultProfileEntity.overallScore(): Double = listOf(attentionScore, stressResistanceScore, responsibilityScore, adaptabilityScore, decisionSpeedAccuracyScore).average()
    private fun Double.round2(): Double = BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP).toDouble()
}
