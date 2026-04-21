package com.example.diplomproject.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.diplomproject.domain.model.FinishedSessionResult
import com.example.diplomproject.domain.model.TestQuestion
import com.example.diplomproject.domain.model.UserRole
import com.example.diplomproject.ui.screens.AppSessionState
import com.example.diplomproject.ui.screens.AuthViewModel
import com.example.diplomproject.ui.screens.CandidateDetailsScreen
import com.example.diplomproject.ui.screens.CandidateDetailsViewModel
import com.example.diplomproject.ui.screens.CandidateHomeScreen
import com.example.diplomproject.ui.screens.CandidateHomeViewModel
import com.example.diplomproject.ui.screens.CandidateListScreen
import com.example.diplomproject.ui.screens.CandidateListViewModel
import com.example.diplomproject.ui.screens.ControllerCreateTestScreen
import com.example.diplomproject.ui.screens.ControllerCreateTestViewModel
import com.example.diplomproject.ui.screens.ControllerHomeScreen
import com.example.diplomproject.ui.screens.ControllerHomeViewModel
import com.example.diplomproject.ui.screens.GuestCompletionScreen
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
            LaunchedEffect(authState.guestStartedSession?.sessionId) {
                val startedSession = authState.guestStartedSession ?: return@LaunchedEffect
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("initialQuestion", startedSession.firstQuestion)
                navController.navigate(AppDestination.Test.createRoute(startedSession.sessionId))
                authViewModel.consumeGuestNavigation()
            }

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
                onCategorySelected = candidateHomeViewModel::onCategorySelected,
                onTokenInputChanged = candidateHomeViewModel::onTokenChanged,
                onStartByTokenClick = candidateHomeViewModel::startByToken,
                onResultClick = { navController.navigate(AppDestination.History.route) },
                onHistoryClick = { navController.navigate(AppDestination.History.route) },
                onLogoutClick = { authViewModel.logout() },
            )
        }

        composable(AppDestination.ControllerHome.route) {
            val controllerViewModel: ControllerHomeViewModel = hiltViewModel()
            val controllerUiState by controllerViewModel.uiState.collectAsState()

            val shouldReloadCategories = navController.currentBackStackEntry
                ?.savedStateHandle
                ?.get<Boolean>("controller_reload_categories") == true
            if (shouldReloadCategories) {
                controllerViewModel.load()
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("controller_reload_categories", false)
            }

            ControllerHomeScreen(
                uiState = controllerUiState,
                onCategorySelected = controllerViewModel::onCategorySelected,
                onGenerateTokenClick = controllerViewModel::generateToken,
                onCreateTestClick = { navController.navigate(AppDestination.ControllerCreateTest.route) },
                onCandidateListClick = { navController.navigate(AppDestination.CandidateList.route) },
                onHistoryClick = { navController.navigate(AppDestination.History.route) },
                onLogoutClick = { authViewModel.logout() },
            )
        }

        composable(AppDestination.ControllerCreateTest.route) {
            val createTestViewModel: ControllerCreateTestViewModel = hiltViewModel()
            val createTestUiState by createTestViewModel.uiState.collectAsState()

            ControllerCreateTestScreen(
                uiState = createTestUiState,
                onBackClick = { navController.popBackStack() },
                onNameChanged = createTestViewModel::onNameChanged,
                onDescriptionChanged = createTestViewModel::onDescriptionChanged,
                onAddQuestion = createTestViewModel::addQuestion,
                onRemoveQuestion = createTestViewModel::removeQuestion,
                onQuestionTextChanged = createTestViewModel::onQuestionTextChanged,
                onAddOption = createTestViewModel::addOption,
                onRemoveOption = createTestViewModel::removeOption,
                onOptionTextChanged = createTestViewModel::onOptionTextChanged,
                onOptionOrderChanged = createTestViewModel::onOptionOrderChanged,
                onOptionContributionChanged = createTestViewModel::onOptionContributionChanged,
                onOptionScaleChanged = createTestViewModel::onOptionScaleChanged,
                onSaveClick = {
                    createTestViewModel.saveTest {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("controller_reload_categories", true)
                        navController.popBackStack()
                    }
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
                if (initialQuestion != null) {
                    testViewModel.setInitialQuestion(initialQuestion)
                } else {
                    testViewModel.retryLoad()
                }
            }

            LaunchedEffect(testUiState.navigateToResult) {
                if (testUiState.navigateToResult) {
                    val finishedResult = testUiState.finishResult ?: return@LaunchedEffect

                    if (authenticatedRole == null) {
                        navController.navigate(AppDestination.GuestComplete.route) {
                            popUpTo(AppDestination.Test.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    } else {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("prefetchedResult", finishedResult)

                        navController.navigate(AppDestination.Result.createRoute(finishedResult.sessionId)) {
                            popUpTo(AppDestination.Test.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }

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

        composable(AppDestination.GuestComplete.route) {
            GuestCompletionScreen(
                onExitClick = {
                    authViewModel.logout()
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(
            route = AppDestination.Result.route,
            arguments = listOf(navArgument(AppDestination.Result.sessionIdArg) { type = NavType.StringType }),
        ) {
            val resultViewModel: ResultViewModel = hiltViewModel()
            val resultUiState by resultViewModel.uiState.collectAsState()

            val prefetchedResult = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<FinishedSessionResult>("prefetchedResult")

            LaunchedEffect(prefetchedResult?.sessionId) {
                if (prefetchedResult != null) {
                    resultViewModel.setPrefetchedResult(prefetchedResult)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.remove<FinishedSessionResult>("prefetchedResult")
                }
            }

            ResultScreen(
                uiState = resultUiState,
                onRetryClick = resultViewModel::load,
                onHistoryClick = { navController.navigate(AppDestination.History.route) },
                onBackToCandidateHomeClick = {
                    val backRoute = when (authenticatedRole) {
                        UserRole.Controller -> AppDestination.ControllerHome.route
                        UserRole.Candidate -> AppDestination.CandidateHome.route
                        null -> AppDestination.Login.route
                    }
                    navController.navigate(backRoute) {
                        if (authenticatedRole == null) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                        launchSingleTop = true
                    }
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
            val candidateListViewModel: CandidateListViewModel = hiltViewModel()
            val candidateListUiState by candidateListViewModel.uiState.collectAsState()
            LaunchedEffect(Unit) { candidateListViewModel.load() }
            CandidateListScreen(
                uiState = candidateListUiState,
                onCandidateDetailsClick = { participantType, participantKey ->
                    navController.navigate(
                        AppDestination.CandidateDetails.createRoute(
                            participantType = participantType,
                            participantKey = Uri.encode(participantKey),
                        ),
                    )
                },
                onBackToControllerHomeClick = {
                    navController.navigate(AppDestination.ControllerHome.route)
                },
                onRetryClick = candidateListViewModel::load,
            )
        }

        composable(AppDestination.CandidateDetails.route) {
            val candidateDetailsViewModel: CandidateDetailsViewModel = hiltViewModel()
            val candidateDetailsUiState by candidateDetailsViewModel.uiState.collectAsState()
            LaunchedEffect(Unit) { candidateDetailsViewModel.load() }
            CandidateDetailsScreen(
                uiState = candidateDetailsUiState,
                onMetricSelected = candidateDetailsViewModel::selectMetric,
                onBackToCandidateListClick = {
                    navController.navigate(AppDestination.CandidateList.route)
                },
            )
        }
    }
}
