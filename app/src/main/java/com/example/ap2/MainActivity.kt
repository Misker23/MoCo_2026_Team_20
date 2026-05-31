package com.example.ap2

import android.R.attr.onClick
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.ap2.ui.theme.MoCo_2026Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoCo_2026Theme {
                HomeScreen()
            }
        }
    }
}
@Composable
fun HomeScreen() {
    var isMarkerScreenVisible by remember { mutableStateOf(false) }
    Scaffold(
        bottomBar = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.Gray))
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(
                    painter = painterResource(R.drawable.baseline_add_location_alt_24),
                    contentDescription = null
                )
            }
        }
    ) {contentPadding ->
        Column(
            modifier = Modifier.
                fillMaxSize().
                padding(contentPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmallMarker(onExpandRequested = {isMarkerScreenVisible = true})
        }
        if (isMarkerScreenVisible) {
            MarkerScreen(
                bottomPadding = contentPadding.calculateBottomPadding(),
                onDismiss = { isMarkerScreenVisible = false }
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    MoCo_2026Theme() {
        HomeScreen()
    }
}

@Composable
fun SmallMarker(onExpandRequested: () -> Unit) {
    var isSneakPeekVisible by remember { mutableStateOf(false) }

    Box() {
        Icon(
            painter = painterResource(R.drawable.baseline_place_24),
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier
                .size(48.dp)
                .clickable { isSneakPeekVisible = true },
        )

        if (isSneakPeekVisible) {
            SneakPeekMarker(
                onDismiss = { isSneakPeekVisible = false },
                onExpand = {
                    onExpandRequested()
                    isSneakPeekVisible = false // Close small when opening big
                }
            )
        }
    }
}

@Preview
@Composable
fun SmallMarkerPreview() {
    SmallMarker(onExpandRequested = {})
}

@Composable
fun SneakPeekMarker(onDismiss: () -> Unit, onExpand: () -> Unit) {
    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .size(200.dp, 120.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                .clickable { onExpand() }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp, 60.dp)
                        .background(Color.Black)
                )
                Text(
                    "Hier steht eine Beschreibung",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontSize = 12.sp
                )
            }
        }
    }


    /*if (isExpanded) {
        MarkerScreen ( onDismiss = {isExpanded = false})
    }*/
}

@Preview
@Composable
fun SneakPeekMarkerPreview() {
    SneakPeekMarker(onDismiss = {}, onExpand = {})
}

@Composable
fun MarkerScreen(bottomPadding: androidx.compose.ui.unit.Dp, onDismiss: () -> Unit) {
    var description by remember {mutableStateOf("")}
    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier.padding(bottom = bottomPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(350.dp, 750.dp)
                    .background(Color.LightGray)
                    .alpha(0.5F)
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()

                ) {
                    Box(
                        modifier = Modifier
                            .size(250.dp, 300.dp)
                            .background(Color.Black)
                    )
                    TextField(
                        value = description,
                        onValueChange = {text ->
                            description = text
                        }
                    )
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MarkerScreenPreview() {
    MarkerScreen(bottomPadding = 0.dp, onDismiss = {})
}