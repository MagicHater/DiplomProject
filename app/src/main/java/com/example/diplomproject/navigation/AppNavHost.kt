package com.example.diplomproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.diplomproject.domain.model.UserRole
import com.example.diplomproject.ui.screens.AuthViewModel
import com.example.diplomproject.ui.screens.AppSessionState
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
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by authViewModel.uiState.collectAsState()
    val authenticatedRole = (authState.appSessionState as? AppSessionState.Authenticated)?.role

    LaunchedEffect(Unit) {
        authViewModel.checkSavedSession()
    }

    LaunchedEffect(authState.appSessionState) {
        val destination = when (val sessionState = authState.appSessionState) {
            AppSessionState.Initializing -> return@LaunchedEffect
            AppSessionState.Unauthenticated -> AppDestination.Login.route
            is AppSessionState.Authenticated -> {
                if (sessionState.role == UserRole.Controller) {
                    AppDestination.ControllerHome.route
                } else {
                    AppDestination.CandidateHome.route
                }
            }
        }

        navController.navigate(destination) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    if (authState.appSessionState is AppSessionState.Initializing) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route,
    ) {
        composable(AppDestination.Login.route) {
            LoginScreen(
                onRegisterClick = { navController.navigate(AppDestination.Register.route) },
                viewModel = authViewModel,
            )
        }

        composable(AppDestination.Register.route) {
            RegisterScreen(
                onLoginClick = { navController.navigate(AppDestination.Login.route) },
                viewModel = authViewModel,
            )
        }

        composable(AppDestination.CandidateHome.route) {
            CandidateHomeScreen(
                onStartTestClick = { navController.navigate(AppDestination.Test.route) },
                onResultClick = { navController.navigate(AppDestination.Result.route) },
                onHistoryClick = { navController.navigate(AppDestination.History.route) },
                onLogoutClick = {
                    authViewModel.logout()
                },
            )
        }

        composable(AppDestination.ControllerHome.route) {
            ControllerHomeScreen(
                onCandidateListClick = { navController.navigate(AppDestination.CandidateList.route) },
                onHistoryClick = { navController.navigate(AppDestination.History.route) },
                onLogoutClick = {
                    authViewModel.logout()
                },
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
            val homeRoute = if (authenticatedRole == UserRole.Controller) {
                AppDestination.ControllerHome.route
            } else {
                AppDestination.CandidateHome.route
            }

            HistoryScreen(
                onBackToHomeClick = {
                    navController.navigate(homeRoute) {
                        launchSingleTop = true
                    }
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
