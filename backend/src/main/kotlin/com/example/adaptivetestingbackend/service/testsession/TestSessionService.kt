package com.example.adaptivetestingbackend.service.testsession

// imports trimmed for brevity (unchanged)

import com.example.adaptivetestingbackend.service.ai.AiResultInterpretationService

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

    // ... остальной код без изменений

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
}
