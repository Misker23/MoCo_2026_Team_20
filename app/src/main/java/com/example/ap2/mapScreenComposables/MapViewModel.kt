package com.example.ap2.mapScreenComposables

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ap2.data_models.MapMarkerUiState
import com.example.ap2.data_models.MarkerDto
import com.example.ap2.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.Position
import java.util.UUID

/**
 * Aufzählung der verschiedenen Zustände des Interaktionsmodus auf der Karte.
 */
enum class MapMode {
    /** Standard-Kartenansicht: Marker können betrachtet werden. */
    DEFAULT,
    /** Modus aktiv: Der Nutzer soll einen Punkt auf der Karte für einen neuen Marker wählen. */
    PLACING_MARKER,
    /** Marker-Position gesetzt: Bestätigungs-Overlay für den finalen Speicher-Vorgang sichtbar. */
    CONFIRMING
}

/**
 * Zentrale ViewModel-Klasse für die Karten-Steuerung.
 *
 * Diese Klasse fungiert als Bindeglied zwischen der UI (MapScreen) und Supabase.
 * Sie verwaltet den aktuellen Kartenmodus, den Standort des Nutzers,
 * das Laden/Speichern von Markern sowie den Upload von Bildern in den Supabase Storage.
 */
class MapViewModel : ViewModel() {

    // --- STATE VARIABLEN ---
    /** Aktuelle GPS-Position des Nutzers auf der Karte. */
    var userPosition by mutableStateOf(Position(longitude = 7.6261, latitude = 51.2180))

    /** Der derzeit aktive Interaktionsmodus. */
    var currentMode by mutableStateOf(MapMode.DEFAULT)

    /** Referenz auf den aktuell ausgewählten Marker (z. B. für Detailansicht). */
    var selectedMarker by mutableStateOf<MarkerDto?>(null)

    /** Vorläufige Position, wenn ein neuer Marker platziert, aber noch nicht bestätigt wurde. */
    var temporaryPosition by mutableStateOf<Position?>(null)

    // --- DATEN-LISTEN ---
    /** Liste der Marker-Datenmodelle (direkt von Supabase). */
    val markerList = mutableStateListOf<MarkerDto>()

    /** Aufbereitete Liste für die UI-Darstellung (MapMarkerUiState). */
    val mapMarkers = mutableStateListOf<MapMarkerUiState>()

    /** Trigger-Variable: Wird bei Änderung inkrementiert, um die Kamera-Animation in der UI auszulösen. */
    var centerOnUserTrigger by mutableIntStateOf(0)
        private set

    // --- MAP STEUERUNG ---

    /**
     * Setzt die Kamera zurück auf den Standort des Nutzers.
     * Schließt zudem die Detailansicht eines Markers.
     */
    fun centerOnUserLocation() {
        selectedMarker = null
        centerOnUserTrigger++
    }

    /** Aktiviert den Modus zum Platzieren eines neuen Markers. */
    fun startPlacingMode() {
        currentMode = MapMode.PLACING_MARKER
    }

    /**
     * Handler für Klicks auf die Karte.
     * Wenn im [MapMode.PLACING_MARKER], wird die Position als [temporaryPosition] gespeichert
     * und der Modus auf [MapMode.CONFIRMING] gewechselt.
     */
    fun handleMapClick(position: Position) {
        if (currentMode == MapMode.PLACING_MARKER) {
            temporaryPosition = position
            currentMode = MapMode.CONFIRMING
        }
    }

    /** Bricht den Erstell- oder Platzierungsprozess ab. */
    fun cancelPlacing() {
        temporaryPosition = null
        currentMode = MapMode.DEFAULT
    }

    /** Hilfsfunktion zum Zurücksetzen auf den Default-Modus. */
    fun resetMode() {
        cancelPlacing()
    }

    // --- SUPABASE OPERATIONEN ---

