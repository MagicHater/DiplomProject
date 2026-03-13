package com.example.diplomproject.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.diplomproject.ui.screens.CandidateScreen
import com.example.diplomproject.ui.screens.ControllerScreen
import com.example.diplomproject.ui.screens.RoleSelectionScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestination.RoleSelection.route,
    ) {
        composable(AppDestination.RoleSelection.route) {
            RoleSelectionScreen(
                onCandidateClick = { navController.navigate(AppDestination.Candidate.route) },
                onControllerClick = { navController.navigate(AppDestination.Controller.route) },
            )
        }
        composable(AppDestination.Candidate.route) {
            CandidateScreen()
        }
        composable(AppDestination.Controller.route) {
            ControllerScreen()
        }
    }
}
