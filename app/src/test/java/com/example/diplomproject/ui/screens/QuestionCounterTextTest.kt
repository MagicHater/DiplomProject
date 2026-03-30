package com.example.diplomproject.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionCounterTextTest {

    @Test
    fun `first question is shown as one when total is known`() {
        val text = formatQuestionCounterText(questionOrder = 1, totalAvailableQuestions = 10)

        assertEquals("Вопрос 1 из 10", text)
    }

    @Test
    fun `first question is shown as one when total is unknown`() {
        val text = formatQuestionCounterText(questionOrder = 1, totalAvailableQuestions = 0)

        assertEquals("Вопрос 1", text)
    }
}
