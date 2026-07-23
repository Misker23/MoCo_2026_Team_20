package com.example.ap2.homeScreenComposables

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.ap2.ui.theme.MoCo_2026Theme

@Composable
fun FriendsButton(onClick: () -> Unit, modifier: Modifier) {
    Button(
        onClick = onClick,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFFFFF),
            contentColor = Color.Black
        )
    ) {
        Text(text = "Friends", textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Preview(showBackground = true)
@Composable
fun FriendsButtonPreview() {
    MoCo_2026Theme {
        FriendsButton(onClick ={}, modifier = Modifier)
    }
}