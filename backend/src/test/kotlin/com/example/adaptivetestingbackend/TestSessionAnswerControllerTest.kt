package com.example.adaptivetestingbackend

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class TestSessionAnswerControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @Test
    fun `candidate can submit answer and receive progress`() {
        val token = registerAndLogin(email = "candidate1@example.com", role = "candidate")

        val sessionId = mockMvc.post("/test-sessions") {
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("sessionId").asText() }

        val questionJson = mockMvc.get("/test-sessions/$sessionId/next-question") {
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("question") }

        val snapshotId = questionJson.get("snapshotId").asText()
        val selectedOptionId = questionJson.get("options").get(0).get("optionId").asText()

        val answerBody = mapOf(
            "snapshotId" to snapshotId,
            "selectedOptionId" to selectedOptionId,
        )

        mockMvc.post("/test-sessions/$sessionId/answers") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(answerBody)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.canContinue") { value(true) }
                jsonPath("$.progress.answeredQuestions") { value(1) }
                jsonPath("$.progress.issuedQuestions") { value(1) }
                jsonPath("$.progress.totalAvailableQuestions") { value(10) }
            }
    }

    @Test
    fun `cannot answer the same snapshot twice`() {
        val token = registerAndLogin(email = "candidate2@example.com", role = "candidate")

        val sessionId = mockMvc.post("/test-sessions") {
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("sessionId").asText() }

        val questionJson = mockMvc.get("/test-sessions/$sessionId/next-question") {
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("question") }

        val answerBody = mapOf(
            "snapshotId" to questionJson.get("snapshotId").asText(),
            "selectedOptionId" to questionJson.get("options").get(1).get("optionId").asText(),
        )

        repeat(2) { attempt ->
            val result = mockMvc.post("/test-sessions/$sessionId/answers") {
                header("Authorization", "Bearer $token")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(answerBody)
            }

            if (attempt == 0) {
                result.andExpect { status { isOk() } }
            } else {
                result.andExpect { status { isConflict() } }
            }
        }
    }

    @Test
    fun `candidate cannot answer another user's session`() {
        val ownerToken = registerAndLogin(email = "candidate3@example.com", role = "candidate")
        val intruderToken = registerAndLogin(email = "candidate4@example.com", role = "candidate")

        val sessionId = mockMvc.post("/test-sessions") {
            header("Authorization", "Bearer $ownerToken")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("sessionId").asText() }

        val questionJson = mockMvc.get("/test-sessions/$sessionId/next-question") {
            header("Authorization", "Bearer $ownerToken")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("question") }

        val answerBody = mapOf(
            "snapshotId" to questionJson.get("snapshotId").asText(),
            "selectedOptionId" to questionJson.get("options").get(0).get("optionId").asText(),
        )

        mockMvc.post("/test-sessions/$sessionId/answers") {
            header("Authorization", "Bearer $intruderToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(answerBody)
        }
            .andExpect { status { isForbidden() } }
    }



    @Test
    fun `candidate can finish active session and retrieve result`() {
        val token = registerAndLogin(email = "candidate5@example.com", role = "candidate")

        val sessionId = mockMvc.post("/test-sessions") {
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("sessionId").asText() }

        val questionJson = mockMvc.get("/test-sessions/$sessionId/next-question") {
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("question") }

        val answerBody = mapOf(
            "snapshotId" to questionJson.get("snapshotId").asText(),
            "selectedOptionId" to questionJson.get("options").get(4).get("optionId").asText(),
        )

        mockMvc.post("/test-sessions/$sessionId/answers") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(answerBody)
        }
            .andExpect { status { isOk() } }

        mockMvc.post("/test-sessions/$sessionId/finish") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.sessionId") { value(sessionId) }
                jsonPath("$.scores.attention") { exists() }
                jsonPath("$.interpretations.attention") { exists() }
                jsonPath("$.overallSummary") { exists() }
            }

        mockMvc.get("/me/results/$sessionId") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.sessionId") { value(sessionId) }
                jsonPath("$.scores.responsibility") { exists() }
                jsonPath("$.interpretations.responsibility") { exists() }
            }
    }

    @Test
    fun `cannot finish someone else's session`() {
        val ownerToken = registerAndLogin(email = "candidate6@example.com", role = "candidate")
        val intruderToken = registerAndLogin(email = "candidate7@example.com", role = "candidate")

        val sessionId = mockMvc.post("/test-sessions") {
            header("Authorization", "Bearer $ownerToken")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("sessionId").asText() }

        mockMvc.post("/test-sessions/$sessionId/finish") {
            header("Authorization", "Bearer $intruderToken")
        }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `cannot finish already completed session`() {
        val token = registerAndLogin(email = "candidate8@example.com", role = "candidate")

        val sessionId = mockMvc.post("/test-sessions") {
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("sessionId").asText() }

        mockMvc.post("/test-sessions/$sessionId/finish") {
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isOk() } }

        mockMvc.post("/test-sessions/$sessionId/finish") {
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isConflict() } }
    }

    @Test
    fun `candidate can get own completed results history sorted desc`() {
        val token = registerAndLogin(email = "candidate9@example.com", role = "candidate")

        val firstSessionId = mockMvc.post("/test-sessions") {
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("sessionId").asText() }

        mockMvc.post("/test-sessions/$firstSessionId/finish") {
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isOk() } }

        Thread.sleep(10)

        val secondSessionId = mockMvc.post("/test-sessions") {
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("sessionId").asText() }

        mockMvc.post("/test-sessions/$secondSessionId/finish") {
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isOk() } }

        mockMvc.get("/me/results") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$[0].sessionId") { value(secondSessionId) }
                jsonPath("$[1].sessionId") { value(firstSessionId) }
                jsonPath("$[0].completedAt") { exists() }
                jsonPath("$[0].summary") { exists() }
                jsonPath("$[0].scores.attention") { exists() }
                jsonPath("$[0].scores.stressResistance") { exists() }
                jsonPath("$[0].scores.responsibility") { exists() }
                jsonPath("$[0].scores.adaptability") { exists() }
                jsonPath("$[0].scores.decisionSpeedAccuracy") { exists() }
            }
    }

    private fun registerAndLogin(email: String, role: String): String {
        val registerBody = mapOf(
            "fullName" to "Test User",
            "email" to email,
            "password" to "password123",
            "role" to role,
        )

        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(registerBody)
        }
            .andExpect { status { isOk() } }

        val loginBody = mapOf("email" to email, "password" to "password123")

        return mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginBody)
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("token").asText() }
    }
}
