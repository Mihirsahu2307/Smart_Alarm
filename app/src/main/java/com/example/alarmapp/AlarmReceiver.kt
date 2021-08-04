package com.example.alarmapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class AlarmReceiver : BroadcastReceiver() {
    private val myTAG = "AlarmReceiver"

    val SCHEDULE_ALARM = "action to schedule alarms"
    val CANCEL_ALARM = "action to cancel alarm"
    val PLAY_RINGTONE = "action for intent"
    val REPEAT_KEY = "repeatDays"
    val MESSAGE_KEY = "extra message"
    val NAME_KEY = "alarm name"
    val TIME_KEY = "time"
    val STATE_KEY = "state of alarm"
    val ID_KEY = "alarm id"
    val DIFFICULTY_KEY = "problem difficulty"

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent: Intent? = Intent(context, AlarmSchedulerService::class.java)
        if(intent.action == PLAY_RINGTONE) {
            val time = intent.extras?.getString(TIME_KEY)
            val name = intent.extras?.getString(NAME_KEY)
            val message = intent.extras?.getString(MESSAGE_KEY)
            val difficulty = intent.extras?.getInt(DIFFICULTY_KEY)
            val repeat = intent.extras?.getString(REPEAT_KEY)
            val state = intent.extras?.getBoolean(STATE_KEY)
            val id = intent.extras?.getInt(ID_KEY)
            Log.d(myTAG, "Received! time: $time, name: $name")

            serviceIntent!!.action = PLAY_RINGTONE
            serviceIntent.putExtra(TIME_KEY, time)
            serviceIntent.putExtra(NAME_KEY, name)
            serviceIntent.putExtra(REPEAT_KEY, repeat)
            serviceIntent.putExtra(MESSAGE_KEY, message)
            serviceIntent.putExtra(DIFFICULTY_KEY, difficulty)
            serviceIntent.putExtra(STATE_KEY, state)
            serviceIntent.putExtra(ID_KEY, id)

            Log.d(myTAG, "Alarm id: $id")
        } else if(intent.action == CANCEL_ALARM) {
            serviceIntent!!.action = CANCEL_ALARM
        } else if(intent.action == SCHEDULE_ALARM) {
            val time = intent.extras?.getString(TIME_KEY)
            val name = intent.extras?.getString(NAME_KEY)
            val message = intent.extras?.getString(MESSAGE_KEY)
            val difficulty = intent.extras?.getInt(DIFFICULTY_KEY)
            val repeat = intent.extras?.getString(REPEAT_KEY)
            val state = intent.extras?.getBoolean(STATE_KEY)
            val id = intent.extras?.getInt(ID_KEY)

            serviceIntent?.putExtra(TIME_KEY, time)
            serviceIntent?.putExtra(NAME_KEY, name)
            serviceIntent?.putExtra(REPEAT_KEY, repeat)
            serviceIntent?.putExtra(MESSAGE_KEY, message)
            serviceIntent?.putExtra(DIFFICULTY_KEY, difficulty)
            serviceIntent?.putExtra(STATE_KEY, state)
            serviceIntent?.putExtra(ID_KEY, id)
            serviceIntent!!.action = SCHEDULE_ALARM
        }

        context.startService(serviceIntent)
    }
}