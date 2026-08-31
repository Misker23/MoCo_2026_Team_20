package com.example.ap2.friendsScreenComposables

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ap2.data.remote.FriendDto
import com.example.ap2.data.remote.MarkerDto
import com.example.ap2.data.repositories.FriendsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FriendsViewModel(
    private val repository: FriendsRepository = FriendsRepository()
) : ViewModel() {

    val friendsList = mutableStateListOf<FriendDto>()
    val friendRequests = mutableStateListOf<FriendDto>()
    val sentFriendRequests = mutableStateListOf<FriendDto>()
    val myMarkers = mutableStateListOf<MarkerDto>()
    val currentFriendSharedMarkerIds = mutableStateListOf<String>()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    val filteredFriends = searchText
        .combine(MutableStateFlow(friendsList)) { text, friends ->
            if (text.isBlank()) {
                friends
            } else {
                friends.filter { it.displayName.contains(text, ignoreCase = true) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = friendsList
        )

    fun onSearchQueryChange(text: String) {
        _searchText.value = text
    }

    fun fetchFriends() {
        viewModelScope.launch {
            val result = repository.fetchFriends()
            friendsList.clear()
            friendsList.addAll(result)
        }
    }

    fun fetchFriendRequests() {
        viewModelScope.launch {
            val result = repository.fetchFriendRequests()
            friendRequests.clear()
            friendRequests.addAll(result)
        }
    }

    fun fetchSentFriendRequests() {
        viewModelScope.launch {
            val result = repository.fetchSentFriendRequests()
            sentFriendRequests.clear()
            sentFriendRequests.addAll(result)
        }
    }

    // Alias für fetchFriendRequests oder fetchSentFriendRequests?
    // In FriendsScreenCompose wird beides + fetchPendingRequests aufgerufen.
    // Ich implementiere es als Alias für fetchFriendRequests für den Moment.
    fun fetchPendingRequests() {
        fetchFriendRequests()
    }

    fun fetchMyMarkers() {
        viewModelScope.launch {
            val result = repository.fetchMyMarkers()
            myMarkers.clear()
            myMarkers.addAll(result)
        }
    }

    suspend fun addFriendByUsername(username: String, color: String): String? {
        val error = repository.addFriendByUsername(username, color)
        if (error == null) {
            fetchSentFriendRequests()
        }
        return error
    }

    fun acceptFriendRequest(requesterId: String) {
        viewModelScope.launch {
            repository.acceptFriendRequest(requesterId)
            fetchFriendRequests()
            fetchFriends()
        }
    }

    fun rejectFriendRequest(requesterId: String) {
        viewModelScope.launch {
            repository.rejectFriendRequest(requesterId)
            fetchFriendRequests()
        }
    }

    fun cancelFriendRequest(friendId: String) {
        viewModelScope.launch {
            repository.cancelFriendRequest(friendId)
            fetchSentFriendRequests()
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            repository.removeFriend(friendId)
            fetchFriends()
        }
    }

    fun fetchSharedMarkerIdsForFriend(friendId: String) {
        viewModelScope.launch {
            val result = repository.fetchSharedMarkerIdsForFriend(friendId)
            currentFriendSharedMarkerIds.clear()
            currentFriendSharedMarkerIds.addAll(result)
        }
    }

    fun saveMarkerSharesForFriend(friendId: String, markerIds: List<String>) {
        viewModelScope.launch {
            repository.saveMarkerSharesForFriend(friendId, markerIds)
        }
    }
}
