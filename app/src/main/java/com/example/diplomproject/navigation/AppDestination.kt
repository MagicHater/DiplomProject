package com.example.diplomproject.navigation

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object Register : AppDestination("register")
    data object CandidateHome : AppDestination("candidate_home")
    data object ControllerHome : AppDestination("controller_home")
    data object Test : AppDestination("test/{sessionId}") {
        const val sessionIdArg = "sessionId"

        fun createRoute(sessionId: String): String = "test/$sessionId"
    }
    data object Result : AppDestination("result")
    data object History : AppDestination("history")
    data object CandidateList : AppDestination("candidate_list")
    data object CandidateDetails : AppDestination("candidate_details")
}
