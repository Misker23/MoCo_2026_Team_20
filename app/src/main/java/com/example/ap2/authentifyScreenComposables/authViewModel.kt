package com.example.ap2.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.ap2.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthViewModel : ViewModel() {

    var isSignUpMode by mutableStateOf(false)
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var username by mutableStateOf("")

    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    suspend fun handleAuth(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Bitte E-Mail und Passwort eingeben."
            return
        }

        if (isSignUpMode && username.isBlank()) {
            errorMessage = "Bitte einen Benutzernamen wählen."
            return
        }

        isLoading = true
        errorMessage = null

        try {
            if (isSignUpMode) {
                // Registrierung: Username wird in die User-Metadata geschrieben,
                // wo ihn dein Postgres-Trigger direkt für die 'profiles'-Tabelle abgreift!
                supabase.auth.signUpWith(Email) {
                    this.email = this@AuthViewModel.email.trim()
                    this.password = this@AuthViewModel.password
                    this.data = buildJsonObject {
                        put("username", this@AuthViewModel.username.trim())
                    }
                }
                Log.d("Auth", "Registrierung erfolgreich!")
            } else {
                // Login
                supabase.auth.signInWith(Email) {
                    this.email = this@AuthViewModel.email.trim()
                    this.password = this@AuthViewModel.password
                }
                Log.d("Auth", "Login erfolgreich!")
            }
            isLoading = false
            onSuccess()
        } catch (e: Exception) {
            isLoading = false
            errorMessage = e.localizedMessage ?: "Ein Fehler ist aufgetreten."
            Log.e("Auth", "Fehler bei Authentifizierung: ${e.message}")
        }
    }
}