package com.example.diplomproject.navigation

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object Register : AppDestination("register")
    data object CandidateHome : AppDestination("candidate_home")
    data object ControllerHome : AppDestination("controller_home")
    data object ControllerDashboard : AppDestination("controller_dashboard")
    data object Test : AppDestination("test/{sessionId}") {
        const val sessionIdArg = "sessionId"
        fun createRoute(sessionId: String): String = "test/$sessionId"
    }

    data object Result : AppDestination("result/{sessionId}") {
        const val sessionIdArg = "sessionId"
        fun createRoute(sessionId: String): String = "result/$sessionId"
    }

    data object GuestComplete : AppDestination("guest_complete")
    data object History : AppDestination("history")
    data object CandidateList : AppDestination("candidate_list")
    data object ControllerCreateTest : AppDestination("controller_create_test")
    data object ControllerCustomTests : AppDestination("controller_custom_tests")
    data object ControllerCustomTestDetails : AppDestination("controller_custom_test_details/{testId}") {
        const val testIdArg = "testId"
        fun createRoute(testId: String): String = "controller_custom_test_details/$testId"
    }

    data object ControllerCustomTestResults : AppDestination("controller_custom_test_results/{testId}") {
        const val testIdArg = "testId"
        fun createRoute(testId: String): String = "controller_custom_test_results/$testId"
    }

    data object ControllerCustomTestStatistics : AppDestination("controller_custom_test_statistics/{testId}") {
        const val testIdArg = "testId"
        fun createRoute(testId: String): String = "controller_custom_test_statistics/$testId"
    }

    data object CandidateCustomTests : AppDestination("candidate_custom_tests")
    data object CandidateCustomTestPass : AppDestination("candidate_custom_test_pass/{testId}") {
        const val testIdArg = "testId"
        fun createRoute(testId: String): String = "candidate_custom_test_pass/$testId"
    }

    data object CandidateDetails : AppDestination("candidate_details/{participantType}/{participantKey}") {
        const val participantTypeArg = "participantType"
        const val participantKeyArg = "participantKey"
        fun createRoute(participantType: String, participantKey: String): String =
            "candidate_details/$participantType/$participantKey"
    }
}
