package com.example.adaptivetestingbackend

import com.example.adaptivetestingbackend.entity.QuestionEntity
import com.example.adaptivetestingbackend.entity.TestCategoryEntity
import com.example.adaptivetestingbackend.service.testsession.AdaptiveQuestionSelectionStrategy
import com.example.adaptivetestingbackend.service.testsession.AdaptiveSessionState
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

class AdaptiveQuestionSelectionStrategyTest {
    private val strategy = AdaptiveQuestionSelectionStrategy(ObjectMapper(), maxQuestionsPerSession = 3)

    @Test
    fun `start of test picks first best priority question when no progress`() {
        val q1 = question(priority = 10, weights = """{"attention":0.5,"stress_resistance":0.5}""")
        val q2 = question(priority = 5, weights = """{"responsibility":0.9,"adaptability":0.1}""")

        val selected = strategy.selectNextQuestion(listOf(q1, q2), emptySet(), AdaptiveSessionState())

        assertEquals(q1.id, selected?.id)
    }

    @Test
    fun `partially covered scales prefer question for weak coverage`() {
        val attentionQuestion = question(priority = 1, weights = """{"attention":0.9,"stress_resistance":0.1}""")
        val adaptabilityQuestion = question(priority = 1, weights = """{"adaptability":0.9,"attention":0.1}""")

        val state = AdaptiveSessionState(
            answeredQuestions = 2,
            scaleCoverage = mapOf(
                "attention" to BigDecimal("1.4"),
                "adaptability" to BigDecimal("0.2"),
            ),
            scaleScoreSums = mapOf("adaptability" to BigDecimal("0.2")),
            scaleScoreSquares = mapOf("adaptability" to BigDecimal("0.8")),
        )

        val selected = strategy.selectNextQuestion(listOf(attentionQuestion, adaptabilityQuestion), emptySet(), state)

        assertEquals(adaptabilityQuestion.id, selected?.id)
    }

    @Test
    fun `returns null when no available questions`() {
        val selected = strategy.selectNextQuestion(emptyList(), emptySet(), AdaptiveSessionState())
        assertNull(selected)
    }

    @Test
    fun `returns null when session limit reached`() {
        val q1 = question(priority = 10, weights = """{"attention":1.0}""")

        val selected = strategy.selectNextQuestion(
            allActiveQuestions = listOf(q1),
            askedQuestionIds = emptySet(),
            state = AdaptiveSessionState(answeredQuestions = 3),
        )

        assertNull(selected)
    }

    @Test
    fun `does not select already asked questions`() {
        val asked = question(priority = 10, weights = """{"attention":1.0}""")
        val available = question(priority = 1, weights = """{"adaptability":1.0}""")

        val selected = strategy.selectNextQuestion(
            allActiveQuestions = listOf(asked, available),
            askedQuestionIds = setOf(asked.id),
            state = AdaptiveSessionState(),
        )

        assertEquals(available.id, selected?.id)
    }

    private val category = TestCategoryEntity(code = "MARKETING", name = "Маркетолог")

    private fun question(priority: Int, weights: String): QuestionEntity =
        QuestionEntity(
            id = UUID.randomUUID(),
            text = "q",
            scaleWeightsJson = weights,
            difficulty = 1,
            priority = priority,
            isActive = true,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            category = category,
        )
}
