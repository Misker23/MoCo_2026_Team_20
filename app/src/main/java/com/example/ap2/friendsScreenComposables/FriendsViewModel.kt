package com.example.ap2.friendsScreenComposables

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.ap2.data_models.FriendDto
import com.example.ap2.data_models.MarkerDto
import com.example.ap2.data_models.ProfileDto
import com.example.ap2.data_models.SharedMarkerDto
import com.example.ap2.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class FriendsViewModel : ViewModel() {

    val friendsList = mutableStateListOf<FriendDto>()

    suspend fun fetchFriends() {
        val user = supabase.auth.currentUserOrNull() ?: return
        try {
            val list = supabase.postgrest.from("friendships")
                .select(Columns.raw("friend_id, color, profiles(id, username)")) {
                    filter { eq("user_id", user.id) }
                    filter { eq("status", "accepted") }
                }
                .decodeList<FriendDto>()

            friendsList.clear()
            friendsList.addAll(list)
        } catch (e: Exception) {
            Log.e("Friends", "Fehler beim Laden der Liste: ${e.message}", e)
        }
    }

    // Gibt null bei Erfolg zurück, oder einen Fehlertext bei Problemen
    suspend fun addFriendByUsername(usernameInput: String, selectedColor: String): String? {
        val user = supabase.auth.currentUserOrNull() ?: return "Nicht angemeldet."
        val cleanName = usernameInput.trim()

        if (cleanName.isBlank()) return "Bitte einen Benutzernamen eingeben."

        try {
            // 1. Suche nach dem Profil
            val profiles = supabase.postgrest.from("profiles")
                .select(Columns.raw("id, username")) {
                    filter { eq("username", cleanName) }
                }
                .decodeList<ProfileDto>()

            val targetProfile = profiles.firstOrNull()
                ?: return "Kein Nutzer mit dem Namen '$cleanName' gefunden."

            if (targetProfile.id == user.id) {
                return "Du kannst dich nicht selbst hinzufügen."
            }

            // 2. Prüfen, ob bereits befreundet
            val existing = supabase.postgrest.from("friendships")
                .select(Columns.raw("friend_id")) {
                    filter { eq("user_id", user.id) }
                    filter { eq("friend_id", targetProfile.id) }
                }
                .decodeList<FriendDto>()

            if (existing.isNotEmpty()) {
                return "Dieser Nutzer ist bereits in deiner Freundesliste."
            }

            // 3. Freundschaft anlegen
            val newFriend = mapOf(
                "user_id" to user.id,
                "friend_id" to targetProfile.id,
                "status" to "accepted",
                "color" to selectedColor
            )
            supabase.postgrest.from("friendships").insert(newFriend)

            // Liste neu laden
            fetchFriends()
            return null // null bedeutet: Erfolg!
        } catch (e: Exception) {
            Log.e("Friends", "Fehler beim Hinzufügen: ${e.message}", e)
            return "Fehler beim Hinzufügen: ${e.message}"
        }
    }

    suspend fun removeFriend(friendUserId: String) {
        val user = supabase.auth.currentUserOrNull() ?: return
        try {
            supabase.postgrest.from("friendships").delete {
                filter { eq("user_id", user.id) }
                filter { eq("friend_id", friendUserId) }
            }
            friendsList.removeAll { it.friend_id == friendUserId }
        } catch (e: Exception) {
            Log.e("Friends", "Fehler beim Entfernen: ${e.message}")
        }
    }

    suspend fun shareMarkerWithFriend(markerId: String, friendUserId: String): Boolean {
        return try {
            val shareData = mapOf(
                "marker_id" to markerId,
                "friend_user_id" to friendUserId
            )
            supabase.postgrest.from("shared_markers").insert(shareData)
            true // Erfolgreich geteilt
        } catch (e: Exception) {
            Log.e("Marker", "Fehler beim Teilen des Markers: ${e.message}")
            false
        }
    }

    val myMarkers = mutableStateListOf<MarkerDto>()
    val currentFriendSharedMarkerIds = mutableStateListOf<String>()

    // 1. Eigene Marker laden
    suspend fun fetchMyMarkers() {
        val user = supabase.auth.currentUserOrNull() ?: return
        try {
            val markers = supabase.postgrest.from("markers")
                .select(Columns.raw("*")) {
                    filter { eq("user_id", user.id) }
                }
                .decodeList<MarkerDto>()
            myMarkers.clear()
            myMarkers.addAll(markers)
        } catch (e: Exception) {
            Log.e("FriendsViewModel", "Fehler beim Laden eigener Marker: ${e.message}")
        }
    }

    // 2. Bereits geteilte Marker-IDs für einen bestimmten Freund laden
    suspend fun fetchSharedMarkerIdsForFriend(friendUserId: String) {
        try {
            val shares = supabase.postgrest.from("shared_markers")
                .select(Columns.list("marker_id", "friend_user_id")) {
                    filter { eq("friend_user_id", friendUserId) }
                }
                .decodeList<SharedMarkerDto>()

            currentFriendSharedMarkerIds.clear()
            currentFriendSharedMarkerIds.addAll(shares.map { it.markerId })
        } catch (e: Exception) {
            Log.e("FriendsViewModel", "Fehler beim Laden der Freigaben: ${e.message}")
        }
    }

    // 3. Neue Auswahl in 'shared_markers' speichern
    suspend fun saveMarkerSharesForFriend(friendUserId: String, selectedMarkerIds: List<String>) {
        try {
            // Alte Freigaben für diesen Freund löschen
            supabase.postgrest.from("shared_markers").delete {
                filter { eq("friend_user_id", friendUserId) }
            }

            // Neue Freigaben einfügen
            if (selectedMarkerIds.isNotEmpty()) {
                val newShares = selectedMarkerIds.map { markerId ->
                    SharedMarkerDto(markerId = markerId, friendUserId = friendUserId)
                }
                supabase.postgrest.from("shared_markers").insert(newShares)
            }
        } catch (e: Exception) {
            Log.e("FriendsViewModel", "Fehler beim Speichern der Freigaben: ${e.message}")
        }
    }
}