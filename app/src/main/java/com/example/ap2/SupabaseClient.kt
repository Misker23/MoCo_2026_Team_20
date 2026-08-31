package com.example.ap2

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

val supabase = createSupabaseClient(
    supabaseUrl = "http://192.168.2.30:8000",
    supabaseKey = "850181e4652dd023b7a98c58ae0d2d34bd487ee0cc3254aed6eda37307425907"
) {
    install(Postgrest) {
        // Zwingt den Parser, zusätzliche Datenbank-Spalten wie 'position' zu ignorieren
        serializer = KotlinXSerializer(Json { ignoreUnknownKeys = true })
    }
    install(Auth)
    install(Storage)
}