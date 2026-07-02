package com.example.ap2.HomeScreenComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
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
fun POIWindow(bottomPadding: Dp, onDismiss: () -> Unit) {
    var description by remember { mutableStateOf("") }
    //Popup Fenster für POIWindow
    Popup(
        //damit es unten und mittig öffnet
        alignment = Alignment.Center,
        //damit das Fenster auch geschlossen werden kann
        onDismissRequest = onDismiss,
        //damit aus dem Fenster klicken das Popup Fenster schließt
        properties = PopupProperties(focusable = true)
    ) {
        Box(modifier = Modifier
            .padding(bottom = bottomPadding)
            .size(350.dp, 750.dp)
            .background(Color.LightGray.copy(alpha = 0.7f), RoundedCornerShape(16.dp)
            )
        ){
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        placeholder = { Text("Search POI") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ){
                        Button(onClick = onDismiss) { Text("Close") }
                    }
                }
            ) { contentPadding ->
                Box(modifier = Modifier.padding(contentPadding)) {
                    LazyColumn() {
                        item{
                            POIExpendableCard("My POIs", "My POI List")
                        }
                        items(10) { i ->
                            POIExpendableCard("Friend ${i + 1}", "Shared POI List of this friend")
                        }
                    }

                }
            }

        }
    }
}


@Preview
@Composable
fun POIWindowPreview() {
    POIWindow(bottomPadding = 0.dp, onDismiss = {})
}