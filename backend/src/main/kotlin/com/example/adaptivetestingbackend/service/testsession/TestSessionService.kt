package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.dto.testsession.*
import com.example.adaptivetestingbackend.entity.*
import com.example.adaptivetestingbackend.repository.*
import com.example.adaptivetestingbackend.service.ai.AiAdaptiveQuestionService
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
    private val resultProfileRepository: ResultProfileRepository,
    private val adaptiveSessionStateService: AdaptiveSessionStateService,
    private val adaptiveQuestionSelectionStrategy: AdaptiveQuestionSelectionStrategy,
    private val resultCalculationService: ResultCalculationService,
    private val resultProfileMapper: ResultProfileMapper,
    private val testCategoryRepository: TestCategoryRepository,
    private val aiAdaptiveQuestionService: AiAdaptiveQuestionService,
) {

    @Transactional
    fun getNextQuestion(sessionId: UUID, actor: SessionActor): NextQuestionResponse {
        val session = testSessionRepository.findById(sessionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Test session not found") }

        ensureSessionAccess(session, actor)

        val snapshots = questionSnapshotRepository.findBySessionIdOrderByQuestionOrderAsc(sessionId)
        val askedQuestionIds = snapshots.mapNotNull { it.question?.id }.toSet()
        val sessionState = adaptiveSessionStateService.readState(session.adaptiveStateJson)

        val nextQuestion = adaptiveQuestionSelectionStrategy.selectNextQuestion(
            allActiveQuestions = questionRepository.findByIsActiveTrueAndCategoryIdOrderByPriorityDescDifficultyAscCreatedAtAsc(session.category.id),
            askedQuestionIds = askedQuestionIds,
            state = sessionState,
        ) ?: return NextQuestionResponse(session.id, session.status.dbValue, false, null)

        val sourceOptions = questionOptionRepository.findByQuestionIdOrderByOptionOrderAsc(nextQuestion.id)

        val generated = aiAdaptiveQuestionService.generateQuestion(
            categoryName = session.category.name,
            sourceQuestionText = nextQuestion.text,
            difficulty = nextQuestion.difficulty,
            options = sourceOptions.map { it.optionText },
        )

        val optionSnapshots = sourceOptions.mapIndexed { index, option ->
            SessionQuestionOptionDto(
                optionId = option.id,
                order = option.optionOrder,
                text = generated.options.getOrNull(index) ?: option.optionText,
            )
        }

        val snapshot = questionSnapshotRepository.save(
            QuestionSnapshotEntity(
                session = session,
                question = nextQuestion,
                questionOrder = snapshots.size + 1,
                questionText = generated.text,
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

    // остальной код без изменений (не трогаем submitAnswer, finish и т.д.)
}
