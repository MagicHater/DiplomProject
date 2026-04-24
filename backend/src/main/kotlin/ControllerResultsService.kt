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
            averages = ControllerDashboardAveragesResponse(0.0,0.0,0.0,0.0,0.0,0.0),
            distribution = ControllerDashboardDistributionResponse(0,0,0),
            weakMetrics = emptyList(),
            topCandidates = emptyList(),
        )
    }

    val total = profiles.size

    val avgAttention = profiles.map { it.attentionScore }.average()
    val avgStress = profiles.map { it.stressResistanceScore }.average()
    val avgResp = profiles.map { it.responsibilityScore }.average()
    val avgAdapt = profiles.map { it.adaptabilityScore }.average()
    val avgDecision = profiles.map { it.decisionSpeedAccuracyScore }.average()

    val overall = listOf(avgAttention, avgStress, avgResp, avgAdapt, avgDecision).average()

    val low = profiles.count { it.attentionScore < 50 }
    val medium = profiles.count { it.attentionScore in 50..75 }
    val high = profiles.count { it.attentionScore > 75 }

    return ControllerDashboardResponse(
        totalCompletedSessions = total,
        totalParticipants = profiles.map { it.session.candidate?.id ?: it.session.guestIdentifier }.distinct().size,
        averages = ControllerDashboardAveragesResponse(
            attention = avgAttention,
            stressResistance = avgStress,
            responsibility = avgResp,
            adaptability = avgAdapt,
            decisionSpeedAccuracy = avgDecision,
            overall = overall,
        ),
        distribution = ControllerDashboardDistributionResponse(
            low = low,
            medium = medium,
            high = high,
        ),
        weakMetrics = listOf(
            ControllerDashboardWeakMetricResponse("attention", "Внимание", avgAttention),
            ControllerDashboardWeakMetricResponse("stress", "Стрессоустойчивость", avgStress),
        ).sortedBy { it.average }.take(2),
        topCandidates = profiles
            .groupBy { it.session.candidate?.id ?: it.session.guestIdentifier }
            .map {
                val avg = it.value.map { p ->
                    (p.attentionScore + p.stressResistanceScore +
                            p.responsibilityScore + p.adaptabilityScore +
                            p.decisionSpeedAccuracyScore) / 5.0
                }.average()

                ControllerDashboardCandidateRankResponse(
                    participantId = it.key.toString(),
                    participantType = if (it.value.first().session.candidate != null) "candidate" else "guest",
                    displayName = it.value.first().session.candidate?.fullName
                        ?: it.value.first().session.guestIdentifier ?: "Гость",
                    email = it.value.first().session.candidate?.email,
                    sessionsCount = it.value.size,
                    averageScore = avg,
                    lastCompletedAt = it.value.first().session.completedAt,
                )
            }
            .sortedByDescending { it.averageScore }
            .take(5)
    )
}