package com.example.ap2.homeScreenComposables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ProfileButton(onLogout: () -> Unit, modifier: Modifier = Modifier) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(start = 16.dp, top = 12.dp)) {
        if (!showMenu) {
            Button(
                onClick = { showMenu = true },
                modifier = modifier,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Profile", textAlign = TextAlign.Center, maxLines = 1)
            }
        }
        if (showMenu) {
            ProfileWindow(
                onDismiss = { showMenu = false },
                profileDto = null,
                onLogout = onLogout
            )
        }
    }
}

@Preview
@Composable
fun ProfileButtonPreview() {
    ProfileButton(onLogout = {})
}