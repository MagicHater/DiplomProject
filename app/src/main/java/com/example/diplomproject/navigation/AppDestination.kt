package com.example.diplomproject.navigation

sealed class AppDestination(val route: String) {
    data object RoleSelection : AppDestination("role_selection")
    data object Candidate : AppDestination("candidate")
    data object Controller : AppDestination("controller")
}
