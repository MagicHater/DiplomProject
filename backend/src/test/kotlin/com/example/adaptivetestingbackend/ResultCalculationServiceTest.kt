package com.example.adaptivetestingbackend

import com.example.adaptivetestingbackend.entity.AnswerEntity
import com.example.adaptivetestingbackend.entity.QuestionSnapshotEntity
import com.example.adaptivetestingbackend.entity.RoleEntity
import com.example.adaptivetestingbackend.entity.RoleName
import com.example.adaptivetestingbackend.entity.TestSessionEntity
import com.example.adaptivetestingbackend.entity.TestSessionStatus
import com.example.adaptivetestingbackend.entity.UserEntity
import com.example.adaptivetestingbackend.service.testsession.ResultCalculationService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ResultCalculationServiceTest {
    private val service = ResultCalculationService(ObjectMapper())

    @Test
    fun `calculate returns 100 for all scales when all answers are maximum`() {
        val session = testSession()
        val snapshot = QuestionSnapshotEntity(
            session = session,
            questionOrder = 1,
            questionText = "Q1",
            scaleWeightsJson = """{"attention":0.2,"stress_resistance":0.2,"responsibility":0.2,"adaptability":0.2,"decision_speed_accuracy":0.2}""",
            difficulty = 1,
            priority = 1,
        )
        val answer = AnswerEntity(
            session = session,
            questionSnapshot = snapshot,
            answerValue = BigDecimal("2.00"),
        )

        val result = service.calculate(listOf(answer))

        assertEquals(BigDecimal("100.00"), result.attention)
        assertEquals(BigDecimal("100.00"), result.stressResistance)
        assertEquals(BigDecimal("100.00"), result.responsibility)
        assertEquals(BigDecimal("100.00"), result.adaptability)
        assertEquals(BigDecimal("100.00"), result.decisionSpeedAccuracy)
    }

    @Test
    fun `calculate returns 50 for all scales on neutral answer`() {
        val session = testSession()
        val snapshot = QuestionSnapshotEntity(
            session = session,
            questionOrder = 1,
            questionText = "Q1",
            scaleWeightsJson = """{"attention":0.5,"stress_resistance":0.2,"responsibility":0.1,"adaptability":0.1,"decision_speed_accuracy":0.1}""",
            difficulty = 1,
            priority = 1,
        )
        val answer = AnswerEntity(
            session = session,
            questionSnapshot = snapshot,
            answerValue = BigDecimal("0.00"),
        )

        val result = service.calculate(listOf(answer))

        assertEquals(BigDecimal("50.00"), result.attention)
        assertEquals(BigDecimal("50.00"), result.stressResistance)
        assertEquals(BigDecimal("50.00"), result.responsibility)
        assertEquals(BigDecimal("50.00"), result.adaptability)
        assertEquals(BigDecimal("50.00"), result.decisionSpeedAccuracy)
    }

    private fun testSession(): TestSessionEntity {
        val role = RoleEntity(name = RoleName.CANDIDATE)
        val user = UserEntity(fullName = "Test", email = "test@example.com", passwordHash = "hash", role = role)
        return TestSessionEntity(candidate = user, status = TestSessionStatus.IN_PROGRESS)
    }
}
