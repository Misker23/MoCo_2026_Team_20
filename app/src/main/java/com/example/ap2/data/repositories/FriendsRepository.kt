package com.example.ap2.data.repositories

import com.example.ap2.data.remote.FriendDto
import com.example.ap2.data.remote.MarkerDto
import com.example.ap2.data.remote.ProfileDto
import com.example.ap2.data.remote.SharedMarkerDto
import android.util.Log
import com.example.ap2.data.remote.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns



class FriendsRepository {

    /**
     * Lädt alle bestätigten Freundschaften (`status = 'accepted'`) des Nutzers aus Supabase.
     */
    suspend fun fetchFriends(): List<FriendDto> {
        val user = supabase.auth.currentUserOrNull() ?: return emptyList()

        return try {
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

            val mappedReceived = received.map { friendship ->
                FriendDto(
                    user_id = friendship.user_id,
                    friend_id = friendship.user_id ?: "",
                    color = friendship.color,
                    profiles = friendship.profiles,
                    is_online = friendship.is_online
                )
            }

            sent + mappedReceived
        } catch (e: Exception) {
            Log.e("FriendsRepository", "Fehler beim Laden der Freundesliste: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Sucht anhand eines Benutzernamens nach einem Profil und fügt dieses als Freund hinzu.
     */
    suspend fun addFriendByUsername(usernameInput: String, selectedColor: String): String? {
        val user = supabase.auth.currentUserOrNull() ?: return "Nicht angemeldet."
        val cleanName = usernameInput.trim()

        if (cleanName.isBlank()) return "Bitte einen Benutzernamen eingeben."

        return try {
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

            val newFriend = mapOf(
                "user_id" to user.id,
                "friend_id" to targetProfile.id,
                "status" to "pending",
                "color" to selectedColor
            )
            supabase.postgrest.from("friendships").insert(newFriend)
            null
        } catch (e: Exception) {
            Log.e("FriendsRepository", "Fehler beim Hinzufügen: ${e.message}", e)
            "Fehler beim Hinzufügen: ${e.message}"
        }
    }

    suspend fun fetchFriendRequests(): List<FriendDto> {
        val user = supabase.auth.currentUserOrNull() ?: return emptyList()

        return try {
            val requests = supabase.postgrest.from("friendships")
                .select(Columns.raw("user_id, friend_id, color, status")) {
                    filter { eq("friend_id", user.id) }
                    filter { eq("status", "pending") }
                }
                .decodeList<FriendDto>()

            val resultList = mutableListOf<FriendDto>()
            for (request in requests) {
                val requesterId = request.user_id ?: continue
                val profile = supabase.postgrest.from("profiles")
                    .select(Columns.raw("id, username")) {
                        filter { eq("id", requesterId) }
                    }
                    .decodeList<ProfileDto>()
                    .firstOrNull()

                resultList.add(request.copy(profiles = profile))
            }
            resultList
        } catch (e: Exception) {
            Log.e("FriendsRepository", "Fehler beim Laden der Freundschaftsanfragen: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchSentFriendRequests(): List<FriendDto> {
        val user = supabase.auth.currentUserOrNull() ?: return emptyList()

        return try {
            supabase.postgrest.from("friendships")
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
        } catch (e: Exception) {
            Log.e("FriendsRepository", "Fehler beim Laden gesendeter Anfragen: ${e.message}", e)
            emptyList()
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
        } catch (e: Exception) {
            Log.e("FriendsRepository", "Fehler beim Abbrechen der Anfrage: ${e.message}", e)
        }
    }

    suspend fun acceptFriendRequest(requesterId: String) {
        val user = supabase.auth.currentUserOrNull() ?: return
        try {
            supabase.postgrest.from("friendships")
                .update({ set("status", "accepted") }) {
                    filter {
                        eq("user_id", requesterId)
                        eq("friend_id", user.id)
                        eq("status", "pending")
                    }
                }
        } catch (e: Exception) {
            Log.e("FriendsRepository", "Fehler beim Annehmen der Anfrage: ${e.message}", e)
        }
    }

    suspend fun rejectFriendRequest(requesterId: String) {
        val user = supabase.auth.currentUserOrNull() ?: return
        try {
            supabase.postgrest.from("friendships").delete {
                filter {
                    eq("user_id", requesterId)
                    eq("friend_id", user.id)
                    eq("status", "pending")
                }
            }
        } catch (e: Exception) {
            Log.e("FriendsRepository", "Fehler beim Ablehnen der Anfrage: ${e.message}", e)
        }
    }

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
        } catch (e: Exception) {
            Log.e("FriendsRepository", "Fehler beim Entfernen des Freundes: ${e.message}", e)
        }
    }

    suspend fun fetchMyMarkers(): List<MarkerDto> {
        val user = supabase.auth.currentUserOrNull() ?: return emptyList()
        return try {
            supabase.postgrest.from("markers")
                .select(Columns.raw("*")) {
                    filter { eq("user_id", user.id) }
                }
                .decodeList<MarkerDto>()
        } catch (e: Exception) {
            Log.e("FriendsRepository", "Fehler beim Laden eigener Marker: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchSharedMarkerIdsForFriend(friendUserId: String): List<String> {
        return try {
            val shares = supabase.postgrest.from("shared_markers")
                .select(Columns.list("marker_id", "friend_user_id")) {
                    filter { eq("friend_user_id", friendUserId) }
                }
                .decodeList<SharedMarkerDto>()

            shares.map { it.markerId }
        } catch (e: Exception) {
            Log.e("FriendsRepository", "Fehler beim Laden der Freigaben: ${e.message}", e)
            emptyList()
        }
    }

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
            Log.e("FriendsRepository", "Fehler beim Speichern der Freigaben: ${e.message}", e)
        }
    }
}