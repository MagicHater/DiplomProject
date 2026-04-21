package com.example.diplomproject.domain.repository

import com.example.diplomproject.domain.model.CandidateResultHistoryItem
import com.example.diplomproject.domain.model.ControllerTestDraft
import com.example.diplomproject.domain.model.ControllerTokenItem
import com.example.diplomproject.domain.model.ControllerParticipantListItem
import com.example.diplomproject.domain.model.ControllerParticipantResults
import com.example.diplomproject.domain.model.ControllerTokenResultHistoryItem
import com.example.diplomproject.domain.model.CustomTestDetails
import com.example.diplomproject.domain.model.CustomTestDraft
import com.example.diplomproject.domain.model.CustomTestListItem
import com.example.diplomproject.domain.model.CustomTestResultItem
import com.example.diplomproject.domain.model.CustomTestStatistics
import com.example.diplomproject.domain.model.CustomTestSubmissionDraft
import com.example.diplomproject.domain.model.FinishedSessionResult
import com.example.diplomproject.domain.model.NextQuestionPayload
import com.example.diplomproject.domain.model.SubmitAnswerResult
import com.example.diplomproject.domain.model.TestCategory
import com.example.diplomproject.domain.model.TokenSessionStartResult
import com.example.diplomproject.domain.model.TokenPreview

interface TestSessionRepository {
    suspend fun getCategories(): List<TestCategory>
    suspend fun createSession(categoryId: String): String
    suspend fun previewToken(token: String): TokenPreview
    suspend fun startGuestByToken(token: String, guestName: String): TokenSessionStartResult
    suspend fun startCandidateByToken(token: String): String
    suspend fun createControllerToken(categoryId: String): ControllerTokenItem
    suspend fun createControllerTest(draft: ControllerTestDraft): TestCategory
    suspend fun getControllerTokens(): List<ControllerTokenItem>
    suspend fun getControllerTokenResults(): List<ControllerTokenResultHistoryItem>
    suspend fun getControllerParticipants(): List<ControllerParticipantListItem>
    suspend fun getControllerParticipantResults(participantType: String, participantKey: String): ControllerParticipantResults
    suspend fun getAvailableCustomTests(): List<CustomTestListItem>
    suspend fun createCustomTest(draft: CustomTestDraft): String
    suspend fun getMyCustomTests(): List<CustomTestListItem>
    suspend fun getCustomTestDetails(testId: String): CustomTestDetails
    suspend fun submitCustomTest(testId: String, submission: CustomTestSubmissionDraft)
    suspend fun getCustomTestResults(testId: String): List<CustomTestResultItem>
    suspend fun getCustomTestStatistics(testId: String): CustomTestStatistics
    suspend fun getNextQuestion(sessionId: String): NextQuestionPayload
    suspend fun submitAnswer(sessionId: String, snapshotId: String, selectedOptionId: String): SubmitAnswerResult
    suspend fun getMyResults(): List<CandidateResultHistoryItem>
    suspend fun getResult(sessionId: String): FinishedSessionResult
    suspend fun finishSession(sessionId: String): FinishedSessionResult
}
