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
import com.example.diplomproject.ui.screens.*

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by authViewModel.uiState.collectAsState()
    val authenticatedRole = (authState.appSessionState as? AppSessionState.Authenticated)?.role

    LaunchedEffect(Unit) { authViewModel.checkSavedSession() }

    LaunchedEffect(authState.appSessionState) {
        val destination = when (val sessionState = authState.appSessionState) {
            AppSessionState.Initializing -> return@LaunchedEffect
            AppSessionState.Unauthenticated -> AppDestination.Login.route
            is AppSessionState.Authenticated -> if (sessionState.role == UserRole.Controller)
                AppDestination.ControllerHome.route else AppDestination.CandidateHome.route
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

    NavHost(navController = navController, startDestination = AppDestination.Login.route) {

        composable(AppDestination.ControllerHome.route) {
            val vm: ControllerHomeViewModel = hiltViewModel()
            val state by vm.uiState.collectAsState()

            ControllerHomeScreen(
                uiState = state,
                onCategorySelected = vm::onCategorySelected,
                onGenerateTokenClick = vm::generateToken,
                onCreateTestClick = { navController.navigate(AppDestination.ControllerCreateTest.route) },
                onCandidateListClick = { navController.navigate(AppDestination.CandidateList.route) },
                onHistoryClick = { navController.navigate(AppDestination.ControllerDashboard.route) },
                onLogoutClick = { authViewModel.logout() },
            )
        }

        composable(AppDestination.ControllerDashboard.route) {
            ControllerDashboardScreen(onBack = { navController.popBackStack() })
        }

        // остальное оставляем без изменений
    }
}
