package com.example.ap2.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

/**
 * Repräsentiert die UI des Authentifizierungsbildschirms.
 * Bietet Eingabemasken für E-Mail, Passwort und (optional bei Registrierung) Benutzernamen.
 *
 * @param viewModel Das zugehörige [AuthViewModel], das den Status verwaltet.
 * @param onAuthSuccess Callback für das Navigieren bei erfolgreichem Login/Registrieren.
 */
@Composable
fun AuthScreenCompose(
    viewModel: AuthViewModel = viewModel(),
    onAuthSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .systemBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (viewModel.isSignUpMode) "Account erstellen" else "Willkommen zurück",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (viewModel.isSignUpMode) "Registriere dich um deine Umgebung zu erkunden und um Marker mit Freunden zu teilen" else "Gib deine Daten ein, um fortzufahren",
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Nur bei Registrierung sichtbar:
            if (viewModel.isSignUpMode) {
                OutlinedTextField(
                    value = viewModel.username,
                    onValueChange = { viewModel.username = it },
                    label = { Text("Benutzername") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.secondary,
                        unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary,
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
                    )
                )
            }

            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                label = { Text("E-Mail Address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.secondary,
                    unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.secondary,
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
                )
            )

            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = { Text("Passwort") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.secondary,
                    unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.secondary,
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
                )
            )

            // Fehlermeldung anzeigen
            viewModel.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color(0xFFD32F2F),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.handleAuth(onSuccess = onAuthSuccess)
                    }
                },
                enabled = !viewModel.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (viewModel.isSignUpMode) "Registrieren" else "Anmelden",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Umschalten zwischen Login & Registrierung
            TextButton(
                onClick = {
                    viewModel.isSignUpMode = !viewModel.isSignUpMode
                    viewModel.errorMessage = null
                }
            ) {
                Text(
                    text = if (viewModel.isSignUpMode) "Bereits einen Account? Hier anmelden" else "Noch keinen Account? Registrieren",
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}