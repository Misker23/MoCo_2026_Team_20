package com.example.ap2.HomeScreenComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

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