package com.example.ap2.homeScreenComposables

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ProfileButton(onLogout: () -> Unit, modifier: Modifier = Modifier) {
    var showMenu by remember { mutableStateOf(false) }
    Button(
        onClick = { showMenu = true },
        modifier = modifier,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFFFFF),
            contentColor = Color.Black)
    ) {
        Text(text = "Profile", textAlign = TextAlign.Center, maxLines = 1)
    }
    if (showMenu) {
        ProfileWindow(
            onDismiss = { showMenu = false },
            onLogout = onLogout
        )
    }

}

@Preview
@Composable
fun ProfileButtonPreview() {
    ProfileButton(onLogout = {})
}