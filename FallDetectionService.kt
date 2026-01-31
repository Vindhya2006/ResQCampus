package com.resqcampus

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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

        createNotificationChannel()
        startForeground(1, createNotification())

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent) {

        // ACCELEROMETER
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val magnitude = sqrt(x * x + y * y + z * z)
            val diff = magnitude - lastMagnitude
            lastMagnitude = magnitude

            if (diff > 15) {
                lastMagnitude = diff
            }
        }

        // GYROSCOPE
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val rx = event.values[0]
            val ry = event.values[1]
            val rz = event.values[2]

            val rotation = sqrt(rx * rx + ry * ry + rz * rz)
            lastRotation = rotation
        }

        // FALL DECISION
        if (!fallDetected && lastMagnitude > 15 && lastRotation > 2.5f) {
            fallDetected = true
            triggerFall()
        }
    }

    private fun triggerFall() {
        Handler(Looper.getMainLooper()).postDelayed({
            sendEmergencyAlert()
        }, 10000)
    }

    private fun sendEmergencyAlert() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "fall_channel")
            .setContentTitle("🚨 Fall Detected!")
            .setContentText("Emergency alert triggered.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, notification)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

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

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "fall_channel")
            .setContentTitle("ResQCampus Active")
            .setContentText("Monitoring for falls in background")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }
}
