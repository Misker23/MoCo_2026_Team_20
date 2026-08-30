package com.example.ap2.homeScreenComposables

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ap2.data_models.ProfileDto
import com.example.ap2.mapScreenComposables.MapViewModel
import com.example.ap2.supabase
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@Composable
fun ProfileWindow(
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    viewModel: MapViewModel
) {

    LaunchedEffect(Unit) {
        if (viewModel.currentUserProfile == null) {
            viewModel.fetchCurrentUserProfile()
            viewModel.loadMarkersForMap()
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    val profile = viewModel.currentUserProfile

    var isEditing by remember { mutableStateOf(false) }
    var tempUsername by remember(profile) { mutableStateOf(profile?.username ?: "") }
    var showStatsDialog by remember { mutableStateOf(false) }

    val previewBitmap = remember(selectedImageBytes) {
        selectedImageBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                selectedImageBytes = context.contentResolver.openInputStream(it)?.readBytes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Popup(
        alignment = Alignment.TopStart,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .size(400.dp, 400.dp)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                .padding(start = 16.dp, top = 12.dp, end = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                Box(
                    modifier = Modifier
                        .size(100.dp, 100.dp)
                        .background(Color.Gray, CircleShape)
                        .align(alignment = Alignment.CenterHorizontally)
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    /* when {
                        previewBitmap != null -> {
                            Image(
                                bitmap = previewBitmap,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        !profileDto?.image_url.isNullOrEmpty() -> {
                            AsyncImage(
                                model = profileDto.image_url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        else -> {
                            Text("Bild hinzufügen", color = Color.Gray)
                        }
                    } */
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedTextField(
                                value = tempUsername,
                                onValueChange = { tempUsername = it },
                                singleLine = true
                            )
                            IconButton(onClick = {
                                viewModel.updateUsername(tempUsername)
                                isEditing = false
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Speichern", tint = Color.Green)
                            }
                        }
                    } else {
                        // NORMALER TEXT
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Ein unsichtbarer Platzhalter links, der genau so groß ist wie der Button rechts.
                            // Das sorgt dafür, dass der Text in der Mitte bleibt.
                            Spacer(modifier = Modifier.size(48.dp))

                            // 2. Der Username nimmt den gesamten restlichen Platz ein und zentriert sich selbst.
                            Text(
                                text = profile?.username ?: "Lädt...",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 20.sp,
                                color = Color.Black
                            )

                            // 3. Das Bearbeitungs-Icon als IconButton (standardmäßig ca. 48dp groß)
                            IconButton(
                                onClick = { isEditing = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Bearbeiten",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(height = 40.dp, width = 100.dp)
                        .background(Color.Black.copy(alpha = 0.07f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Quests", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(width = 350.dp, height = 40.dp)
                        .background(Color.Black.copy(alpha = 0.07f), RoundedCornerShape(24.dp))
                        .clickable{showStatsDialog = true},
                    contentAlignment = Alignment.Center
                ) {
                    Text("Stats", color = Color.Black)
                }

                // Schiebt den Button an den unteren Rand der Box
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            supabase.auth.signOut()
                            onLogout() // Bringt den User zurück zum AuthScreen
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Text("Abmelden")
                }
            }
        }
        if (showStatsDialog) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Deine Statistiken", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Zurückgelegte Schritte: ${viewModel.stepsFromDistance}")
                        Text("Eigene Marker: ${viewModel.ownMarkersCount}")
                        Text("Mit dir geteilte Marker: ${viewModel.markersSharedWithMeCount}")
                    }
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Schließen", color = Color(0xFF2196F3))
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }
    }
}

@Preview
@Composable
fun ProfileWindowPreview() {
    ProfileWindow(onDismiss = {}, onLogout = {}, viewModel = MapViewModel())
}