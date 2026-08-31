package com.example.ap2.friendsScreenComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.ap2.data.remote.FriendDto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search

@Composable
fun FriendsScreenCompose(
    viewModel: FriendsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var friendForSharing by remember { mutableStateOf<FriendDto?>(null) }
    val searchText by viewModel.searchText.collectAsState()
    val filteredFriends by viewModel.filteredFriends.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchFriends()
        viewModel.fetchFriendRequests()
        viewModel.fetchSentFriendRequests()
        viewModel.fetchPendingRequests()
        viewModel.fetchMyMarkers() // Lädt eigene Marker für den Freigabe-Dialog
    }

    // VOLLBILD-CONTAINER
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER MIT ZURÜCK & ADD BUTTON ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Zurück zum Hauptbildschirm",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Text(
                    text = "Deine Freunde",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Freund hinzufügen",
                        tint = Color(0xFF2196F3)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = { Text("Suche") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    selectionColors = TextSelectionColors(
                        handleColor = MaterialTheme.colorScheme.secondary,
                        backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    ),
                    focusedIndicatorColor = Color.LightGray,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- FREUNDSCHAFTSANFRAGEN + FREUNDESLISTE ---
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {

                // Freundschaftsanfragen ganz oben
                if (viewModel.friendRequests.isNotEmpty()) {
                    item {
                        Text(
                            text = "Freundschaftsanfragen",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(viewModel.friendRequests) { request ->

                        FriendRequestItem(
                            request = request,
                            onAccept = {
                                coroutineScope.launch {
                                    viewModel.acceptFriendRequest(request.user_id!!)
                                }
                            },
                            onReject = {
                                coroutineScope.launch {
                                    viewModel.rejectFriendRequest(request.user_id!!)
                                }
                            }
                        )
                    }
                }

                if (viewModel.sentFriendRequests.isNotEmpty()) {

                    item {
                        Text(
                            text = "Ausstehende Anfragen",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                top = 16.dp,
                                bottom = 8.dp
                            )
                        )
                    }

                    items(viewModel.sentFriendRequests) { request ->

                        SentFriendRequestItem(
                            request = request,
                            onCancel = {
                                coroutineScope.launch {
                                    viewModel.cancelFriendRequest(request.friend_id)
                                }
                            }
                        )
                    }
                }

                // Überschrift für Freunde
                item {
                    Text(
                        text = "Deine Freunde",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            top = 16.dp,
                            bottom = 8.dp
                        )
                    )
                }

                // Freunde
                if (viewModel.friendsList.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                            ){
                            Text(
                                text = "Du hast noch keine Freunde.",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(filteredFriends) { friend: FriendDto ->

                        FriendCardItem(
                            friend = friend,
                            onShare = {
                                coroutineScope.launch {
                                    viewModel.fetchSharedMarkerIdsForFriend(friend.friend_id)
                                    friendForSharing = friend
                                }
                            },
                            onRemove = {
                                coroutineScope.launch {
                                    viewModel.removeFriend(friend.friend_id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG: FREUND HINZUFÜGEN ---
    if (showAddDialog) {
        AddFriendDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { username, chosenColor ->
                viewModel.addFriendByUsername(username, chosenColor)
            }
        )
    }

    // --- DIALOG: MARKER TEILEN (Nutzt die ShareMarkersDialog.kt Komponente) ---
    friendForSharing?.let { friend ->
        ShareMarkersDialog(
            friendName = friend.displayName,
            myMarkers = viewModel.myMarkers,
            initialSharedIds = viewModel.currentFriendSharedMarkerIds,
            onDismiss = { friendForSharing = null },
            onSave = { updatedSelectedIds ->
                coroutineScope.launch {
                    viewModel.saveMarkerSharesForFriend(friend.friend_id, updatedSelectedIds)
                    friendForSharing = null
                }
            }
        )
    }
}

// --- ITEM CARD FOR SINGLE FRIEND ---
@Composable
fun FriendCardItem(
    friend: FriendDto,
    onShare: () -> Unit,
    onRemove: () -> Unit
) {
    val friendColor = remember(friend.color) {
        try {
            Color(android.graphics.Color.parseColor(friend.color ?: "#2196F3"))
        } catch (e: Exception) {
            Color(0xFF2196F3)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Online-Indikator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (friend.is_online == true) Color.Green else Color.Gray,
                            CircleShape
                        )
                )

                // Marker-Farbe des Freundes
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(friendColor, CircleShape)
                )

                Column {
                    Text(
                        text = friend.displayName,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (friend.is_online == true) "Online" else "Offline",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Teilen-Button
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Marker freigeben",
                        tint = Color(0xFF2196F3)
                    )
                }

                // Löschen-Button
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Entfernen",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Composable
fun FriendRequestItem(
    request: FriendDto,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "${request.displayName} möchte mit dir befreundet sein.",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(Color.Green)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Annehmen"
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text("Annehmen", color = Color.Black)
                }

                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(Color.Red)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Ablehnen"
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text("Ablehnen", color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun SentFriendRequestItem(
    request: FriendDto,
    onCancel: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Anfrage an ${request.displayName}",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Wartet auf Annahme",
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color.Red)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Anfrage abbrechen"
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text("Anfrage abbrechen", color = Color.Black)
            }
        }
    }
}

// --- DIALOG: FREUND HINZUFÜGEN ---
@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onConfirm: suspend (username: String, color: String) -> String?
) {
    val coroutineScope = rememberCoroutineScope()
    var usernameInput by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#2196F3") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val colorPalette = listOf(
        "#2196F3", "#E91E63", "#9C27B0", "#4CAF50",
        "#FF9800", "#00BCD4", "#FFEB3B", "#FF5722"
    )

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.primary,
        onDismissRequest = onDismiss,
        title = { Text("Freund hinzufügen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = usernameInput,
                    colors = TextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.secondary,
                        selectionColors = TextSelectionColors(
                            handleColor = MaterialTheme.colorScheme.secondary,
                            backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        ),
                        focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedTextColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary,
                    ),
                    onValueChange = {
                        usernameInput = it
                        errorMessage = null
                    },
                    label = { Text("Benutzername") },
                    placeholder = { Text("z. B. user2") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text("Marker-Farbe wählen:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colorPalette) { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, CircleShape)
                                .border(
                                    width = if (selectedColor == hex) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        val error = onConfirm(usernameInput, selectedColor)
                        isLoading = false
                        if (error != null) {
                            errorMessage = error
                        } else {
                            onDismiss()
                        }
                    }
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF2196F3))
                } else {
                    Text("Hinzufügen", color = Color.Black)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(Color.Red)
                ) {
                Text("Abbrechen", color = Color.Black)
            }
        }
    )
}