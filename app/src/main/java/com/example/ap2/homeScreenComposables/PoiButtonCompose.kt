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
fun POIButton(onClick: () -> Unit, modifier: Modifier) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(135.dp, 40.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Text(text = "POI", textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Preview(showBackground = true)
@Composable
fun POIButtonPreview() {
    MoCo_2026Theme {
        POIButton(onClick ={}, modifier = Modifier)
    }
}