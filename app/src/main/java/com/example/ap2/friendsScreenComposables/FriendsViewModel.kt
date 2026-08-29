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

/**
 * ViewModel zur Verwaltung von Freundschaften und der Freigabe von Markern an Freunde.
 * Handhabt Datenbank-Operationen für Freundeslisten, Suche und Freigabe-Rechte (`shared_markers`).
 */
class FriendsViewModel : ViewModel() {

    /** Beobachtbare Liste der aktuell bestätigten Freunde des angemeldeten Nutzers. */
    val friendsList = mutableStateListOf<FriendDto>()

    val friendRequests = mutableStateListOf<FriendDto>()

    val sentFriendRequests = mutableStateListOf<FriendDto>()

    val pendingRequests = mutableStateListOf<FriendDto>()

    /** Liste aller eigenen, selbst erstellten Marker. */
    val myMarkers = mutableStateListOf<MarkerDto>()

    /** Enthält die Marker-IDs, die aktuell für den ausgewählten Freund freigegeben sind. */
    val currentFriendSharedMarkerIds = mutableStateListOf<String>()

    /**
     * Lädt alle bestätigten Freundschaften (`status = 'accepted'`) des Nutzers aus Supabase,
     * inklusive zugehörigem Benutzerprofil und zugewiesener Marker-Farbe.
     */
    suspend fun fetchFriends() {
        val user = supabase.auth.currentUserOrNull() ?: return

        try {
            // Freundschaften, die ich selbst gesendet habe
            val sent = supabase.postgrest.from("friendships")
                .select(
                    Columns.raw(
                        "user_id, friend_id, color, status, profiles!friendships_friend_id_fkey(id, username)"
                    )
                ) {
                    filter {
                        eq("user_id", user.id)
                        eq("status", "accepted")
                    }
                }
                .decodeList<FriendDto>()

            // Freundschaften, die ich erhalten habe
            val received = supabase.postgrest.from("friendships")
                .select(
                    Columns.raw(
                        "user_id, friend_id, color, status, profiles!friendships_user_profile_fkey(id, username)"
                    )
                ) {
                    filter {
                        eq("friend_id", user.id)
                        eq("status", "accepted")
                    }
                }
                .decodeList<FriendDto>()

            friendsList.clear()

            // Selbst gesendete Freundschaften
            friendsList.addAll(sent)

            // Erhaltene Freundschaften
            friendsList.addAll(
                received.map { friendship ->
                    FriendDto(
                        user_id = friendship.user_id,
                        friend_id = friendship.user_id ?: "",
                        color = friendship.color,
                        profiles = friendship.profiles,
                        is_online = friendship.is_online
                    )
                }
            )

        } catch (e: Exception) {
            Log.e(
                "Friends",
                "Fehler beim Laden der Freundesliste: ${e.message}",
                e
            )
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
                .select(Columns.raw("user_id, friend_id, status")) {
                    filter {
                        or {
                            and {
                                eq("user_id", user.id)
                                eq("friend_id", targetProfile.id)
                            }
                            and {
                                eq("user_id", targetProfile.id)
                                eq("friend_id", user.id)
                            }
                        }
                    }
                }
                .decodeList<FriendDto>()

            if (existing.isNotEmpty()) {
                val friendship = existing.first()

                return when (friendship.status) {
                    "accepted" -> "Dieser Nutzer ist bereits dein Freund."
                    "pending" -> "Für diesen Nutzer existiert bereits eine offene Anfrage."
                    else -> "Für diesen Nutzer existiert bereits eine Freundschaftsanfrage."
                }
            }

            // 3. Freundschaft anlegen
            val newFriend = mapOf(
                "user_id" to user.id,
                "friend_id" to targetProfile.id,
                "status" to "pending",
                "color" to selectedColor
            )
            supabase.postgrest.from("friendships").insert(newFriend)
            fetchSentFriendRequests()

            return null
        } catch (e: Exception) {
            Log.e("Friends", "Fehler beim Hinzufügen: ${e.message}", e)
            return "Fehler beim Hinzufügen: ${e.message}"
        }
    }

    suspend fun fetchFriendRequests() {
        val user = supabase.auth.currentUserOrNull() ?: return

        try {
            // Alle offenen Anfragen, die an den aktuellen Nutzer gerichtet sind
            val requests = supabase.postgrest.from("friendships")
                .select(Columns.raw("user_id, friend_id, color, status")) {
                    filter { eq("friend_id", user.id) }
                    filter { eq("status", "pending") }
                }
                .decodeList<FriendDto>()

            friendRequests.clear()

            // Zu jeder Anfrage den Namen des Absenders laden
            for (request in requests) {

                val requesterId = request.user_id ?: continue

                val profile = supabase.postgrest.from("profiles")
                    .select(Columns.raw("id, username")) {
                        filter { eq("id", requesterId) }
                    }
                    .decodeList<ProfileDto>()
                    .firstOrNull()

                friendRequests.add(
                    request.copy(
                        profiles = profile
                    )
                )
            }

        } catch (e: Exception) {
            Log.e(
                "Friends",
                "Fehler beim Laden der Freundschaftsanfragen: ${e.message}",
                e
            )
        }
    }

