package com.example.ap2.mapScreenComposables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Path

/**
 * Zeigt einen Hinweistext am oberen Bildschirmrand an, wenn sich die App
 * im Modus [MapMode.PLACING_MARKER] befindet
 * Fordert den Nutzer auf, auf einen beliebigen Punkt der Karte zu tippen.
 *
 * @param onCancel Callback zum Abbrechen des Platzierungsmodus
 * @param modifier Optionaler Layout-Modifier
 */
@Composable
fun PlacingModeHint(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tippe auf die Karte, um den Marker zu setzen",
                color = Color.Black
            )
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.6f)),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Abbrechen", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Ein Bestätigungs-Overlay am unteren Bildschirmrand, nachdem der Nutzer
 * im Platzierungsmodus einen Punkt auf der Karte angetippt hat
 *
 * @param onConfirm Callback zum Speichern des temporären Markers an der gewählten Position
 * @param onCancel Callback zum Verwürfen des gesetzten Punkts und Zurückkehren in den Standardmodus
 * @param modifier Optionaler Layout-Modifier
 */
@Composable
fun ConfirmMarkerOverlay(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Marker hier speichern?", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Abbrechen")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Bestätigen")
                }
            }
        }
    }
}