package com.example.diplomproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diplomproject.domain.model.UserRole

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginSuccess: (UserRole) -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Вход", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = uiState.login,
            onValueChange = viewModel::onLoginChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Логин / Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            isError = uiState.loginError != null,
            supportingText = {
                uiState.loginError?.let { Text(it) }
            },
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            isError = uiState.passwordError != null,
            supportingText = {
                uiState.passwordError?.let { Text(it) }
            },
        )

        Text(
            text = "Роль",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            UserRole.entries.forEach { role ->
                OutlinedButton(
                    onClick = { viewModel.onRoleSelected(role) },
                    modifier = Modifier.weight(1f),
                ) {
                    val label = if (role == uiState.selectedRole) "✓ ${role.name}" else role.name
                    Text(text = label)
                }
            }
        }

        Button(
            onClick = {
                if (viewModel.validate()) {
                    onLoginSuccess(uiState.selectedRole)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Войти")
        }

        TextButton(onClick = onRegisterClick) {
            Text(text = "Нет аккаунта? Зарегистрироваться")
        }
    }
}