    /**
     * Speichert einen neuen Marker in Supabase.
     *
     * 1. Falls [imageBytes] vorhanden: Upload in Supabase Storage (`marker-images` Bucket).
     * 2. Speichern des Marker-Objekts inkl. der resultierenden Bild-URL in der `markers` Tabelle.
     * 3. Refresh der Marker-Liste.
     */
    fun confirmMarker(
        context: Context,
        description: String = "Neuer Marker",
        color: String = "#2196F3",
        imageBytes: ByteArray? = null
    ) {
        val pos = temporaryPosition ?: return

        viewModelScope.launch {
            val currentUser = supabase.auth.currentUserOrNull() ?: return@launch

            try {
                var uploadedImageUrl: String? = null

                // Bild-Upload Logik
                if (imageBytes != null && imageBytes.isNotEmpty()) {
                    val fileName = "marker_${UUID.randomUUID()}.jpg"
                    val bucket = supabase.storage.from("marker-images")

                    bucket.upload(fileName, imageBytes, upsert = false)
                    uploadedImageUrl = bucket.publicUrl(fileName)
                }

                // DB-Insert
                val newMarker = buildJsonObject {
                    put("user_id", currentUser.id)
                    put("lat", pos.latitude)
                    put("lon", pos.longitude)
                    put("position", "POINT(${pos.longitude} ${pos.latitude})")
                    put("description", description)
                    put("color", color)
                    if (uploadedImageUrl != null) {
                        put("image_url", uploadedImageUrl)
                    }
                }

                supabase.postgrest.from("markers").insert(newMarker)

                Toast.makeText(context, "Marker gespeichert!", Toast.LENGTH_SHORT).show()
                cancelPlacing()
                loadMarkersForMap()

            } catch (e: Exception) {
                Log.e("MapViewModel", "Fehler beim Speichern: ${e.message}", e)
                Toast.makeText(context, "Fehler: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Setzt den ausgewählten Marker (Wird von der UI aufgerufen). */
    fun selectMarker(marker: MarkerDto?) {
        selectedMarker = marker
    }

    /** Aktualisiert die intern gespeicherte Position des Nutzers. */
    fun updateUserPosition(position: Position) {
        userPosition = position
    }

    /**
     * Lädt alle Marker aus der Supabase-Datenbank und aktualisiert die lokale [markerList].
     */
    suspend fun loadMarkersForMap() {
        val currentUser = supabase.auth.currentUserOrNull() ?: run {
            Log.e("MapViewModel", "CANCELLED: Kein User in Supabase Auth angemeldet!")
            return
        }

        try {
            Log.d("MapViewModel", "Starte Laden der Marker für User: ${currentUser.id}...")

            val rawMarkers = supabase.postgrest.from("markers")
                .select()
                .decodeList<MarkerDto>()

            markerList.clear()
            markerList.addAll(rawMarkers)

        } catch (e: Exception) {
            Log.e("MapViewModel", "EXCEPTION BEIM LADEN DER MARKER: ${e.message}", e)
        }
    }

    /**
     * Aktualisiert einen bestehenden Marker.
     *
     * Bei Änderung des Bildes: Upload eines neuen Bildes in den Storage
     * und Update der Datenbank-Einträge via PostgREST.
     */
    fun updateMarkerWithImage(
        markerId: String,
        description: String,
        color: String,
        oldImageUrl: String?,
        newImageBytes: ByteArray?
    ) {
        viewModelScope.launch {
            try {
                var finalImageUrl = oldImageUrl

                // Bild-Aktualisierung
                if (newImageBytes != null && newImageBytes.isNotEmpty()) {
                    val fileName = "marker_${UUID.randomUUID()}.jpg"
                    val bucket = supabase.storage.from("marker-images")

                    bucket.upload(fileName, newImageBytes, upsert = true)
                    finalImageUrl = bucket.publicUrl(fileName)
                }

                // DB-Update
                supabase.postgrest.from("markers").update({
                    set("description", description)
                    set("color", color)
                    set("image_url", finalImageUrl)
                }) {
                    filter {
                        eq("id", markerId)
                    }
                }

                loadMarkersForMap()

            } catch (e: Exception) {
                Log.e("MapViewModel", "Fehler beim Aktualisieren des Markers", e)
            }
        }
    }

    /** Löscht einen Marker dauerhaft aus der Datenbank anhand seiner ID. */
    fun deleteMarker(id: String) {
        viewModelScope.launch {
            try {
                supabase.postgrest.from("markers").delete {
                    filter { eq("id", id) }
                }
                selectedMarker = null
                loadMarkersForMap()
            } catch (e: Exception) {
                Log.e("MapViewModel", "Fehler beim Löschen: ${e.message}")
            }
        }
    }
}