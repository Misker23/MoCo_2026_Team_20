package com.example.ap2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ap2.ui.theme.MoCo_2026Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoCo_2026Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun Button_Mold(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(56.dp)
            .height(54.dp),
        contentPadding = PaddingValues(0.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFFFFF),
            contentColor = Color.Black
        )
    ) {
        content()
    }
}

@Composable
fun Setting_Button() {
    var showMenu by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        Button_Mold(
            onClick = { showMenu = !showMenu },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            shape = CircleShape
        ) {
            Text(text = "Setting", textAlign = TextAlign.Center, maxLines = 1)
        }
        if (showMenu) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(0.25f),
                shape = RectangleShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    IconButton(
                        onClick = { showMenu = false },
                        modifier = Modifier
                            .width(25.dp)
                            .height(25.dp)
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

@Composable
fun POI_Button() {
    var showMenu by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        Button_Mold(
            onClick = { showMenu = !showMenu },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = CircleShape
        ) {
            Text(text = "POI", textAlign = TextAlign.Center, maxLines = 1)
        }
        if (showMenu) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(0.25f),
                shape = RectangleShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    IconButton(
                        onClick = { showMenu = false },
                        modifier = Modifier
                            .width(25.dp)
                            .height(25.dp)
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
                        modifier = Modifier
                            .width(25.dp)
                            .height(25.dp)
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
fun GreetingPreview() {
    MoCo_2026Theme {
        Greeting("Android")
    }
}

@Preview(showBackground = true)
@Composable
fun Setting_ButtonPreview() {
    MoCo_2026Theme {
        Setting_Button()
    }
}

@Preview(showBackground = true)
@Composable
fun POI_ButtonPreview() {
    MoCo_2026Theme {
        POI_Button()
    }
}

@Preview(showBackground = true)
@Composable
fun Profil_ButtonPreview() {
    MoCo_2026Theme {
        ProfileScreen()
    }
}