package com.example.diplomproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

private enum class LoginScreenMode {
    AUTH,
    GUEST
}

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var screenMode by rememberSaveable { mutableStateOf(LoginScreenMode.AUTH) }

    AppScreenScaffold(title = "Вход") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (screenMode) {
                LoginScreenMode.AUTH -> {
                    OutlinedTextField(
                        value = uiState.login,
                        onValueChange = viewModel::onLoginChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        isError = uiState.loginError != null,
                        supportingText = { uiState.loginError?.let { Text(it) } },
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
                        supportingText = { uiState.passwordError?.let { Text(it) } },
                    )

                    uiState.authError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        onClick = viewModel::login,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(text = "Войти")
                    }

                    OutlinedButton(
                        onClick = { screenMode = LoginScreenMode.GUEST },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                    ) {
                        Text("Пройти тест по токену")
                    }

                    TextButton(onClick = onRegisterClick) {
                        Text(text = "Нет аккаунта? Зарегистрироваться")
                    }
                }

                LoginScreenMode.GUEST -> {
                    Text(
                        text = "Прохождение теста по токену",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = uiState.guestTokenInput,
                        onValueChange = viewModel::onGuestTokenChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Токен доступа") },
                        singleLine = true,
                        isError = uiState.guestTokenError != null,
                        supportingText = { uiState.guestTokenError?.let { Text(it) } },
                    )

                    OutlinedTextField(
                        value = uiState.guestNameInput,
                        onValueChange = viewModel::onGuestNameChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Имя гостя") },
                        singleLine = true,
                        isError = uiState.guestNameError != null,
                        supportingText = { uiState.guestNameError?.let { Text(it) } },
                    )

                    uiState.guestStartError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        onClick = viewModel::startGuestByToken,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Пройти тест по токену")
                    }

                    OutlinedButton(
                        onClick = { screenMode = LoginScreenMode.AUTH },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                    ) {
                        Text("Назад")
                    }
                }
            }
        }
    }
}