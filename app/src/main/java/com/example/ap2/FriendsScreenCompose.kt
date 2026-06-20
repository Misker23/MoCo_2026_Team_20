package com.example.ap2

import android.R.attr.onClick
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ap2.FriendsScreenComposables.AddFriendButton
import com.example.ap2.FriendsScreenComposables.FriendBox

@Composable
fun FriendsScreen() {
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            // Surface gibt der gesamten Sektion einen Hintergrund und Schatten,
            // so wirkt es wie eine zusammenhängende "Bar"
            Surface(
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp) // Abstand zum Rand der Bar
                ) {
                    Row() {
//                        Icon(
//                            painter = painterResource(R.drawable.arrow_top_left),
//                            contentDescription = null,
//                            modifier = Modifier
//                                .size(24.dp)
//                                .align(Alignment.CenterVertically)
//                                .clickable(
//                                    interactionSource = remember { MutableInteractionSource() },
//                                    indication = null,
//                                ) {}
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search Friends") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                        )
                    }
                    // Ein kleiner Abstand zwischen Textfeld und Button
                    Spacer(modifier = Modifier.height(8.dp))
                    AddFriendButton()
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ){
                Button(onClick = {}) { Text("Back") }
            }
        }
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(15) { i ->
                    FriendBox()
                }
            }
        }
    }
}

@Preview
@Composable
fun FriendsScreenPreview() {
    FriendsScreen()
}
