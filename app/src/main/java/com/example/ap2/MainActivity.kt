package com.example.ap2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ap2.auth.AuthScreenCompose
import com.example.ap2.ui.theme.MoCo_2026Theme
import io.github.jan.supabase.gotrue.auth
import com.example.ap2.homeScreenComposables.HomeScreen
import com.example.ap2.friendsScreenComposables.FriendsScreenCompose
import com.example.ap2.mapScreenComposables.MapViewModel

/**
 * Haupteinstiegspunkt der Anwendung.
 *
 * Die Aktivität übernimmt folgende Aufgaben:
 * 1. Initialisierung der Standortberechtigungen (GPS).
 * 2. Aktivierung der Edge-to-Edge-Anzeige für ein modernes Design.
 * 3. Überprüfung des Authentifizierungsstatus (Supabase).
 * 4. Bereitstellung der Navigations-Host-Logik (zwischen Auth-, Home- und Friends-Bildschirmen).
 */
class MainActivity : ComponentActivity() {

    /**
     * Launcher für das Anfordern von Standortberechtigungen.
     * Verarbeitet das Ergebnis der Anfrage (Fine/Coarse Location).
     */
    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocation || coarseLocation) {
                // Berechtigung wurde erteilt, Kartenanwendung kann Standort nutzen.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MapViewModel by viewModels()

        // Standortberechtigungen beim Start prüfen
        requestLocationPermission()

        // Aktiviert Edge-to-Edge für rahmenlose Darstellung
        enableEdgeToEdge()

        setContent {
            MoCo_2026Theme(darkTheme = viewModel.isDarkMode) {
                // Prüft beim Start, ob bereits eine aktive Supabase-Sitzung existiert.
                // Steuert, ob der User direkt auf den Homescreen oder zum Login muss.
                var isLoggedIn by remember {
                    mutableStateOf(supabase.auth.currentUserOrNull() != null)
                }

                if (isLoggedIn) {
                    // EINGELOGGT: Navigations-Controller und Pfade für die Hauptanwendung
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute
                    ) {
                        // Hauptansicht (Karte & Overlays)
                        composable<HomeRoute> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToFriends = {
                                    navController.navigate(FriendsRoute)
                                },
                                onLogout = {
                                    isLoggedIn = false
                                }
                            )
                        }

                        // Freundesliste & Marker-Freigabe
                        composable<FriendsRoute> {
                            FriendsScreenCompose(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                } else {
                    // NICHT EINGELOGGT: Auth-Screen für Login oder Registrierung
                    AuthScreenCompose(
                        onAuthSuccess = {
                            isLoggedIn = true
                        }
                    )
                }
            }
        }
    }

    /**
     * Überprüft, ob Standortberechtigungen bereits vorhanden sind.
     * Wenn nicht, werden diese via System-Dialog angefragt.
     */
    private fun requestLocationPermission() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}