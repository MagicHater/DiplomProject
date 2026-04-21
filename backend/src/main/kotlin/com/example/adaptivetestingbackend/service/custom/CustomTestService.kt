package com.example.adaptivetestingbackend.service.custom

import com.example.adaptivetestingbackend.dto.custom.CreateCustomTestRequest
import com.example.adaptivetestingbackend.dto.custom.CreateCustomTestResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestDetailsResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestListItemResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestOptionDetailsResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestOptionStatisticsResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestQuestionDetailsResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestQuestionStatisticsResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestResultAnswerResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestResultItemResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestStatisticsResponse
import com.example.adaptivetestingbackend.dto.custom.CustomTestSubmissionRequest
import com.example.adaptivetestingbackend.dto.custom.CustomTestSubmissionResponse
import com.example.adaptivetestingbackend.entity.RoleName
import com.example.adaptivetestingbackend.entity.UserEntity
import com.example.adaptivetestingbackend.entity.custom.CustomTestAllowedEmailEntity
import com.example.adaptivetestingbackend.entity.custom.CustomTestEntity
import com.example.adaptivetestingbackend.entity.custom.CustomTestOptionEntity
import com.example.adaptivetestingbackend.entity.custom.CustomTestQuestionEntity
import com.example.adaptivetestingbackend.entity.custom.CustomTestSubmissionAnswerEntity
import com.example.adaptivetestingbackend.entity.custom.CustomTestSubmissionEntity
import com.example.adaptivetestingbackend.repository.UserRepository
import com.example.adaptivetestingbackend.repository.custom.CustomTestAllowedEmailRepository
import com.example.adaptivetestingbackend.repository.custom.CustomTestOptionRepository
import com.example.adaptivetestingbackend.repository.custom.CustomTestQuestionRepository
import com.example.adaptivetestingbackend.repository.custom.CustomTestRepository
import com.example.adaptivetestingbackend.repository.custom.CustomTestSubmissionAnswerRepository
import com.example.adaptivetestingbackend.repository.custom.CustomTestSubmissionRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CustomTestService(
    private val userRepository: UserRepository,
    private val customTestRepository: CustomTestRepository,
    private val customTestQuestionRepository: CustomTestQuestionRepository,
    private val customTestOptionRepository: CustomTestOptionRepository,
    private val customTestAllowedEmailRepository: CustomTestAllowedEmailRepository,
    private val customTestSubmissionRepository: CustomTestSubmissionRepository,
    private val customTestSubmissionAnswerRepository: CustomTestSubmissionAnswerRepository,
) {
    @Transactional
    fun createTest(controllerEmail: String, request: CreateCustomTestRequest): CreateCustomTestResponse {
        val controller = getController(controllerEmail)
        val normalizedEmails = parseAndNormalizeEmails(request.allowedEmailsInput)
        if (normalizedEmails.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Allowed emails list must not be empty")
        }
        validateQuestions(request)

        val test = customTestRepository.save(
            CustomTestEntity(
                controller = controller,
                title = request.title.trim(),
                description = request.description?.trim()?.ifBlank { null },
                createdAt = OffsetDateTime.now(),
            ),
        )

        request.questions.mapIndexed { questionIndex, questionRequest ->
            val savedQuestion = customTestQuestionRepository.save(
                CustomTestQuestionEntity(
                    test = test,
                    questionOrder = questionIndex + 1,
                    text = questionRequest.text.trim(),
                ),
            )

            questionRequest.options.mapIndexed { optionIndex, optionRequest ->
                CustomTestOptionEntity(
                    question = savedQuestion,
                    optionOrder = optionIndex + 1,
                    text = optionRequest.text.trim(),
                )
            }.also { customTestOptionRepository.saveAll(it) }

            savedQuestion
        }

        customTestAllowedEmailRepository.saveAll(
            normalizedEmails.map { normalizedEmail ->
                CustomTestAllowedEmailEntity(test = test, email = normalizedEmail)
            },
        )

        return CreateCustomTestResponse(testId = test.id)
    }

    @Transactional(readOnly = true)
    fun getControllerTests(controllerEmail: String): List<CustomTestListItemResponse> {
        val controller = getController(controllerEmail)
        return customTestRepository.findAllByControllerIdOrderByCreatedAtDesc(controller.id).map { test ->
            CustomTestListItemResponse(
                id = test.id,
                title = test.title,
                description = test.description,
                questionsCount = customTestQuestionRepository.findAllByTestIdOrderByQuestionOrderAsc(test.id).size,
                allowedEmailsCount = customTestAllowedEmailRepository.findAllByTestId(test.id).size,
                submissionsCount = customTestSubmissionRepository.countByTestId(test.id),
                createdAt = test.createdAt,
            )
        }
    }

    @Transactional(readOnly = true)
    fun getControllerTestDetails(controllerEmail: String, testId: UUID): CustomTestDetailsResponse {
        val controller = getController(controllerEmail)
        val test = getControllerOwnedTest(controller.id, testId)
        return buildDetails(test)
    }

    @Transactional(readOnly = true)
    fun getAvailableTestsForUser(userEmail: String): List<CustomTestListItemResponse> {
        val user = getUser(userEmail)
        val normalizedEmail = user.email.trim().lowercase()
        return customTestRepository.findAvailableForEmail(normalizedEmail).map { test ->
            CustomTestListItemResponse(
                id = test.id,
                title = test.title,
                description = test.description,
                questionsCount = customTestQuestionRepository.findAllByTestIdOrderByQuestionOrderAsc(test.id).size,
                allowedEmailsCount = customTestAllowedEmailRepository.findAllByTestId(test.id).size,
                submissionsCount = customTestSubmissionRepository.countByTestId(test.id),
                createdAt = test.createdAt,
            )
        }
    }

    @Transactional
    fun submitTest(userEmail: String, testId: UUID, request: CustomTestSubmissionRequest): CustomTestSubmissionResponse {
        val user = getUser(userEmail)
        val normalizedEmail = user.email.trim().lowercase()
        if (!customTestAllowedEmailRepository.existsByTestIdAndEmail(testId, normalizedEmail)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "No access to this custom test")
        }

        val test = customTestRepository.findById(testId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Custom test not found") }

        val questions = customTestQuestionRepository.findAllByTestIdOrderByQuestionOrderAsc(testId)
        val options = customTestOptionRepository.findAllByQuestionIdInOrderByOptionOrderAsc(questions.map { it.id })
        val optionsByQuestion = options.groupBy { it.question.id }

        if (request.answers.size != questions.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "All questions must be answered")
        }

        val answersByQuestion = request.answers.associateBy { it.questionId }
        if (answersByQuestion.size != request.answers.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate answers for the same question")
        }

        questions.forEach { question ->
            val answer = answersByQuestion[question.id]
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing answer for question ${question.id}")
            val allowedOptionIds = optionsByQuestion[question.id].orEmpty().map { it.id }.toSet()
            if (answer.optionId !in allowedOptionIds) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Option does not belong to question ${question.id}")
            }
        }

        val submission = customTestSubmissionRepository.save(
            CustomTestSubmissionEntity(
                test = test,
                user = user,
                submittedAt = OffsetDateTime.now(),
            ),
        )

        customTestSubmissionAnswerRepository.saveAll(
            request.answers.map { answer ->
                val question = questions.first { it.id == answer.questionId }
                val option = options.first { it.id == answer.optionId }
                CustomTestSubmissionAnswerEntity(
                    submission = submission,
                    question = question,
                    option = option,
                )
            },
        )

        return CustomTestSubmissionResponse(submissionId = submission.id, submittedAt = submission.submittedAt)
    }

    @Transactional(readOnly = true)
    fun getControllerResults(controllerEmail: String, testId: UUID): List<CustomTestResultItemResponse> {
        val controller = getController(controllerEmail)
        getControllerOwnedTest(controller.id, testId)

        val submissions = customTestSubmissionRepository.findAllByTestIdOrderBySubmittedAtDesc(testId)
        val answers = customTestSubmissionAnswerRepository.findAllBySubmissionIdIn(submissions.map { it.id })
            .groupBy { it.submission.id }

        return submissions.map { submission ->
            val submissionAnswers = answers[submission.id].orEmpty().sortedBy { it.question.questionOrder }
            CustomTestResultItemResponse(
                submissionId = submission.id,
                userId = submission.user.id,
                userName = submission.user.fullName,
                userEmail = submission.user.email,
                submittedAt = submission.submittedAt,
                answers = submissionAnswers.map { answer ->
                    CustomTestResultAnswerResponse(
                        questionId = answer.question.id,
                        questionText = answer.question.text,
                        selectedOptionId = answer.option.id,
                        selectedOptionText = answer.option.text,
                    )
                },
            )
        }
    }

    @Transactional(readOnly = true)
    fun getControllerStatistics(controllerEmail: String, testId: UUID): CustomTestStatisticsResponse {
        val controller = getController(controllerEmail)
        getControllerOwnedTest(controller.id, testId)

        val questions = customTestQuestionRepository.findAllByTestIdOrderByQuestionOrderAsc(testId)
        val options = customTestOptionRepository.findAllByQuestionIdInOrderByOptionOrderAsc(questions.map { it.id })
        val aggregated = customTestSubmissionAnswerRepository.aggregateOptionSelection(testId)

        val selectionMap = mutableMapOf<UUID, MutableMap<UUID, Long>>()
        aggregated.forEach { row ->
            val questionId = row[0] as UUID
            val optionId = row[1] as UUID
            val count = row[2] as Long
            val questionMap = selectionMap.getOrPut(questionId) { mutableMapOf() }
            questionMap[optionId] = count
        }

        val submissionsCount = customTestSubmissionRepository.countByTestId(testId)

        return CustomTestStatisticsResponse(
            testId = testId,
            totalSubmissions = submissionsCount,
            questions = questions.map { question ->
                val optionSelection = selectionMap[question.id].orEmpty()
                CustomTestQuestionStatisticsResponse(
                    questionId = question.id,
                    questionText = question.text,
                    options = options.filter { it.question.id == question.id }.map { option ->
                        val count = optionSelection[option.id] ?: 0L
                        CustomTestOptionStatisticsResponse(
                            optionId = option.id,
                            optionText = option.text,
                            selectionsCount = count,
                            selectionsPercent = if (submissionsCount == 0L) 0.0 else (count.toDouble() * 100.0 / submissionsCount.toDouble()),
                        )
                    },
                )
            },
        )
    }

    private fun buildDetails(test: CustomTestEntity): CustomTestDetailsResponse {
        val questions = customTestQuestionRepository.findAllByTestIdOrderByQuestionOrderAsc(test.id)
        val options = customTestOptionRepository.findAllByQuestionIdInOrderByOptionOrderAsc(questions.map { it.id })
            .groupBy { it.question.id }
        val allowedEmails = customTestAllowedEmailRepository.findAllByTestId(test.id)
            .map { it.email }
            .sorted()

        return CustomTestDetailsResponse(
            id = test.id,
            title = test.title,
            description = test.description,
            createdAt = test.createdAt,
            allowedEmails = allowedEmails,
            questions = questions.map { question ->
                CustomTestQuestionDetailsResponse(
                    id = question.id,
                    order = question.questionOrder,
                    text = question.text,
                    options = options[question.id].orEmpty().map { option ->
                        CustomTestOptionDetailsResponse(
                            id = option.id,
                            order = option.optionOrder,
                            text = option.text,
                        )
                    },
                )
            },
        )
    }

    private fun getController(controllerEmail: String): UserEntity {
        val user = getUser(controllerEmail)
        if (user.role.name != RoleName.CONTROLLER) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only controller can use custom tests management")
        }
        return user
    }

    private fun getUser(email: String): UserEntity =
        userRepository.findByEmail(email.trim().lowercase())
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }

    private fun getControllerOwnedTest(controllerId: UUID, testId: UUID): CustomTestEntity {
        val test = customTestRepository.findById(testId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Custom test not found") }

        if (test.controller.id != controllerId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for this custom test")
        }
        return test
    }

    private fun validateQuestions(request: CreateCustomTestRequest) {
        if (request.title.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Title must not be blank")
        }
        if (request.questions.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one question is required")
        }

        request.questions.forEachIndexed { qIndex, question ->
            if (question.text.isBlank()) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Question #${qIndex + 1} text is blank")
            }
            if (question.options.size < 2) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Question #${qIndex + 1} must have at least 2 options")
            }
            question.options.forEachIndexed { oIndex, option ->
                if (option.text.isBlank()) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Option #${oIndex + 1} in question #${qIndex + 1} is blank")
                }
            }
        }
    }

    private fun parseAndNormalizeEmails(input: String): List<String> {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        return input
            .replace(';', '\n')
            .replace(',', '\n')
            .lines()
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
            .also { emails ->
                val invalid = emails.firstOrNull { !emailRegex.matches(it) }
                if (invalid != null) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email: $invalid")
                }
            }
    }
}
