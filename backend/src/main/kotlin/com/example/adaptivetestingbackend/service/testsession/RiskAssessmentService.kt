package com.example.adaptivetestingbackend.service.testsession

import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class RiskAssessmentService {
    fun assess(profile: ResultCalculationService.CalculatedProfile): CandidateRiskAssessment {
        val factors = mutableListOf<RiskFactor>()

        addScaleRisk(
            factors = factors,
            scaleTitle = "Внимание",
            score = profile.attention,
            criticalReason = "низкое внимание повышает риск пропуска важных деталей и ошибок в задачах, требующих точности",
            moderateReason = "средний уровень внимания может требовать дополнительного контроля в задачах с большим количеством деталей",
        )
        addScaleRisk(
            factors = factors,
            scaleTitle = "Стрессоустойчивость",
            score = profile.stressResistance,
            criticalReason = "низкая стрессоустойчивость повышает риск снижения качества решений при давлении и дедлайнах",
            moderateReason = "умеренная стрессоустойчивость может проявляться нестабильно в условиях высокой нагрузки",
        )
        addScaleRisk(
            factors = factors,
            scaleTitle = "Ответственность",
            score = profile.responsibility,
            criticalReason = "низкая ответственность повышает риск несоблюдения договорённостей и слабого контроля результата",
            moderateReason = "средний уровень ответственности требует внешнего контроля сроков и качества выполнения задач",
        )
        addScaleRisk(
            factors = factors,
            scaleTitle = "Адаптивность",
            score = profile.adaptability,
            criticalReason = "низкая адаптивность повышает риск ошибок при изменении условий, новых требованиях или нестандартных ситуациях",
            moderateReason = "средняя адаптивность может ограничивать эффективность в динамичной среде",
        )
        addScaleRisk(
            factors = factors,
            scaleTitle = "Скорость и точность решений",
            score = profile.decisionSpeedAccuracy,
            criticalReason = "низкая скорость и точность решений повышает риск затруднений при выборе действий в ограниченное время",
            moderateReason = "средний уровень скорости и точности решений может требовать дополнительной проверки в срочных задачах",
        )

        if (profile.reliabilityFactor < BigDecimal("0.85")) {
            factors += RiskFactor(
                severity = RiskSeverity.MODERATE,
                title = "Достоверность результата",
                description = "сниженная достоверность результата требует осторожной интерпретации выводов",
            )
        }
        if (profile.reliabilityFactor < BigDecimal("0.75")) {
            factors += RiskFactor(
                severity = RiskSeverity.HIGH,
                title = "Достоверность результата",
                description = "выявлены признаки возможного искажения ответов или недостаточно надёжного прохождения теста",
            )
        }

        val highCount = factors.count { it.severity == RiskSeverity.HIGH }
        val moderateCount = factors.count { it.severity == RiskSeverity.MODERATE }
        val level = when {
            highCount >= 2 -> CandidateRiskLevel.HIGH
            highCount == 1 || moderateCount >= 3 -> CandidateRiskLevel.MEDIUM
            moderateCount >= 1 -> CandidateRiskLevel.LOW
            else -> CandidateRiskLevel.MINIMAL
        }

        val recommendation = when (level) {
            CandidateRiskLevel.HIGH -> "Рекомендуется дополнительное собеседование или повторная проверка по слабым зонам перед принятием решения."
            CandidateRiskLevel.MEDIUM -> "Рекомендуется учитывать выявленные слабые зоны и сопоставить результат с требованиями конкретной роли."
            CandidateRiskLevel.LOW -> "Существенных критических рисков не выявлено, но отдельные зоны требуют внимания при адаптации кандидата."
            CandidateRiskLevel.MINIMAL -> "Выраженных рисков профиля не выявлено."
        }

        return CandidateRiskAssessment(
            level = level,
            title = riskTitle(level),
            factors = factors.distinctBy { it.title to it.description },
            recommendation = recommendation,
        )
    }

    private fun addScaleRisk(
        factors: MutableList<RiskFactor>,
        scaleTitle: String,
        score: BigDecimal,
        criticalReason: String,
        moderateReason: String,
    ) {
        when {
            score < BigDecimal("40") -> factors += RiskFactor(
                severity = RiskSeverity.HIGH,
                title = scaleTitle,
                description = criticalReason,
            )
            score < BigDecimal("60") -> factors += RiskFactor(
                severity = RiskSeverity.MODERATE,
                title = scaleTitle,
                description = moderateReason,
            )
        }
    }

    private fun riskTitle(level: CandidateRiskLevel): String = when (level) {
        CandidateRiskLevel.MINIMAL -> "минимальный"
        CandidateRiskLevel.LOW -> "низкий"
        CandidateRiskLevel.MEDIUM -> "средний"
        CandidateRiskLevel.HIGH -> "высокий"
    }
}

data class CandidateRiskAssessment(
    val level: CandidateRiskLevel,
    val title: String,
    val factors: List<RiskFactor>,
    val recommendation: String,
) {
    fun toSummaryBlock(): String {
        val factorText = if (factors.isEmpty()) {
            "существенных факторов риска не выявлено"
        } else {
            factors.joinToString("; ") { "${it.title}: ${it.description}" }
        }

        return "Риск профиля: $title. Причины: $factorText. $recommendation"
    }
}

data class RiskFactor(
    val severity: RiskSeverity,
    val title: String,
    val description: String,
)

enum class CandidateRiskLevel {
    MINIMAL,
    LOW,
    MEDIUM,
    HIGH,
}

enum class RiskSeverity {
    MODERATE,
    HIGH,
}
