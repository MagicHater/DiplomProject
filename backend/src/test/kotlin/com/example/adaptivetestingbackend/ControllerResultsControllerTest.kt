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
class ControllerResultsControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @Test
    fun `controller can list candidates and view candidate completed sessions`() {
        val candidateToken = registerAndLogin(email = "candidate-controller-1@example.com", role = "candidate")
        val controllerToken = registerAndLogin(email = "controller1@example.com", role = "controller")

        val sessionId = createAndFinishSession(candidateToken)

        val candidatesResponse = mockMvc.get("/controller/candidates") {
            header("Authorization", "Bearer $controllerToken")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$[0].candidateId") { exists() }
                jsonPath("$[0].fullName") { exists() }
                jsonPath("$[0].email") { exists() }
                jsonPath("$[0].completedSessionsCount") { exists() }
            }
            .andReturn().response.contentAsString

        val candidateId = objectMapper.readTree(candidatesResponse)
            .first { it.get("email").asText() == "candidate-controller-1@example.com" }
            .get("candidateId")
            .asText()

        mockMvc.get("/controller/candidates/$candidateId/results") {
            header("Authorization", "Bearer $controllerToken")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.candidateId") { value(candidateId) }
                jsonPath("$.fullName") { exists() }
                jsonPath("$.sessions[0].sessionId") { value(sessionId) }
            }
    }

    @Test
    fun `controller can open result details by session id`() {
        val candidateToken = registerAndLogin(email = "candidate-controller-2@example.com", role = "candidate")
        val controllerToken = registerAndLogin(email = "controller2@example.com", role = "controller")

        val sessionId = createAndFinishSession(candidateToken)

        mockMvc.get("/controller/results/$sessionId") {
            header("Authorization", "Bearer $controllerToken")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.sessionId") { value(sessionId) }
                jsonPath("$.scores.attention") { exists() }
                jsonPath("$.interpretations.attention") { exists() }
                jsonPath("$.overallSummary") { exists() }
            }
    }

    @Test
    fun `candidate cannot access controller endpoints`() {
        val candidateToken = registerAndLogin(email = "candidate-controller-3@example.com", role = "candidate")

        mockMvc.get("/controller/candidates") {
            header("Authorization", "Bearer $candidateToken")
        }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `controller gets not found for unknown candidate`() {
        val controllerToken = registerAndLogin(email = "controller3@example.com", role = "controller")

        mockMvc.get("/controller/candidates/00000000-0000-0000-0000-000000000000/results") {
            header("Authorization", "Bearer $controllerToken")
        }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Candidate not found: 00000000-0000-0000-0000-000000000000") }
            }
    }

    private fun createAndFinishSession(candidateToken: String): String {
        val sessionId = mockMvc.post("/test-sessions") {
            header("Authorization", "Bearer $candidateToken")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("sessionId").asText() }

        mockMvc.post("/test-sessions/$sessionId/finish") {
            header("Authorization", "Bearer $candidateToken")
        }
            .andExpect { status { isOk() } }

        return sessionId
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
