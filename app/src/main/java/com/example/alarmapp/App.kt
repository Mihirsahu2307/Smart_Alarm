package com.example.alarmapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class App : Application() {
    val CHANNEL_1_ID = "Alarm Notification channel"
    val CHANNEL_2_ID = "Prevents service from being killed"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(
                    CHANNEL_1_ID,
                    "create alarm notification",
                    NotificationManager.IMPORTANCE_HIGH
            )

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            manager?.createNotificationChannel(channel1)

            val channel2 = NotificationChannel(
                    CHANNEL_2_ID,
                    "service protection",
                    NotificationManager.IMPORTANCE_HIGH
            )

            val manger2 = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            manger2?.createNotificationChannel(channel2)
        }
    }
}