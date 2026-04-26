package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.dto.testsession.CreateTestSessionResponse
import com.example.adaptivetestingbackend.dto.testsession.MyResultListItemResponse
import com.example.adaptivetestingbackend.dto.testsession.NextQuestionResponse
import com.example.adaptivetestingbackend.dto.testsession.ResultProfileResponse
import com.example.adaptivetestingbackend.dto.testsession.SessionProgressDto
import com.example.adaptivetestingbackend.dto.testsession.SessionQuestionDto
import com.example.adaptivetestingbackend.dto.testsession.SessionQuestionOptionDto
import com.example.adaptivetestingbackend.dto.testsession.SubmitAnswerRequest
import com.example.adaptivetestingbackend.dto.testsession.SubmitAnswerResponse
import com.example.adaptivetestingbackend.dto.testsession.TestCategoryResponse
import com.example.adaptivetestingbackend.entity.AnswerEntity
import com.example.adaptivetestingbackend.entity.QuestionSnapshotEntity
import com.example.adaptivetestingbackend.entity.RoleName
import com.example.adaptivetestingbackend.entity.ResultProfileEntity
import com.example.adaptivetestingbackend.entity.TestAccessTokenEntity
import com.example.adaptivetestingbackend.entity.TestSessionEntity
import com.example.adaptivetestingbackend.entity.TestSessionStatus
import com.example.adaptivetestingbackend.entity.UserEntity
import com.example.adaptivetestingbackend.repository.AnswerRepository
import com.example.adaptivetestingbackend.repository.QuestionOptionRepository
import com.example.adaptivetestingbackend.repository.QuestionRepository
import com.example.adaptivetestingbackend.repository.QuestionSnapshotRepository
import com.example.adaptivetestingbackend.repository.ResultProfileRepository
import com.example.adaptivetestingbackend.repository.TestCategoryRepository
import com.example.adaptivetestingbackend.repository.TestSessionRepository
import com.example.adaptivetestingbackend.repository.UserRepository
import com.example.adaptivetestingbackend.service.ai.AiAdaptiveQuestionService
import com.example.adaptivetestingbackend.service.ai.AiResultInterpretationService
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Service
class TestSessionService(
    private val userRepository: UserRepository,
    private val testSessionRepository: TestSessionRepository,
    private val questionRepository: QuestionRepository,
    private val questionOptionRepository: QuestionOptionRepository,
    private val questionSnapshotRepository: QuestionSnapshotRepository,
    private val answerRepository: AnswerRepository,
    private val resultProfileRepository: ResultProfileRepository,
    private val adaptiveSessionStateService: AdaptiveSessionStateService,
    private val adaptiveQuestionSelectionStrategy: AdaptiveQuestionSelectionStrategy,
    private val resultCalculationService: ResultCalculationService,
    private val resultProfileMapper: ResultProfileMapper,
    private val testCategoryRepository: TestCategoryRepository,
    private val aiAdaptiveQuestionService: AiAdaptiveQuestionService,
    private val aiResultInterpretationService: AiResultInterpretationService,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun createSession(userEmail: String, categoryId: UUID?): CreateTestSessionResponse {
        val user = getCandidateUser(userEmail)
        return createSessionByActor(SessionActor.CandidateActor(user), categoryId ?: resolveDefaultCategoryId(), null)
    }

    @Transactional
    fun createSessionByActor(
        actor: SessionActor,
        categoryId: UUID,
        accessToken: TestAccessTokenEntity?,
    ): CreateTestSessionResponse {
        val category = testCategoryRepository.findById(categoryId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found") }

        val availableQuestions = questionRepository.countByIsActiveTrueAndCategoryId(category.id)
        if (availableQuestions == 0) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Category has no active questions")
        }

        val now = OffsetDateTime.now()
        val session = when (actor) {
            is SessionActor.CandidateActor -> testSessionRepository.save(
                TestSessionEntity(
                    candidate = actor.user,
                    category = category,
                    accessToken = accessToken,
                    status = TestSessionStatus.IN_PROGRESS,
                    createdAt = now,
                    updatedAt = now,
                    startedAt = now,
                ),
            )

            is SessionActor.GuestActor -> testSessionRepository.save(
                TestSessionEntity(
                    id = actor.sessionId,
                    candidate = null,
                    category = category,
                    accessToken = accessToken,
                    guestIdentifier = actor.guestName,
                    guestSessionKey = actor.guestKey,
                    status = TestSessionStatus.IN_PROGRESS,
                    createdAt = now,
                    updatedAt = now,
                    startedAt = now,
                ),
            )
        }

        accessToken?.testSession = session

        return CreateTestSessionResponse(
            sessionId = session.id,
            status = session.status.dbValue,
            createdAt = session.createdAt,
            startedAt = session.startedAt,
            category = category.toResponse(),
            guestSession = session.candidate == null,
            guestSessionKey = session.guestSessionKey,
        )
    }

    @Transactional
    fun getNextQuestion(sessionId: UUID, actor: SessionActor): NextQuestionResponse {
        val session = testSessionRepository.findById(sessionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Test session not found") }

        ensureSessionAccess(session, actor)

        val snapshots = questionSnapshotRepository.findBySessionIdOrderByQuestionOrderAsc(sessionId)
        val unansweredSnapshot = snapshots.lastOrNull { snapshot ->
            !answerRepository.existsByQuestionSnapshotId(snapshot.id)
        }

        if (unansweredSnapshot != null) {
            return NextQuestionResponse(
                sessionId = session.id,
                status = session.status.dbValue,
                hasNextQuestion = true,
                question = snapshotToQuestionDto(unansweredSnapshot),
            )
        }

        val askedQuestionIds = snapshots.mapNotNull { it.question?.id }.toSet()
        val sessionState = adaptiveSessionStateService.readState(session.adaptiveStateJson)

        val nextQuestion = adaptiveQuestionSelectionStrategy.selectNextQuestion(
            allActiveQuestions = questionRepository.findByIsActiveTrueAndCategoryIdOrderByPriorityDescDifficultyAscCreatedAtAsc(session.category.id),
            askedQuestionIds = askedQuestionIds,
            state = sessionState,
        )

        if (nextQuestion == null) {
            return NextQuestionResponse(
                sessionId = session.id,
                status = session.status.dbValue,
                hasNextQuestion = false,
                question = null,
            )
        }

        val sourceOptions = questionOptionRepository.findByQuestionIdOrderByOptionOrderAsc(nextQuestion.id)
        val generatedQuestion = aiAdaptiveQuestionService.generateQuestion(
            categoryName = session.category.name,
            targetScale = dominantScale(nextQuestion.scaleWeightsJson),
            sourceQuestionText = nextQuestion.text,
            difficulty = nextQuestion.difficulty.toInt(),
            options = sourceOptions.map { it.optionText },
        )

        val nextOrder = snapshots.size + 1
        val optionSnapshots = sourceOptions.mapIndexed { index, option ->
            SessionQuestionOptionDto(
                optionId = option.id,
                order = (index + 1).toShort(),
                text = generatedQuestion.options.getOrNull(index) ?: option.optionText,
            )
        }.shuffled().mapIndexed { index, option ->
            option.copy(order = (index + 1).toShort())
        }

        val snapshot = questionSnapshotRepository.save(
            QuestionSnapshotEntity(
                session = session,
                question = nextQuestion,
                questionOrder = nextOrder,
                questionText = generatedQuestion.text,
                scaleWeightsJson = nextQuestion.scaleWeightsJson,
                difficulty = nextQuestion.difficulty,
                priority = nextQuestion.priority,
                optionSnapshotsJson = objectMapper.writeValueAsString(optionSnapshots),
            ),
        )

        return NextQuestionResponse(
            sessionId = session.id,
            status = session.status.dbValue,
            hasNextQuestion = true,
            question = snapshotToQuestionDto(snapshot),
        )
    }

    @Transactional
    fun submitAnswer(sessionId: UUID, actor: SessionActor, request: SubmitAnswerRequest): SubmitAnswerResponse {
        val session = testSessionRepository.findById(sessionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Test session not found") }

        ensureSessionAccess(session, actor)

        if (session.status == TestSessionStatus.COMPLETED || session.status == TestSessionStatus.CANCELLED) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Cannot answer in completed or cancelled session")
        }

        val snapshotId = request.snapshotId ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "snapshotId is required")
        val selectedOptionId = request.selectedOptionId ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "selectedOptionId is required")

        val snapshot = questionSnapshotRepository.findByIdAndSessionId(snapshotId, sessionId)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Question snapshot was not issued in this session")

        if (answerRepository.existsByQuestionSnapshotId(snapshot.id)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Question already answered")
        }

        val questionId = snapshot.question?.id
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Snapshot has no source question")

        val selectedOption = questionOptionRepository.findById(selectedOptionId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected option not found") }

        if (selectedOption.question.id != questionId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected option does not belong to snapshot question")
        }

        answerRepository.save(
            AnswerEntity(
                session = session,
                questionSnapshot = snapshot,
                selectedOption = selectedOption,
                answerValue = selectedOption.contributionValue,
                responseTimeMs = request.responseTimeMs,
            ),
        )

        val previousState = adaptiveSessionStateService.readState(session.adaptiveStateJson)
        val updatedState = adaptiveSessionStateService.applyAnswer(previousState, snapshot, selectedOption.contributionValue)

        session.adaptiveStateJson = adaptiveSessionStateService.writeState(updatedState)
        session.updatedAt = OffsetDateTime.now()
        testSessionRepository.save(session)

        val totalAvailableQuestions = questionRepository.countByIsActiveTrueAndCategoryId(session.category.id)
        val effectiveMaxQuestions = minOf(totalAvailableQuestions, adaptiveQuestionSelectionStrategy.maxQuestionsPerSession)
        val issuedQuestions = questionSnapshotRepository.findBySessionIdOrderByQuestionOrderAsc(sessionId).size

        return SubmitAnswerResponse(
            success = true,
            sessionId = session.id,
            sessionStatus = session.status.dbValue,
            canContinue = session.status == TestSessionStatus.IN_PROGRESS && updatedState.answeredQuestions < effectiveMaxQuestions,
            progress = SessionProgressDto(
                answeredQuestions = updatedState.answeredQuestions,
                issuedQuestions = issuedQuestions,
                totalAvailableQuestions = totalAvailableQuestions,
                completionPercent = if (totalAvailableQuestions == 0) 0 else (updatedState.answeredQuestions * 100) / totalAvailableQuestions,
                cumulativeScore = updatedState.cumulativeScore,
                scaleCoverage = updatedState.scaleCoverage,
            ),
        )
    }

    @Transactional
    fun finishSession(sessionId: UUID, actor: SessionActor): ResultProfileResponse {
        val session = testSessionRepository.findById(sessionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Test session not found") }

        ensureSessionAccess(session, actor)

        if (session.status != TestSessionStatus.IN_PROGRESS) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Only active session can be finished")
        }

        if (resultProfileRepository.findBySessionId(session.id).isPresent) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Result profile already exists for session")
        }

        val answers = answerRepository.findBySessionId(session.id)
        val calculated = resultCalculationService.calculate(answers)
        val baseSummary = resultProfileMapper.buildOverallSummary(calculated)
        val aiSummary = aiResultInterpretationService.interpret(
            categoryName = session.category.name,
            profile = calculated,
            fallbackSummary = baseSummary,
        )
        val now = OffsetDateTime.now()

        val profile = resultProfileRepository.save(
            ResultProfileEntity(
                session = session,
                attentionScore = calculated.attention,
                stressResistanceScore = calculated.stressResistance,
                responsibilityScore = calculated.responsibility,
                adaptabilityScore = calculated.adaptability,
                decisionSpeedAccuracyScore = calculated.decisionSpeedAccuracy,
                summary = aiSummary,
                createdAt = now,
                updatedAt = now,
            ),
        )

        session.status = TestSessionStatus.COMPLETED
        session.completedAt = now
        session.updatedAt = now

        session.accessToken?.let { token ->
            if (!token.isUsed) {
                token.isUsed = true
                token.usedAt = now
                when (actor) {
                    is SessionActor.CandidateActor -> token.usedByUser = actor.user
                    is SessionActor.GuestActor -> token.usedByGuestDisplayName = actor.guestName ?: session.guestIdentifier
                }
            }
        }

        testSessionRepository.save(session)

        return resultProfileMapper.toResultProfile(profile)
    }

    @Transactional(readOnly = true)
    fun getMyResult(sessionId: UUID, userEmail: String): ResultProfileResponse {
        val user = getCandidateUser(userEmail)
        val session = testSessionRepository.findById(sessionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Test session not found") }

        ensureSessionAccess(session, SessionActor.CandidateActor(user))

        val resultProfile = resultProfileRepository.findBySessionId(sessionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Result profile not found") }

        return resultProfileMapper.toResultProfile(resultProfile)
    }

    @Transactional(readOnly = true)
    fun getMyResults(userEmail: String): List<MyResultListItemResponse> {
        val user = getCandidateUser(userEmail)
        return resultProfileRepository.findCompletedByCandidateIdOrderByCompletedAtDesc(
            candidateId = user.id,
            status = TestSessionStatus.COMPLETED,
        ).map { resultProfileMapper.toResultListItem(it) }
    }

    fun resolveCandidateActor(userEmail: String): SessionActor.CandidateActor = SessionActor.CandidateActor(getCandidateUser(userEmail))

    private fun snapshotToQuestionDto(snapshot: QuestionSnapshotEntity): SessionQuestionDto {
        val options = snapshot.optionSnapshotsJson
            ?.let { json -> runCatching { objectMapper.readValue(json, object : TypeReference<List<SessionQuestionOptionDto>>() {}) }.getOrNull() }
            ?: snapshot.question?.let { question ->
                questionOptionRepository.findByQuestionIdOrderByOptionOrderAsc(question.id).mapIndexed { index, option ->
                    SessionQuestionOptionDto(
                        optionId = option.id,
                        order = (index + 1).toShort(),
                        text = option.optionText,
                    )
                }
            }
            ?: emptyList()

        return SessionQuestionDto(
            snapshotId = snapshot.id,
            order = snapshot.questionOrder,
            text = snapshot.questionText,
            difficulty = snapshot.difficulty,
            options = options,
        )
    }

    private fun resolveDefaultCategoryId(): UUID =
        testCategoryRepository.findFirstByIsActiveTrueOrderByNameAsc()?.id
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No active test categories")

    private fun getCandidateUser(userEmail: String): UserEntity {
        val user = userRepository.findByEmail(userEmail)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }

        if (user.role.name != RoleName.CANDIDATE) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can use test session endpoints")
        }

        return user
    }

    private fun dominantScale(scaleWeightsJson: String): String {
        return runCatching {
            val weights = objectMapper.readValue(scaleWeightsJson, object : TypeReference<Map<String, BigDecimal>>() {})
            weights.maxByOrNull { it.value.abs() }?.key
        }.getOrNull() ?: "attention"
    }

    private fun ensureSessionAccess(session: TestSessionEntity, actor: SessionActor) {
        when (actor) {
            is SessionActor.CandidateActor -> {
                val owner = session.candidate ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Guest session")
                if (owner.id != actor.user.id) {
                    throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for this test session")
                }
            }

            is SessionActor.GuestActor -> {
                if (session.id != actor.sessionId || session.guestSessionKey != actor.guestKey) {
                    throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid guest session credentials")
                }
            }
        }
    }
}

private fun com.example.adaptivetestingbackend.entity.TestCategoryEntity.toResponse() = TestCategoryResponse(
    id = id,
    code = code,
    name = name,
    description = description,
)
