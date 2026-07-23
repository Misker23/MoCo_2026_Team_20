package com.example.ap2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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

class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocation || coarseLocation) {
                // Berechtigung erteilt
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLocationPermission()
        enableEdgeToEdge()

        setContent {
            MoCo_2026Theme {
                // Prüft beim Start, ob bereits eine aktive Supabase-Sitzung existiert
                var isLoggedIn by remember {
                    mutableStateOf(supabase.auth.currentUserOrNull() != null)
                }

                if (isLoggedIn) {
                    // EINGELOGGT: Zeige Navigation zwischen HomeScreen und FriendsScreen
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute
                    ) {
                        composable<HomeRoute> {
                            HomeScreen(
                                onNavigateToFriends = {
                                    navController.navigate(FriendsRoute)
                                },
                                onLogout = {
                                    isLoggedIn = false
                                }
                            )
                        }

                        composable<FriendsRoute> {
                            FriendsScreenCompose(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                } else {
                    // NICHT EINGELOGGT: Zeige den AuthScreen für Login / Registrierung
                    AuthScreenCompose(
                        onAuthSuccess = {
                            isLoggedIn = true
                        }
                    )
                }
            }
        }
    }

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