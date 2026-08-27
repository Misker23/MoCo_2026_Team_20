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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.ap2.data_models.ProfileDto
import com.example.ap2.supabase
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@Composable
fun ProfileWindow(
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    profileDto: ProfileDto?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

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
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = "Benutzername: ${profileDto?.username ?: "Unbekannt"}",
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        Modifier.size(18.dp).align(Alignment.CenterVertically)
                    )
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
                        .background(Color.Black.copy(alpha = 0.07f), RoundedCornerShape(24.dp)),
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
    }
}

@Preview
@Composable
fun ProfileWindowPreview() {
    ProfileWindow(onDismiss = {}, profileDto = null, onLogout = {})
}