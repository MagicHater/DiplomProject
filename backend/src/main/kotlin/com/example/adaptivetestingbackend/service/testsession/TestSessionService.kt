package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.dto.testsession.CreateTestSessionResponse
import com.example.adaptivetestingbackend.dto.testsession.NextQuestionResponse
import com.example.adaptivetestingbackend.dto.testsession.SessionQuestionDto
import com.example.adaptivetestingbackend.dto.testsession.SessionQuestionOptionDto
import com.example.adaptivetestingbackend.entity.QuestionSnapshotEntity
import com.example.adaptivetestingbackend.entity.RoleName
import com.example.adaptivetestingbackend.entity.TestSessionEntity
import com.example.adaptivetestingbackend.entity.TestSessionStatus
import com.example.adaptivetestingbackend.entity.UserEntity
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

        val nextQuestion = questionRepository.findByIsActiveTrueOrderByPriorityDescDifficultyAscCreatedAtAsc()
            .firstOrNull { it.id !in askedQuestionIds }

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
