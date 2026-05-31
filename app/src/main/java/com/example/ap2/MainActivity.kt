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
import androidx.compose.ui.unit.Dp
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
    //State Remember für MarkerScreen, ob dieser angezeigt wird oder nicht
    var isMarkerScreenVisible by remember { mutableStateOf(false) }
    //Einteilung des HomeScreens in mehrere Sektionen (Hauptcontent, Bottom Navigation Bar)
    Scaffold(
        //Bottom Navigation Bar ohne buttons
        bottomBar = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.Gray))
        },
        //Add Marker button ohne Funktion
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(
                    painter = painterResource(R.drawable.baseline_add_location_alt_24),
                    contentDescription = null
                )
            }
        }
        //Padding damit die Bottom Bar und Hauptcontent getrennt sind
    ) {contentPadding ->
        //um den Marker bisher in der Mitte anzuzeigen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //damit der initial Marker auf der "map" zu sehen ist und anklickbar ist
            SmallMarker(onExpandRequested = {isMarkerScreenVisible = true})
        }
        //hier damit der Screen vom Boden des Hauptcontents erscheint statt komplett unten oder vom Marker aus
        if (isMarkerScreenVisible) {
            MarkerScreen(
                //damit der Screen die Bottombar nicht überdeckt
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
    //State Remember für SneakPeekMarker, ob dieser angezeigt wird oder nicht
    var isSneakPeekVisible by remember { mutableStateOf(false) }

    //box damit der Marker in der Mitte ist
    Box() {
        //damit der Button nur das Icon ist
        Icon(
            //woher das Icon kommt
            painter = painterResource(R.drawable.baseline_place_24),
            contentDescription = null,
            //Farbänderung des Icons
            tint = Color.Red,
            //Vergrößerung des Icons und macht das Icon clickable
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    //damit kein graußes Quadrat im Hintergrund ist
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                    // zeigt den SneakPeekMarker an
                ) { isSneakPeekVisible = true },
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
    //um ein Popup Fenster zu generieren
    Popup(
        //damit es vom Marker aus unten und mittig geöffnet wird
        alignment = Alignment.BottomCenter,
        //damit das Fenster auch geschlossen werden kann
        onDismissRequest = onDismiss,
        //damit man das Fenster schließt wenn man außerhalb des Fensters klickt
        properties = PopupProperties(focusable = true)
    ) {
        //box um das Popup Fenster zu gestalten
        Box(
            modifier = Modifier
                .size(200.dp, 120.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                //clickable damit der MarkerScreen geöffnet werden kann
                .clickable { onExpand() }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Ersatz für ein Bild, nur als Beispiel für die UI gedacht
                Box(
                    modifier = Modifier
                        .size(120.dp, 60.dp)
                        .background(Color.Black)
                )
                //muss noch den Text vom MarkerScreen bekommen und behalten statt einem statischen Text
                Text(
                    "Hier steht eine Beschreibung",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun SneakPeekMarkerPreview() {
    SneakPeekMarker(onDismiss = {}, onExpand = {})
}

@Composable
fun MarkerScreen(bottomPadding: Dp, onDismiss: () -> Unit) {
    //State Remember für die Beschreibung, damit das was im Feld eingegeben wird auch zwischengespeichert wird
    var description by remember { mutableStateOf("") }
    //Popup Fenster für MarkerScreen
    Popup(
        //damit es unten und mittig öffnet
        alignment = Alignment.BottomCenter,
        //damit das Fenster auch geschlossen werden kann
        onDismissRequest = onDismiss,
        //damit aus dem Fenster klicken das Popup Fenster schließt
        properties = PopupProperties(focusable = true)
    ) {
        //Um mehrere Dinge untereinander anzuzeigen und zu formatieren
        Column(
            modifier = Modifier
                //damit das Fenster nicht über der Bottombar öffnet
                .padding(bottom = bottomPadding)
                .size(350.dp, 750.dp)
                //durchsichtiger Hintergrund, Bild, Beschreibung und Button sind nicht Durchsichtig
                .background(Color.LightGray.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            //verticales spacing zwischen den Items im Column
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //Bild ersatz
            Box(Modifier
                .size(250.dp, 250.dp)
                .background(Color.Black))
            //Textfeld um die Beschreibung einzugeben, behält die Beschreibung noch nicht und gibt sie nicht weiter an SneakPeekMarker
            TextField(
                //was bisher im Textfeld steht
                value = description,
                //wenn was geändert wird, wird das im Textfeld angepasst
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth()
            )
            //Button zum schließen des Screens, da es ein größerer Screen ist
            Button(onClick = onDismiss) { Text("Close") }
        }
    }
}

@Preview
@Composable
fun MarkerScreenPreview() {
    MarkerScreen(bottomPadding = 0.dp, onDismiss = {})
}