package com.example.alarmapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// This receiver provokes MainActivity to call scheduleAlarms for all the active alarms in list
// When the system reboots

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent!!.action == "android.intent.action.BOOT_COMPLETED") {

        }
    }

}