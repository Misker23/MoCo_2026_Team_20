package com.example.ap2.homeScreenComposables

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ap2.ui.theme.MoCo_2026Theme

@Composable
fun SettingButton(onClick: () -> Unit, modifier: Modifier) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(width = 135.dp, height = 40.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.08f),
            contentColor = Color.Black
        )
    ) {
        Text(text = "Einstellungen", textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingButtonPreview() {
    MoCo_2026Theme {
        SettingButton(onClick ={}, modifier = Modifier)
    }
}