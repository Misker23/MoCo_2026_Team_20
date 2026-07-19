package com.example.ap2.HomeScreenComposables

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog // NEU
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults // NEU
import androidx.compose.material3.OutlinedButton // NEU
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.ap2.MarkerDto

@Composable
fun MarkerWindow(
    bottomPadding: Dp,
    markerDto: MarkerDto?,
    onDismiss: () -> Unit,
    onSave: (String, String, ByteArray?) -> Unit,
    onDelete: () -> Unit // NEU: Callback fürs Löschen
) {
    val context = LocalContext.current
    var description by remember(markerDto) { mutableStateOf(markerDto?.description ?: "") }
    var selectedColor by remember(markerDto) { mutableStateOf(markerDto?.color ?: "#E91E63") }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    // NEU: State für die Sichtbarkeit der Sicherheitsabfrage
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val previewBitmap = remember(selectedImageBytes) {
        selectedImageBytes?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try { selectedImageBytes = context.contentResolver.openInputStream(it)?.readBytes() }
            catch (e: Exception) { e.printStackTrace() }
        }
    }

    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = bottomPadding)
                .size(350.dp, 650.dp)
                .background(Color.LightGray.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bild-Bereich
            Box(
                modifier = Modifier
                    .size(220.dp, 220.dp)
                    .background(Color.Black, RoundedCornerShape(8.dp))
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    previewBitmap != null -> {
                        Image(bitmap = previewBitmap, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    !markerDto?.image_url.isNullOrEmpty() -> {
                        AsyncImage(model = markerDto.image_url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    else -> { Text("Bild hinzufügen (Klicken)", color = Color.Gray) }
                }
            }

            val extendedColorPalette = listOf(
                "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3",
                "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A",
                "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722",
                "#795548", "#9E9E9E", "#607D8B"
            )

            Text("Marker-Farbe wählen:")

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                items(extendedColorPalette) { hexColor ->
                    val composeColor = Color(android.graphics.Color.parseColor(hexColor))
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(composeColor, CircleShape)
                            .border(
                                width = if (selectedColor.lowercase() == hexColor.lowercase()) 3.dp else 0.dp,
                                color = Color.Black,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = hexColor }
                    )
                }
            }

            TextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Beschreibung") }
            )

            // Button-Reihe untereinander
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        onSave(description, selectedColor, selectedImageBytes)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Änderungen speichern")
                }

                // NEU: Löschen Button im gleichen Format (Rot eingefärbt zur Verdeutlichung)
                Button(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Marker löschen", color = Color.White)
                }
            }
        }
    }

    // NEU: Die zusätzliche Sicherheitsabfrage als AlertDialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Marker löschen?") },
            text = { Text("Bist du dir sicher, dass du diesen Marker dauerhaft aus der Datenbank entfernen möchtest?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete() // Führt Löschvorgang aus
                        onDismiss() // Schließt das Marker-Fenster
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}