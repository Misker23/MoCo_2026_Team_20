package com.example.ap2

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

val supabase = createSupabaseClient(
    supabaseUrl = "http://192.168.0.110:8000",
    supabaseKey = "sb_publishable_ACJWlzQHlZjBrEguHvfOxg_3BJgxAaH"
) {
    install(Postgrest) {
        // Zwingt den Parser, zusätzliche Datenbank-Spalten wie 'position' zu ignorieren
        serializer = KotlinXSerializer(Json { ignoreUnknownKeys = true })
    }
    install(Auth)
    install(Storage)
}