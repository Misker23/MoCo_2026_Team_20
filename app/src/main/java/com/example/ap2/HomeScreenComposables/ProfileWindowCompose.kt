package com.example.ap2.HomeScreenComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun ProfileWindow(onDismiss: () -> Unit) {
    Popup(
        alignment = Alignment.TopStart,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .size(200.dp, 300.dp)
                .background(Color.LightGray)
        ){
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Menüpunkt 1")
                Text("Menüpunkt 2")
            }
        }
    }
}

@Preview
@Composable
fun ProfileWindowPreview() {
    ProfileWindow(onDismiss = {})
}