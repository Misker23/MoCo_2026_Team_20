package com.example.ap2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ap2.ui.theme.MoCo_2026Theme
import androidx.lifecycle.lifecycleScope // Import für die Coroutine
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email // Import für den Email-Login
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineLocation =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false

            val coarseLocation =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false


            if (fineLocation || coarseLocation) {
                // Permission wurde erteilt
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLocationPermission()

        enableEdgeToEdge()
        //Login Logik
        lifecycleScope.launch {
            try {
                supabase.auth.signInWith(Email) {
                    email = "test@example.com"
                    password = "password123"
                }
                println("DEBUG: Login erfolgreich!")
            } catch (e: Exception) {
                println("DEBUG: Login FEHLGESCHLAGEN: ${e.message}")
            }
        }

        setContent {
            MoCo_2026Theme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = HomeRoute
                ) {
                    composable<HomeRoute> {
                        HomeScreen(
                            onNavigateToFriends = {
                                navController.navigate(FriendsRoute)
                            }
                        )
                    }

                    composable<FriendsRoute> {
                        FriendsScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }


    private fun requestLocationPermission() {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


        val coarseGranted =
            ContextCompat.checkSelfPermission(
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