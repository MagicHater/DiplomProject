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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.diplomproject.domain.model.TestQuestion
import com.example.diplomproject.domain.model.UserRole
import com.example.diplomproject.ui.screens.AuthViewModel
import com.example.diplomproject.ui.screens.AppSessionState
import com.example.diplomproject.ui.screens.CandidateDetailsScreen
import com.example.diplomproject.ui.screens.CandidateHomeScreen
import com.example.diplomproject.ui.screens.CandidateHomeViewModel
import com.example.diplomproject.ui.screens.CandidateListScreen
import com.example.diplomproject.ui.screens.ControllerHomeScreen
import com.example.diplomproject.ui.screens.HistoryScreen
import com.example.diplomproject.ui.screens.HistoryViewModel
import com.example.diplomproject.ui.screens.LoginScreen
import com.example.diplomproject.ui.screens.RegisterScreen
import com.example.diplomproject.ui.screens.ResultScreen
import com.example.diplomproject.ui.screens.ResultViewModel
import com.example.diplomproject.ui.screens.TestQuestionScreen
import com.example.diplomproject.ui.screens.TestViewModel

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
            val candidateHomeViewModel: CandidateHomeViewModel = hiltViewModel()
            val candidateHomeState by candidateHomeViewModel.uiState.collectAsState()

            LaunchedEffect(candidateHomeState.startedSession?.sessionId) {
                val startedSession = candidateHomeState.startedSession ?: return@LaunchedEffect
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("initialQuestion", startedSession.firstQuestion)
                navController.navigate(AppDestination.Test.createRoute(startedSession.sessionId))
                candidateHomeViewModel.consumeNavigation()
            }

            CandidateHomeScreen(
                uiState = candidateHomeState,
                onStartTestClick = { candidateHomeViewModel.startTest() },
                onResultClick = { navController.navigate(AppDestination.History.route) },
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

        composable(
            route = AppDestination.Test.route,
            arguments = listOf(navArgument(AppDestination.Test.sessionIdArg) { type = NavType.StringType }),
        ) {
            val testViewModel: TestViewModel = hiltViewModel()
            val testUiState by testViewModel.uiState.collectAsState()
            val initialQuestion = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<TestQuestion>("initialQuestion")

            LaunchedEffect(initialQuestion?.snapshotId) {
                initialQuestion?.let(testViewModel::setInitialQuestion)
            }

            LaunchedEffect(testUiState.navigateToResult) {
                if (testUiState.navigateToResult) {
                    val finishedSessionId = testUiState.finishResult?.sessionId ?: return@LaunchedEffect
                    navController.navigate(AppDestination.Result.createRoute(finishedSessionId))
                    testViewModel.consumeResultNavigation()
                }
            }

            TestQuestionScreen(
                uiState = testUiState,
                onOptionSelected = testViewModel::onOptionSelected,
                onNextClick = testViewModel::onNextClick,
                onRetryClick = testViewModel::retryLoad,
            )
        }

        composable(
            route = AppDestination.Result.route,
            arguments = listOf(navArgument(AppDestination.Result.sessionIdArg) { type = NavType.StringType }),
        ) {
            val resultViewModel: ResultViewModel = hiltViewModel()
            val resultUiState by resultViewModel.uiState.collectAsState()

            ResultScreen(
                uiState = resultUiState,
                onRetryClick = resultViewModel::load,
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
            val historyViewModel: HistoryViewModel = hiltViewModel()
            val historyUiState by historyViewModel.uiState.collectAsState()

            LaunchedEffect(authenticatedRole) {
                historyViewModel.load(authenticatedRole)
            }

            HistoryScreen(
                uiState = historyUiState,
                onResultClick = { sessionId ->
                    navController.navigate(AppDestination.Result.createRoute(sessionId))
                },
                onBackToHomeClick = {
                    navController.navigate(homeRoute) {
                        launchSingleTop = true
                    }
                },
                onRetryClick = { historyViewModel.load(authenticatedRole) },
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
