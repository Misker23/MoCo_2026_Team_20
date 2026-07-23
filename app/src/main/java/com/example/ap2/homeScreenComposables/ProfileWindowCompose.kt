package com.example.ap2.homeScreenComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.ap2.supabase
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@Composable
fun ProfileWindow(
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Popup(
        alignment = Alignment.TopStart,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .size(200.dp, 300.dp)
                .background(Color.LightGray)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Menüpunkt 1")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Menüpunkt 2")

                // Schiebt den Button an den unteren Rand der Box
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            supabase.auth.signOut()
                            onLogout() // Bringt den User zurück zum AuthScreen
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abmelden")
                }
            }
        }
    }
}

@Preview
@Composable
fun ProfileWindowPreview() {
    ProfileWindow(onDismiss = {}, onLogout = {})
}