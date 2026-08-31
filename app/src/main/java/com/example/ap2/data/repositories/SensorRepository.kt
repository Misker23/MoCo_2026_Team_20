package com.example.ap2.data.repositories

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.ap2.data.sensors.CameraSensorDataSource
import com.example.ap2.data.sensors.MotionSensorDataSource
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.Executor

class SensorRepository(
    context: Context,
    private val motionDataSource: MotionSensorDataSource = MotionSensorDataSource(context),
    private val cameraDataSource: CameraSensorDataSource = CameraSensorDataSource(context)
) {
    // --- Motion / Kompass ---
    fun getRotationUpdates(): Flow<Float> = motionDataSource.getRotationUpdates()

    // --- Kamera ---
    suspend fun getCameraProvider(): ProcessCameraProvider = cameraDataSource.getCameraProvider()

    fun takePhoto(
        imageCapture: ImageCapture,
        executor: Executor,
        onImageCaptured: (Uri) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        cameraDataSource.takePhoto(imageCapture, executor, onImageCaptured, onError)
    }
}