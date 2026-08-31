package com.example.ap2.auth

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ap2.data.remote.supabase
import com.example.ap2.utils.UserPreferences
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * ViewModel zur Steuerung der Authentifizierungslogik (Login und Registrierung).
 * Verwalte den UI-Zustand für Eingabefelder, Fehlermeldungen sowie den Ladezustand
 * und kommuniziert direkt mit Supabase GoTrue Auth.
 */
class AuthViewModel : ViewModel() {

    /** Steuert, ob der Bildschirm im Registrierungs- (true) oder Login-Modus (false) ist. */
    var isSignUpMode by mutableStateOf(false)

    /** Die im Textfeld eingegebene E-Mail-Adresse. */
    var email by mutableStateOf("")

    /** Das im Textfeld eingegebene Passwort. */
    var password by mutableStateOf("")

    /** Der gewählte Benutzername (wird nur bei der Registrierung verwendet). */
    var username by mutableStateOf("")

    /** Aktualisierbare Fehlermeldung zur Anzeige in der UI. Null, wenn kein Fehler vorliegt. */
    var errorMessage by mutableStateOf<String?>(null)

    /** Ladeindikator für asynchrone Netzwerk-Anfragen. */
    var isLoading by mutableStateOf(false)

    var usernameInput by mutableStateOf("")

    /**
     * Führt je nach [isSignUpMode] entweder eine Registrierung oder einen Login über Supabase durch.
     *
     * Bei der Registrierung wird der Benutzername in die `data`-Metadata von Supabase Auth geschrieben,
     * wo ein Postgres-Trigger ihn automatisch in die `profiles`-Tabelle überträgt.
     *
     * @param onSuccess Callback, der nach erfolgreicher Authentifizierung ausgeführt wird (z. B. Navigation).
     */

    fun loadSavedUsername(context: Context) {
        viewModelScope.launch {
            UserPreferences(context).lastUsernameFlow.collect { savedName ->
                if (usernameInput.isEmpty()) {
                    usernameInput = savedName
                }
            }
        }
    }

    fun checkOfflineOrOnlineSession(onAutoLoginSuccess: () -> Unit, onLoginRequired: () -> Unit) {
        viewModelScope.launch {
            val cachedUser = supabase.auth.currentUserOrNull()

            if (cachedUser != null) {
                // Nutzer war bereits angemeldet -> direkt in die App weiterleiten!
                onAutoLoginSuccess()
            } else {
                // Keine lokale Session vorhanden -> Login-Screen zeigen
                onLoginRequired()
            }
        }
    }
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
                supabase.auth.signUpWith(Email) {
                    this.email = this@AuthViewModel.email.trim()
                    this.password = this@AuthViewModel.password
                    this.data = buildJsonObject {
                        put("username", this@AuthViewModel.username.trim())
                    }
                }
                Log.d("Auth", "Registrierung erfolgreich!")
            } else {
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

    fun onLoginSuccess(context: Context, usernameOrEmail: String) {
        viewModelScope.launch {
            UserPreferences(context).saveLastUsername(usernameOrEmail)
        }
    }


}