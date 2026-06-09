package com.example.ap2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun OpenedMarker(bottomPadding: Dp, onDismiss: () -> Unit) {
    //State Remember für die Beschreibung, damit das was im Feld eingegeben wird auch zwischengespeichert wird
    var description by remember { mutableStateOf("") }
    //Popup Fenster für OpenedMarker
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
            //Textfeld, um die Beschreibung einzugeben, behält die Beschreibung noch nicht und gibt sie nicht weiter an SneakPeekMarker
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
fun OpenedMarkerPreview() {
    OpenedMarker(bottomPadding = 0.dp, onDismiss = {})
}