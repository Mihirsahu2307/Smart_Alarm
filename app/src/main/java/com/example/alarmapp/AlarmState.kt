package com.example.alarmapp

import android.util.Log


// If alarm ringtone is currently playing, redirect user to DismissAlarmActivity
object AlarmState {
    var isPlaying = false
    var id: Int? = null
    var message: String? = null
    var name: String? = null
    var difficulty: Int? = null

    fun updateState(id: Int = 0, message: String = "null", name: String = "null", difficulty: Int = 0) {
        val TAG = "AlarmState"
        Log.d(TAG, "updateState: Toggled state: $isPlaying")
        isPlaying = isPlaying xor true
        this.id = id
        this.message = message
        this.name = name
        this.difficulty = difficulty
    }
}