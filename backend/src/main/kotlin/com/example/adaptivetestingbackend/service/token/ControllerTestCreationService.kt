package com.example.adaptivetestingbackend.service.token

import com.example.adaptivetestingbackend.dto.controller.CreateControllerTestRequest
import com.example.adaptivetestingbackend.dto.controller.CreateControllerTestResponse
import com.example.adaptivetestingbackend.dto.controller.CreateControllerQuestionRequest
import com.example.adaptivetestingbackend.dto.controller.CreateControllerQuestionOptionRequest
import com.example.adaptivetestingbackend.entity.QuestionEntity
import com.example.adaptivetestingbackend.entity.QuestionOptionEntity
import com.example.adaptivetestingbackend.entity.RoleName
import com.example.adaptivetestingbackend.entity.TestCategoryEntity
import com.example.adaptivetestingbackend.repository.QuestionOptionRepository
import com.example.adaptivetestingbackend.repository.QuestionRepository
import com.example.adaptivetestingbackend.repository.TestCategoryRepository
import com.example.adaptivetestingbackend.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
class ControllerTestCreationService(
    private val userRepository: UserRepository,
    private val testCategoryRepository: TestCategoryRepository,
    private val questionRepository: QuestionRepository,
    private val questionOptionRepository: QuestionOptionRepository,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun createTest(controllerEmail: String, request: CreateControllerTestRequest): CreateControllerTestResponse {
        val normalizedEmail = controllerEmail.trim().lowercase()
        val controller = userRepository.findByEmail(normalizedEmail)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }

        if (controller.role.name != RoleName.CONTROLLER) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only controller can create tests")
        }

        validateQuestionOrders(request.questions)

        val now = OffsetDateTime.now()
        val category = testCategoryRepository.save(
            TestCategoryEntity(
                code = generateUniqueCategoryCode(request.name),
                name = request.name.trim(),
                description = request.description?.trim()?.ifBlank { null },
                isActive = true,
                createdAt = now,
                updatedAt = now,
            ),
        )

        request.questions.forEach { questionRequest ->
            val question = questionRepository.save(
                QuestionEntity(
                    text = questionRequest.text.trim(),
                    scaleWeightsJson = objectMapper.writeValueAsString(defaultScaleWeights()),
                    difficulty = questionRequest.difficulty ?: 1,
                    priority = questionRequest.priority ?: 0,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now,
                    category = category,
                ),
            )

            val options = questionRequest.options.map { optionRequest ->
                optionRequest.toEntity(question, objectMapper)
            }

            questionOptionRepository.saveAll(options)
        }

        return CreateControllerTestResponse(
            categoryId = category.id,
            code = category.code,
            name = category.name,
            questionsCount = request.questions.size,
        )
    }

    private fun generateUniqueCategoryCode(name: String): String {
        val base = name
            .trim()
            .uppercase()
            .replace(Regex("[^A-Z0-9]+"), "_")
            .trim('_')
            .ifBlank { "CUSTOM_TEST" }
            .take(40)

        var attempt = 1
        while (true) {
            val candidate = if (attempt == 1) base else "${base}_${attempt}"
            if (testCategoryRepository.findByCode(candidate).isEmpty) {
                return candidate
            }
            attempt += 1
        }
    }

    private fun validateQuestionOrders(questions: List<CreateControllerQuestionRequest>) {
        questions.forEachIndexed { questionIndex, question ->
            val duplicate = question.options.groupingBy { it.order }.eachCount().any { (order, count) -> order != null && count > 1 }
            if (duplicate) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Question #${questionIndex + 1} has duplicate option order values",
                )
            }
        }
    }

    private fun defaultScaleWeights(): Map<String, Double> = mapOf(
        "attention" to 0.20,
        "stress_resistance" to 0.20,
        "responsibility" to 0.20,
        "adaptability" to 0.20,
        "decision_speed_accuracy" to 0.20,
    )
}

private fun CreateControllerQuestionOptionRequest.toEntity(
    question: QuestionEntity,
    objectMapper: ObjectMapper,
): QuestionOptionEntity {
    val scales = requireNotNull(scaleContributions)
    return QuestionOptionEntity(
        question = question,
        optionOrder = requireNotNull(order),
        optionText = text.trim(),
        contributionValue = requireNotNull(contributionValue),
        scaleContributionsJson = objectMapper.writeValueAsString(
            mapOf(
                "attention" to requireNotNull(scales.attention),
                "stress_resistance" to requireNotNull(scales.stressResistance),
                "responsibility" to requireNotNull(scales.responsibility),
                "adaptability" to requireNotNull(scales.adaptability),
                "decision_speed_accuracy" to requireNotNull(scales.decisionSpeedAccuracy),
            ),
        ),
    )
}
