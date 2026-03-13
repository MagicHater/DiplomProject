package com.example.diplomproject.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.diplomproject.domain.model.UserRole
import com.example.diplomproject.ui.screens.CandidateDetailsScreen
import com.example.diplomproject.ui.screens.CandidateHomeScreen
import com.example.diplomproject.ui.screens.CandidateListScreen
import com.example.diplomproject.ui.screens.ControllerHomeScreen
import com.example.diplomproject.ui.screens.HistoryScreen
import com.example.diplomproject.ui.screens.LoginScreen
import com.example.diplomproject.ui.screens.RegisterScreen
import com.example.diplomproject.ui.screens.ResultScreen
import com.example.diplomproject.ui.screens.TestScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route,
    ) {
        composable(AppDestination.Login.route) {
            LoginScreen(
                onRegisterClick = { navController.navigate(AppDestination.Register.route) },
                onLoginSuccess = { role ->
                    val destination = if (role == UserRole.Controller) {
                        AppDestination.ControllerHome.route
                    } else {
                        AppDestination.CandidateHome.route
                    }
                    navController.navigate(destination)
                },
            )
        }

        composable(AppDestination.Register.route) {
            RegisterScreen(onLoginClick = { navController.navigate(AppDestination.Login.route) })
        }

        composable(AppDestination.CandidateHome.route) {
            CandidateHomeScreen(
                onStartTestClick = { navController.navigate(AppDestination.Test.route) },
                onResultClick = { navController.navigate(AppDestination.Result.route) },
                onHistoryClick = { navController.navigate(AppDestination.History.route) },
                onLogoutClick = { navController.navigate(AppDestination.Login.route) },
            )
        }

        composable(AppDestination.ControllerHome.route) {
            ControllerHomeScreen(
                onCandidateListClick = { navController.navigate(AppDestination.CandidateList.route) },
                onHistoryClick = { navController.navigate(AppDestination.History.route) },
                onLogoutClick = { navController.navigate(AppDestination.Login.route) },
            )
        }

        composable(AppDestination.Test.route) {
            TestScreen(
                onFinishTestClick = { navController.navigate(AppDestination.Result.route) },
                onBackToHomeClick = { navController.navigate(AppDestination.CandidateHome.route) },
            )
        }

        composable(AppDestination.Result.route) {
            ResultScreen(
                onHistoryClick = { navController.navigate(AppDestination.History.route) },
                onBackToCandidateHomeClick = {
                    navController.navigate(AppDestination.CandidateHome.route)
                },
            )
        }

        composable(AppDestination.History.route) {
            HistoryScreen(
                onCandidateHomeClick = {
                    navController.navigate(AppDestination.CandidateHome.route)
                },
                onControllerHomeClick = {
                    navController.navigate(AppDestination.ControllerHome.route)
                },
            )
        }

        composable(AppDestination.CandidateList.route) {
            CandidateListScreen(
                onCandidateDetailsClick = {
                    navController.navigate(AppDestination.CandidateDetails.route)
                },
                onBackToControllerHomeClick = {
                    navController.navigate(AppDestination.ControllerHome.route)
                },
            )
        }

        composable(AppDestination.CandidateDetails.route) {
            CandidateDetailsScreen(
                onBackToCandidateListClick = {
                    navController.navigate(AppDestination.CandidateList.route)
                },
            )
        }
    }
}
