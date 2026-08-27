package com.example.ap2.sensor_repositories

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

//Schrittzähler, Handyausrichtung und Kompass

class MotionRepository(context: Context) {
    //Hier wird das SensorManager initialisiert
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    //Schrittzähler -Nachtrag: Wird mittlerweile über hinterlegte Distanz berechnet und ungefähr ausgegeben
    // wir konnten es nicht testen mit dem Emulator oder zumindest haben wir keinen Weg gefunden es zu testen

//    fun getStepCountUpdates(): Flow<Float> = callbackFlow {
//        //Schrittzähler Sensor initialisieren
//        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
//        //Listener erstellen
//        val listener = object : SensorEventListener {
//            override fun onSensorChanged(event: SensorEvent?) {
//                // Sobald neue Daten kommen, werden diese updates und dem Flow übergeben
//                event?.values?.get(0)?.let { trySend(it) }
//            }
//            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//        }
//        //Sensor einschalten
//        if (sensor != null) {
//            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
//        }
//        //Sensor ausschalten
//        awaitClose {
//            sensorManager.unregisterListener(listener)
//        }
//    }

    //Blickrichtung/Handyausrichtung
    fun getRotationUpdates(): Flow<Float> = callbackFlow {
        //Handyausrichtung/Blickrichtung Sensor initialisieren
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        //Listener erstellen
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                //Sobald neue Faten kommen, werden diese umgerechnet und dem Flow übergeben
                event?.values?.let { rotationVector ->
                    // 1. Umrechnung der Rohdaten in eine Rotationsmatrix
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)

                    // 2. Umrechnung der Matrix in Orientierungswerte (Radiant)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)

                    // 3. Den Azimuth (Index 0) in Grad umrechnen (0 bis 360 oder -180 bis 180)
                    var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()

                    azimuth = (azimuth + 360) % 360
                    // Wert in den Flow schicken
                    trySend(azimuth)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        //Sensor einschalten
        if (sensor != null) {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        //Sensor ausschalten
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}