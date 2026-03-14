package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.dto.testsession.CreateTestSessionResponse
import com.example.adaptivetestingbackend.dto.testsession.NextQuestionResponse
import com.example.adaptivetestingbackend.dto.testsession.SessionProgressDto
import com.example.adaptivetestingbackend.dto.testsession.SessionQuestionDto
import com.example.adaptivetestingbackend.dto.testsession.SessionQuestionOptionDto
import com.example.adaptivetestingbackend.dto.testsession.SubmitAnswerRequest
import com.example.adaptivetestingbackend.dto.testsession.SubmitAnswerResponse
import com.example.adaptivetestingbackend.entity.AnswerEntity
import com.example.adaptivetestingbackend.entity.QuestionSnapshotEntity
import com.example.adaptivetestingbackend.entity.RoleName
import com.example.adaptivetestingbackend.entity.TestSessionEntity
import com.example.adaptivetestingbackend.entity.TestSessionStatus
import com.example.adaptivetestingbackend.entity.UserEntity
import com.example.adaptivetestingbackend.repository.AnswerRepository
import com.example.adaptivetestingbackend.repository.QuestionOptionRepository
import com.example.adaptivetestingbackend.repository.QuestionRepository
import com.example.adaptivetestingbackend.repository.QuestionSnapshotRepository
import com.example.adaptivetestingbackend.repository.TestSessionRepository
import com.example.adaptivetestingbackend.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
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
    private val adaptiveSessionStateService: AdaptiveSessionStateService,
    private val adaptiveQuestionSelectionStrategy: AdaptiveQuestionSelectionStrategy,
) {
    @Transactional
    fun createSession(userEmail: String): CreateTestSessionResponse {
        val user = getCandidateUser(userEmail)
        val now = OffsetDateTime.now()

        val session = testSessionRepository.save(
            TestSessionEntity(
                candidate = user,
                status = TestSessionStatus.IN_PROGRESS,
                createdAt = now,
                updatedAt = now,
                startedAt = now,
            ),
        )

        return CreateTestSessionResponse(
            sessionId = session.id,
            status = session.status.dbValue,
            createdAt = session.createdAt,
            startedAt = session.startedAt,
        )
    }

    @Transactional
    fun getNextQuestion(sessionId: UUID, userEmail: String): NextQuestionResponse {
        val user = getCandidateUser(userEmail)
        val session = testSessionRepository.findById(sessionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Test session not found") }

        ensureSessionOwner(session, user)

        val snapshots = questionSnapshotRepository.findBySessionIdOrderByQuestionOrderAsc(sessionId)
        val askedQuestionIds = snapshots.mapNotNull { it.question?.id }.toSet()
        val sessionState = adaptiveSessionStateService.readState(session.adaptiveStateJson)

        val nextQuestion = adaptiveQuestionSelectionStrategy.selectNextQuestion(
            allActiveQuestions = questionRepository.findByIsActiveTrueOrderByPriorityDescDifficultyAscCreatedAtAsc(),
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

        val nextOrder = snapshots.size + 1
        val optionSnapshots = questionOptionRepository.findByQuestionIdOrderByOptionOrderAsc(nextQuestion.id)
            .map {
                SessionQuestionOptionDto(
                    optionId = it.id,
                    order = it.optionOrder,
                    text = it.optionText,
                )
            }

        val snapshot = questionSnapshotRepository.save(
            QuestionSnapshotEntity(
                session = session,
                question = nextQuestion,
                questionOrder = nextOrder,
                questionText = nextQuestion.text,
                scaleWeightsJson = nextQuestion.scaleWeightsJson,
                difficulty = nextQuestion.difficulty,
                priority = nextQuestion.priority,
                optionSnapshotsJson = null,
            ),
        )

        return NextQuestionResponse(
            sessionId = session.id,
            status = session.status.dbValue,
            hasNextQuestion = true,
            question = SessionQuestionDto(
                snapshotId = snapshot.id,
                order = snapshot.questionOrder,
                text = snapshot.questionText,
                difficulty = snapshot.difficulty,
                options = optionSnapshots,
            ),
        )
    }

    @Transactional
    fun submitAnswer(sessionId: UUID, userEmail: String, request: SubmitAnswerRequest): SubmitAnswerResponse {
        val user = getCandidateUser(userEmail)
        val session = testSessionRepository.findById(sessionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Test session not found") }

        ensureSessionOwner(session, user)

        if (session.status == TestSessionStatus.COMPLETED || session.status == TestSessionStatus.CANCELLED) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Cannot answer in completed or cancelled session")
        }

        val snapshotId = request.snapshotId
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "snapshotId is required")
        val selectedOptionId = request.selectedOptionId
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "selectedOptionId is required")

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
            ),
        )

        val previousState = adaptiveSessionStateService.readState(session.adaptiveStateJson)
        val updatedState = adaptiveSessionStateService.applyAnswer(
            currentState = previousState,
            snapshot = snapshot,
            answerValue = selectedOption.contributionValue,
        )

        session.adaptiveStateJson = adaptiveSessionStateService.writeState(updatedState)
        session.updatedAt = OffsetDateTime.now()
        testSessionRepository.save(session)

        val totalAvailableQuestions = questionRepository.countByIsActiveTrue()
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

    private fun getCandidateUser(userEmail: String): UserEntity {
        val user = userRepository.findByEmail(userEmail)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }

        if (user.role.name != RoleName.CANDIDATE) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can use test session endpoints")
        }

        return user
    }

    private fun ensureSessionOwner(session: TestSessionEntity, user: UserEntity) {
        if (session.candidate.id != user.id) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for this test session")
        }
    }
}
