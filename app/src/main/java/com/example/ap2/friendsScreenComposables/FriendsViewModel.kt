package com.example.ap2.friendsScreenComposables

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ap2.data_models.FriendDto
import com.example.ap2.data_models.MarkerDto
import com.example.ap2.data_models.ProfileDto
import com.example.ap2.data_models.SharedMarkerDto
import com.example.ap2.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel zur Verwaltung von Freundschaften und der Freigabe von Markern an Freunde.
 * Handhabt Datenbank-Operationen für Freundeslisten, Suche und Freigabe-Rechte (`shared_markers`).
 */
class FriendsViewModel : ViewModel() {

    /** Beobachtbare Liste der aktuell bestätigten Freunde des angemeldeten Nutzers. */
    val friendsList = mutableStateListOf<FriendDto>()

    /** Liste aller eigenen, selbst erstellten Marker. */
    val myMarkers = mutableStateListOf<MarkerDto>()

    /** Enthält die Marker-IDs, die aktuell für den ausgewählten Freund freigegeben sind. */
    val currentFriendSharedMarkerIds = mutableStateListOf<String>()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    val filteredFriends = searchText
        .combine(snapshotFlow { friendsList.toList() }) { text, friends ->
            if (text.isBlank()) {
                friends
            } else {
                friends.filter { it.displayName.contains(text, ignoreCase = true) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    /**
     * Lädt alle bestätigten Freundschaften (`status = 'accepted'`) des Nutzers aus Supabase,
     * inklusive zugehörigem Benutzerprofil und zugewiesener Marker-Farbe.
     */
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

    /**
     * Sucht anhand eines Benutzernamens nach einem Profil und fügt dieses als Freund hinzu.
     *
     * @param usernameInput Der exakte Benutzername des anzufragenden Nutzers.
     * @param selectedColor Die zugewiesene Farbe für die Marker dieses Freundes auf der Karte.
     * @return `null` bei Erfolg oder eine Fehlermeldung als [String] zur Anzeige im Dialog.
     */
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

            fetchFriends()
            return null
        } catch (e: Exception) {
            Log.e("Friends", "Fehler beim Hinzufügen: ${e.message}", e)
            return "Fehler beim Hinzufügen: ${e.message}"
        }
    }

    /**
     * Löscht eine Freundschaftsbeziehung zwischen dem Nutzer und der angegebenen [friendUserId].
     */
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

    /**
     * Lädt alle eigenen Marker, die der angemeldete Nutzer erstellt hat.
     */
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

    /**
     * Ruft die IDs aller Marker ab, die der Nutzer derzeit für einen bestimmten Freund freigegeben hat.
     *
     * @param friendUserId Die ID des betreffenden Freundes.
     */
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

    /**
     * Aktualisiert die Marker-Freigaben für einen Freund, indem alle alten Einträge in `shared_markers`
     * gelöscht und die neuen [selectedMarkerIds] neu eingefügt werden.
     *
     * @param friendUserId ID des Freundes.
     * @param selectedMarkerIds Eine Liste mit allen Marker-IDs, die freigegeben werden sollen.
     */
    suspend fun saveMarkerSharesForFriend(friendUserId: String, selectedMarkerIds: List<String>) {
        try {
            supabase.postgrest.from("shared_markers").delete {
                filter { eq("friend_user_id", friendUserId) }
            }

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

    fun onSearchQueryChange(newQuery: String) {
        _searchText.value = newQuery
    }


}