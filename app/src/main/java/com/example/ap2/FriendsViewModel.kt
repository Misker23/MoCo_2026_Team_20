package com.example.ap2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FriendsViewModel : ViewModel() {
    // Interner veränderbarer Zustand
    private val _friendsCountLive = MutableLiveData<Int>(15) // Initialer Wert

    // Öffentliches LiveData für die View
    val friendsCountLive: LiveData<Int> = _friendsCountLive

    fun addFriend() {
        val currentCount = _friendsCountLive.value ?: 0
        _friendsCountLive.postValue(currentCount + 1)
    }
}