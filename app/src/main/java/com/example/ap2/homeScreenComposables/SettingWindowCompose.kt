package com.example.ap2.homeScreenComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun SettingWindow(bottomPadding: Dp, onDismiss: () -> Unit) {
    //Popup Fenster für SettingWindow
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
                .background(Color.LightGray, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            //verticales spacing zwischen den Items im Column
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Menüpunkt 1", modifier = Modifier.fillMaxWidth())
            Text("Menüpunkt 2", modifier = Modifier.fillMaxWidth())
            //Button zum schließen des Screens, da es ein größeres Window ist
            Button(onClick = onDismiss) { Text("Close") }
        }
    }
}

@Preview
@Composable
fun SettingWindowPreview() {
    SettingWindow(bottomPadding = 0.dp, onDismiss = {})
}
