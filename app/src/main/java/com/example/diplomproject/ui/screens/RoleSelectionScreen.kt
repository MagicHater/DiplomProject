package com.example.diplomproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RoleSelectionScreen(
    onCandidateClick: () -> Unit,
    onControllerClick: () -> Unit,
    viewModel: RoleSelectionViewModel = hiltViewModel(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Select role",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Available: ${viewModel.roles.joinToString()}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Button(onClick = onCandidateClick) {
            Text(text = "Candidate")
        }
        Button(
            onClick = onControllerClick,
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text(text = "Controller")
        }
    }
}
