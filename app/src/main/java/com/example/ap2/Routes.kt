package com.example.ap2

import kotlinx.serialization.Serializable

// Changed to 'data object' to fix SerializationException for type-safe navigation
@Serializable
data object HomeRoute

// Changed to 'data object' to fix SerializationException for type-safe navigation
@Serializable
data object FriendsRoute