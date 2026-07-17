package com.example.ap2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ap2.FriendsScreenComposables.AddFriendButton
import com.example.ap2.FriendsScreenComposables.FriendBox
import androidx.compose.ui.tooling.preview.Preview
import com.example.ap2.viewmodels.FriendsViewModel
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel = viewModel(), // ViewModel injizieren
    onNavigateBack: () -> Unit // Callback für Navigation
) {
    var description by remember { mutableStateOf("") }

    // LiveData aus dem ViewModel als Compose-State beobachten
    val friendsCount by viewModel.friendsCountLive.observeAsState(initial = 15)

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search Friends") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // ViewModel-Aktion an den Button übergeben
                    AddFriendButton(onClick = { viewModel.addFriend() })
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
                // Callback für den Back-Stack aufrufen
                Button(onClick = onNavigateBack) { Text("Back") }
            }
        }
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Dynamische Anzahl aus dem LiveData-State verwenden
                items(friendsCount) { i ->
                    FriendBox()
                }
            }
        }
    }
}

@Preview
@Composable
fun FriendsScreenPreview() {
    // Ein leeres Lambda {}
    FriendsScreen(onNavigateBack = {})
}
