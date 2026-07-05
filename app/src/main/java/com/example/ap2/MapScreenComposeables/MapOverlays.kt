package com.example.ap2.MapScreenComposeables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlacingModeHint(
    onCancel: () -> Unit, // Aktion fürs Abbrechen
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tippe auf die Karte, um den Marker zu setzen",
                color = Color.White
            )
            // Abbruch-Button
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

@Composable
fun ConfirmMarkerOverlay(
    onConfirm: () -> Unit, // Aktion fürs Bestätigen
    onCancel: () -> Unit, // Aktion fürs Abbrechen
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Marker hier speichern?", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))

            // Buttons nebeneinander anordnen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Abbrechen-Button
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Abbrechen")
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Bestätigen-Button
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