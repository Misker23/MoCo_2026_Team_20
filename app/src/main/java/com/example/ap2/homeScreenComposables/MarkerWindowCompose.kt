package com.example.ap2.homeScreenComposables

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton // NEU
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.ap2.data.remote.MarkerDto

@Composable
fun MarkerWindow(
    bottomPadding: Dp,
    markerDto: MarkerDto?,
    isOwnMarker: Boolean, // NEU: Prüft Eigentum
    onDismiss: () -> Unit,
    onSave: (String, String, ByteArray?) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var description by remember(markerDto) { mutableStateOf(markerDto?.description ?: "") }
    var selectedColor by remember(markerDto) { mutableStateOf(markerDto?.color ?: "#E91E63") }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val previewBitmap = remember(selectedImageBytes) {
        selectedImageBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (isOwnMarker) { // Nur für den Eigentümer
            uri?.let {
                try { selectedImageBytes = context.contentResolver.openInputStream(it)?.readBytes() }
                catch (e: Exception) { e.printStackTrace() }
            }
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
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(24.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bild-Bereich
            Box(
                modifier = Modifier
                    .size(200.dp, 200.dp)
                    .background(Color.Gray, RoundedCornerShape(12.dp))
                    .clickable(enabled = isOwnMarker) { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    previewBitmap != null -> {
                        Image(bitmap = previewBitmap, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    !markerDto?.image_url.isNullOrEmpty() -> {
                        AsyncImage(model = markerDto.image_url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    else -> { Text(if (isOwnMarker) "Bild hinzufügen" else "Kein Bild", color = Color.White) }
                }
            }

            if (isOwnMarker) {
                // Farbe wählen nur für Eigentümer
                val extendedColorPalette = listOf(
                    "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3",
                    "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A",
                    "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
                )

                Text("Marker-Farbe wählen:", color = MaterialTheme.colorScheme.secondary)

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(extendedColorPalette) { hexColor ->
                        val composeColor = Color(android.graphics.Color.parseColor(hexColor))
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(composeColor, CircleShape)
                                .border(
                                    width = if (selectedColor.lowercase() == hexColor.lowercase()) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hexColor }
                        )
                    }
                }
            }

            TextField(
                value = description,
                onValueChange = { if (isOwnMarker) description = it },
                enabled = isOwnMarker, // Für Empfänger gesperrt
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                label = { Text("Beschreibung", color = MaterialTheme.colorScheme.secondary) },
                colors = TextFieldDefaults.colors(
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    focusedTextColor = MaterialTheme.colorScheme.secondary,
                    unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                    disabledTextColor = MaterialTheme.colorScheme.secondary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Buttons nur für Eigentümer anzeigen
            if (isOwnMarker) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            onSave(description, selectedColor, selectedImageBytes)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("Änderungen speichern", color = Color.Black)
                    }

                    Button(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Marker löschen", color = Color.Black)
                    }
                }
            } else {
                Text(
                    text = "Geteilter Marker (Nur Lesezugriff)",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    if (showDeleteConfirmation && isOwnMarker) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Marker löschen?") },
            text = { Text("Bist du dir sicher, dass du diesen Marker dauerhaft aus der Datenbank entfernen möchtest?") },
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.secondary,
            textContentColor = MaterialTheme.colorScheme.secondary,
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Löschen", color = Color.Black)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Abbrechen", color = Color.Black)
                }
            }
        )
    }
}