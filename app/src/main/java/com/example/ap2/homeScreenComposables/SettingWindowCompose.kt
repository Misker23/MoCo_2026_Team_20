package com.example.ap2.homeScreenComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ap2.mapScreenComposables.MapViewModel

@Composable
fun SettingWindow(bottomPadding: Dp, onDismiss: () -> Unit, viewModel: MapViewModel) {
    var darkMode by remember { mutableStateOf(false) }
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
                .size(350.dp, 685.dp)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(24.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            //verticales spacing zwischen den Items im Column
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 60.dp, width = 100.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                Switch(checked = viewModel.isDarkMode, onCheckedChange = { viewModel.toggleDarkMode(it) })
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .size(height = 60.dp, width = 100.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(10.dp),

                ) {
                (Text(modifier = Modifier.align(Alignment.CenterStart),
                    text ="Nutzungsbedingungen",
                    color = MaterialTheme.colorScheme.secondary))
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .size(height = 60.dp, width = 100.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(10.dp),

                ) {
                (Text(modifier = Modifier.align(Alignment.CenterStart),
                    text ="Hilfe",
                    color = MaterialTheme.colorScheme.secondary))
            }

            //Button zum schließen des Screens, da es ein größeres Window ist
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.Black))
            { Text("Schließen") }
            Text(
                "App created by Artem, Dustin & Türker",
                color = Color.Gray.copy(alpha = 0.5f),
                fontSize = 8.sp
            )
        }
    }
}