    suspend fun fetchSentFriendRequests() {
        val user = supabase.auth.currentUserOrNull() ?: return

        try {
            val requests = supabase.postgrest.from("friendships")
                .select(
                    Columns.raw(
                        "user_id, friend_id, color, status, profiles!friendships_friend_id_fkey(id, username)"
                    )
                ) {
                    filter {
                        eq("user_id", user.id)
                        eq("status", "pending")
                    }
                }
                .decodeList<FriendDto>()

            sentFriendRequests.clear()
            sentFriendRequests.addAll(requests)

        } catch (e: Exception) {
            Log.e(
                "Friends",
                "Fehler beim Laden der gesendeten Freundschaftsanfragen: ${e.message}",
                e
            )
        }
    }

    suspend fun fetchPendingRequests() {
        val user = supabase.auth.currentUserOrNull() ?: return

        try {
            val requests = supabase.postgrest
                .from("friendships")
                .select(
                    Columns.raw(
                        "user_id, friend_id, color, status, profiles!friendships_friend_id_fkey(id, username)"
                    )
                ) {
                    filter {
                        eq("user_id", user.id)
                        eq("status", "pending")
                    }
                }
                .decodeList<FriendDto>()

            pendingRequests.clear()
            pendingRequests.addAll(requests)

        } catch (e: Exception) {
            Log.e(
                "FriendsViewModel",
                "Fehler beim Laden ausgehender Anfragen: ${e.message}",
                e
            )
        }
    }

    suspend fun cancelFriendRequest(friendUserId: String) {
        val user = supabase.auth.currentUserOrNull() ?: return

        try {
            supabase.postgrest.from("friendships").delete {
                filter {
                    eq("user_id", user.id)
                    eq("friend_id", friendUserId)
                    eq("status", "pending")
                }
            }

            fetchSentFriendRequests()

        } catch (e: Exception) {
            Log.e(
                "Friends",
                "Fehler beim Abbrechen der Freundschaftsanfrage: ${e.message}",
                e
            )
        }
    }

    suspend fun acceptFriendRequest(requesterId: String) {
        val user = supabase.auth.currentUserOrNull() ?: return

        try {
            supabase.postgrest.from("friendships")
                .update({
                    set("status", "accepted")
                }) {
                    filter {
                        eq("user_id", requesterId)
                        eq("friend_id", user.id)
                        eq("status", "pending")
                    }
                }

            fetchFriendRequests()
            fetchFriends()

        } catch (e: Exception) {
            Log.e(
                "Friends",
                "Fehler beim Annehmen der Freundschaftsanfrage: ${e.message}",
                e
            )
        }
    }

    suspend fun rejectFriendRequest(requesterId: String) {
        val user = supabase.auth.currentUserOrNull() ?: return

        try {
            supabase.postgrest.from("friendships")
                .delete {
                    filter {
                        eq("user_id", requesterId)
                        eq("friend_id", user.id)
                        eq("status", "pending")
                    }
                }

            fetchFriendRequests()

        } catch (e: Exception) {
            Log.e(
                "Friends",
                "Fehler beim Ablehnen der Freundschaftsanfrage: ${e.message}",
                e
            )
        }
    }

    /**
     * Löscht eine Freundschaftsbeziehung zwischen dem Nutzer und der angegebenen [friendUserId].
     */
    suspend fun removeFriend(friendUserId: String) {
        val user = supabase.auth.currentUserOrNull() ?: return

        try {
            supabase.postgrest.from("shared_markers").delete {
                filter {
                    or {
                        eq("friend_user_id", user.id)
                        eq("friend_user_id", friendUserId)
                    }
                }
            }

            supabase.postgrest.from("friendships").delete {
                filter {
                    or {
                        and {
                            eq("user_id", user.id)
                            eq("friend_id", friendUserId)
                        }
                        and {
                            eq("user_id", friendUserId)
                            eq("friend_id", user.id)
                        }
                    }
                }
            }

            fetchFriends()

        } catch (e: Exception) {
            Log.e(
                "Friends",
                "Fehler beim Entfernen: ${e.message}",
                e
            )
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
}