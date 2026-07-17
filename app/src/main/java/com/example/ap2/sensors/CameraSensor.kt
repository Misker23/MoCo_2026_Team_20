package com.example.ap2.sensors

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//Kamera

class CameraRepository(private val context: Context) {

    //Pausiert Kamera bis sie genutzt wird
    suspend fun getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
        // Holt eine Instanz des Providers (asynchron)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        // Wartet, bis der Provider fertig geladen ist
        cameraProviderFuture.addListener({
            // Gibt den fertigen Provider an das ViewModel zurück
            continuation.resume(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(context))
    }

    //Foto machen
    fun takePhoto(
        imageCapture: ImageCapture, // Das Werkzeug zum Auslösen
        executor: Executor,         // Der Thread, auf dem das Speichern läuft
        onImageCaptured: (Uri) -> Unit, // Erfolg: "Hier ist der Pfad zum Bild"
        onError: (ImageCaptureException) -> Unit // Fehler: "Etwas ging schief"
    ) {
        // 1. Speicherort festlegen (Temporäre Datei im Cache des Handys)
        val photoFile = File(
            context.cacheDir, // Speicher im Cache-Ordner der App
            "marker_${System.currentTimeMillis()}.jpg" // Eindeutiger Dateiname mit Zeitstempel
        )

        // 2. Einstellungen für die Speicherung (Wohin soll die Datei?)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // 3. Den Befehl zum Auslösen geben
        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                // Bei Erfolg: Die Datei in eine URI umwandeln und zurückgeben
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    onImageCaptured(savedUri)
                }

                // Bei Fehlern (z.B. Speicher voll)
                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }
}