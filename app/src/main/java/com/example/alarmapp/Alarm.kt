package com.example.alarmapp

data class Alarm(
        var alarmName: String = "Default",
        var alarmTime: String,
        var repeat: String = "0000000",
        var alarmMessage: String,
        var state: Boolean = false,
        var id: Int = -1,
        var difficulty: Int = 0
)