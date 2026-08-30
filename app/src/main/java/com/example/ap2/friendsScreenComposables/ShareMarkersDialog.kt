package com.example.ap2.friendsScreenComposables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ap2.data_models.MarkerDto

/**
 * Ein Auswahldialog zur Freigabe eigener Marker an einen bestimmten Freund.
 * Zeigt eine Checkliste aller eigenen Marker mit deren Freigabestatus an.
 *
 * @param friendName Name des Freundes für den Titel.
 * @param myMarkers Liste der eigenen Marker des Nutzers.
 * @param initialSharedIds Liste der aktuell bereits freigegebenen Marker-IDs.
 * @param onDismiss Callback beim Schließen des Dialogs ohne Speichern.
 * @param onSave Callback mit den neu ausgewählten Marker-IDs beim Bestätigen.
 */
@Composable
fun ShareMarkersDialog(
    friendName: String,
    myMarkers: List<MarkerDto>,
    initialSharedIds: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    val selectedIds = remember { mutableStateListOf<String>().apply { addAll(initialSharedIds) } }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.secondary,
        onDismissRequest = onDismiss,
        title = { Text(text = "Marker mit $friendName teilen") },
        text = {
            if (myMarkers.isEmpty()) {
                Text("Du hast noch keine eigenen Marker auf der Karte erstellt.", color = MaterialTheme.colorScheme.secondary)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(myMarkers) { marker ->
                        val markerId = marker.id ?: return@items
                        val isChecked = selectedIds.contains(markerId)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedIds.remove(markerId)
                                    else selectedIds.add(markerId)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedIds.add(markerId)
                                    else selectedIds.remove(markerId)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = marker.description?.takeIf { it.isNotBlank() } ?: "Marker ohne Name",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = { onSave(selectedIds.toList()) },
                enabled = myMarkers.isNotEmpty()
            ) {
                Text("Speichern", color = MaterialTheme.colorScheme.secondary)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Abbrechen", color = MaterialTheme.colorScheme.secondary)
            }
        }
    )
}