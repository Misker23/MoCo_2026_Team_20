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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ap2.authentifyScreenComposables.AuthScreenCompose
import com.example.ap2.friendsScreenComposables.FriendsScreenCompose
import com.example.ap2.homeScreenComposables.HomeScreen
import com.example.ap2.mapScreenComposables.MapViewModel
import com.example.ap2.ui.theme.MoCo_2026Theme
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocation || coarseLocation) {
                // Standortberechtigung erteilt
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MapViewModel by viewModels()

        requestLocationPermission()
        enableEdgeToEdge()

        setContent {
            MoCo_2026Theme(darkTheme = viewModel.isDarkMode) {
                val coroutineScope = rememberCoroutineScope()

                // Liest die gecachte Sitzung beim App-Start (Offline & Online funktionsfähig)
                var isLoggedIn by remember {
                    mutableStateOf(supabase.auth.currentUserOrNull() != null)
                }

                if (isLoggedIn) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute
                    ) {
                        composable<HomeRoute> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToFriends = {
                                    navController.navigate(FriendsRoute)
                                },
                                onLogout = {
                                    coroutineScope.launch {
                                        try {
                                            // 1. Lokalen Offline-Token und Server-Session löschen
                                            supabase.auth.signOut()
                                        } catch (_: Exception) {
                                            // Ignoriert Netzwerkfehler beim Offline-Logout
                                        } finally {
                                            // 2. UI auf AuthScreen umschalten
                                            isLoggedIn = false
                                        }
                                    }
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