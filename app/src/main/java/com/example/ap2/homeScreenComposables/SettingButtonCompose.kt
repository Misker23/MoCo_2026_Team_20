package com.example.ap2.HomeScreenComposables

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.ap2.ui.theme.MoCo_2026Theme

@Composable
fun SettingButton(onClick: () -> Unit, modifier: Modifier) {
    Button(
        onClick = onClick,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFFFFF),
            contentColor = Color.Black
        )
    ) {
        Text(text = "Settings", textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingButtonPreview() {
    MoCo_2026Theme {
        SettingButton(onClick ={}, modifier = Modifier)
    }
}