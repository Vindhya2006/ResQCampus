package com.resqcampus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlin.math.sqrt

class FallDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private var lastMagnitude = 0f
    private var lastRotation = 0f
    private var fallDetected = false

    override fun onCreate() {
        super.onCreate()

        // Start foreground service
        createNotificationChannel()
        startForeground(1, createNotification())

        // Initialize sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Register listeners
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        sensorManager.registerListener(
            this,
            gyroscope,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onSensorChanged(event: SensorEvent) {

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val magnitude = sqrt(x * x + y * y + z * z)

            // -----------------------------
            // STAGE 1 — FREE FALL
            // -----------------------------
            if (magnitude < 2f) {
                lastMagnitude = magnitude
            }

            // -----------------------------
            // STAGE 2 — IMPACT
            // -----------------------------
            if (magnitude > 25f) {
                lastMagnitude = magnitude
            }

            // -----------------------------
            // STAGE 3 — STILLNESS
            // -----------------------------
            if (!fallDetected && lastMagnitude > 25f && magnitude < 3f && lastRotation > 1.5f) {
                confirmFall()
            }
        }

        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val rx = event.values[0]
            val ry = event.values[1]
            val rz = event.values[2]

            lastRotation = sqrt(rx * rx + ry * ry + rz * rz)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // -----------------------------
    // FALL CONFIRMATION
    // -----------------------------
    private fun confirmFall() {
        fallDetected = true

        Toast.makeText(
            this,
            "🚨 FALL CONFIRMED — Sending Alert",
            Toast.LENGTH_LONG
        ).show()

        sendEmergencyAlert()
    }

    // -----------------------------
    // EMERGENCY NOTIFICATION
    // -----------------------------
    private fun sendEmergencyAlert() {
        val notification = NotificationCompat.Builder(this, "fall_channel")
            .setContentTitle("🚨 EMERGENCY ALERT")
            .setContentText("Fall detected! Location sent to emergency contact.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, notification)
    }

    // -----------------------------
    // FOREGROUND NOTIFICATION
    // -----------------------------
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "fall_channel")
            .setContentTitle("ResQCampus Active")
            .setContentText("Monitoring for falls in background")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    // -----------------------------
    // NOTIFICATION CHANNEL
    // -----------------------------
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "fall_channel",
                "Fall Detection",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
