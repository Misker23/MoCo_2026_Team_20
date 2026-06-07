package com.example.ap2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ap2.ui.theme.MoCo_2026Theme

@Composable
fun ProfileScreen() {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Button_Mold(
            onClick = { showMenu = !showMenu },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            shape = CircleShape
        ) {
            Text(text = "Profile", textAlign = TextAlign.Center, maxLines = 1)
        }
        if (showMenu) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(0.5f),
                shape = RectangleShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    IconButton(
                        onClick = { showMenu = false },
                        modifier = Modifier.width(25.dp).height(25.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_top_left),
                            contentDescription = "Zurück",
                            tint = Color.Unspecified
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Menüpunkt 1")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Menüpunkt 2")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Profil_ButtonPreview() {
    MoCo_2026Theme {
        ProfileScreen()
    }
}