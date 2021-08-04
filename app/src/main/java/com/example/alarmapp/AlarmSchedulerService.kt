package com.example.alarmapp

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.lang.Integer.min
import java.util.*


class AlarmSchedulerService : Service(), MediaPlayer.OnCompletionListener {
    private var ringtone: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private val myTAG = "AlarmService"
    private val notificationID = 2307
    private var pendingIntent: PendingIntent? = null
    private var alarmManager: AlarmManager? = null
    private var notification: Notification? = null

    val SCHEDULE_ALARM = "action to schedule alarms"
    val CANCEL_ALARM = "action to cancel alarm"
    val PLAY_RINGTONE = "action for intent"
    val CHANNEL_1_ID = "Alarm Notification channel"
    val CHANNEL_2_ID = "Prevents service from being killed"
    val REPEAT_KEY = "repeatDays"
    val MESSAGE_KEY = "extra message"
    val NAME_KEY = "alarm name"
    val TIME_KEY = "time"
    val STATE_KEY = "state of alarm"
    val ID_KEY = "alarm id"
    val DIFFICULTY_KEY = "problem difficulty"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?

        val navigationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
                this, 22330077, navigationIntent, PendingIntent.FLAG_ONE_SHOT)

        notification = NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setContentTitle("Currently Running")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Don't close the app")
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build()

        startForeground(notificationID, notification)
        super.onCreate()
    }



    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent!!.action == CANCEL_ALARM) {
            releaseMediaPlayer()

            val navigationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    this, 22330077, navigationIntent, PendingIntent.FLAG_ONE_SHOT)

            notification = NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setContentTitle("Currently Running")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText("Don't close the app")
                    .setContentIntent(pendingIntent)
                    .setOnlyAlertOnce(true)
                    .build()

            startForeground(notificationID, notification)
        } else if(intent.action == SCHEDULE_ALARM) {
            val id = intent.extras!!.getInt(ID_KEY)
            val time = intent.extras!!.getString(TIME_KEY)
            val name = intent.extras!!.getString(NAME_KEY)
            val repeat = intent.extras!!.getString(REPEAT_KEY)
            val message = intent.extras!!.getString(MESSAGE_KEY)
            val difficulty = intent.extras!!.getInt(DIFFICULTY_KEY)
            val state = intent.extras!!.getBoolean(STATE_KEY)

            val alarm = Alarm(name!!, time!!, repeat!!, message!!, state, id, difficulty)
            startAlert(alarm)
        } else if(intent.action == PLAY_RINGTONE) {
            val id = intent.extras?.getInt(ID_KEY)
            val time = intent.extras?.getString(TIME_KEY)
            val name = intent.extras?.getString(NAME_KEY)
            var message = intent.extras?.getString(MESSAGE_KEY)
            val difficulty = intent.extras?.getInt(DIFFICULTY_KEY)
            message = message?.substring(0, min(message.length, 15))

            Log.d(myTAG, "Received! time: $time, name: $name")
            Log.d(myTAG, "Alarm id: $id")

            vibrate(applicationContext)
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, (0.8 * (audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)!!)).toInt(), 0);

            ringtone = MediaPlayer.create(applicationContext, R.raw.alarm_ringtone)
            ringtone?.isLooping = true
            ringtone?.start()

//            ringtone?.setOnCompletionListener {
//                this.onCompletion(ringtone)
//            }

            AlarmState.updateState(id!!, message!!, name!!, difficulty!!)

            val cancelAlarmIntent = Intent(this, DismissAlarmActivity::class.java)
            cancelAlarmIntent.putExtra(ID_KEY, id)
            cancelAlarmIntent.putExtra(MESSAGE_KEY, message)
            cancelAlarmIntent.putExtra(NAME_KEY, name)
            cancelAlarmIntent.putExtra(DIFFICULTY_KEY, difficulty)
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    this, 420, cancelAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            notification = NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setContentTitle(name)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText("$message ~ Click to dismiss.")
                    .setContentIntent(pendingIntent)
                    .build()

            startForeground(notificationID, notification)
        }
        return START_STICKY
    }

    private fun releaseMediaPlayer() {
        if(ringtone != null) {
            ringtone?.release()
            ringtone = null
        }
    }

    private fun vibrate(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(2307, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(2307)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun startAlert(alarm: Alarm) {
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.action = PLAY_RINGTONE
        intent.putExtra(TIME_KEY, alarm.alarmTime)
        intent.putExtra(NAME_KEY, alarm.alarmName)
        intent.putExtra(MESSAGE_KEY, alarm.alarmMessage)
        intent.putExtra(ID_KEY, alarm.id)
        intent.putExtra(DIFFICULTY_KEY, alarm.difficulty)

        if(!alarm.state) {
            cancelAllPendingIntents(alarm.id, intent)
            Log.d(myTAG, "Cancelled all alarms for ${alarm.id}")
        } else {
            val formattedTime = Utils.timeTo24HrFormat(alarm.alarmTime)
            val hr = formattedTime.substring(0, 2).toInt()
            val minute = formattedTime.substring(3, 5).toInt()

            val calendar = Calendar.getInstance()

            Log.d(myTAG, "Hr: $hr and Minute: $minute")

            if(alarm.repeat == "0000000") {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.HOUR_OF_DAY, hr)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1)
                }

                val timeInMillis = calendar.timeInMillis

                pendingIntent = PendingIntent.getBroadcast(
                        applicationContext, alarm.id * 8, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                if (System.currentTimeMillis() < timeInMillis) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager?.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                timeInMillis,
                                pendingIntent
                        )
                    }
                }
                Log.d(myTAG, "Created alarm ${alarm.id}," +
                        " goes off in ${timeInMillis - Calendar.getInstance().timeInMillis}")
            } else {
                for(i in 0..6) {
                    if(alarm.repeat[i] == '1') {
                        calendar.set(Calendar.DAY_OF_WEEK, (i + 1) % 7 + 1)
//                    calendar.timeInMillis = System.currentTimeMillis()
                        calendar.set(Calendar.HOUR_OF_DAY, hr)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        val timeDifference = calendar.timeInMillis - Calendar.getInstance().timeInMillis

                        // NOTE: Avoid division when accuracy is extremely necessary,
                        // use multiplication instead like so:
                        if(timeDifference > 7.toLong() * 86400000) {
                            calendar.add(Calendar.DAY_OF_MONTH, -7)
                            Log.d(myTAG, "Scheduled time over 7 days for day $i")
                        }

                        // if alarm time has already passed, increment day by 7
                        if(calendar.timeInMillis < System.currentTimeMillis()) {
                            Log.d(myTAG, "Alarm for day: $i scheduled for next week")
                            calendar.add(Calendar.DAY_OF_YEAR, 7)
                        }

                        createAlarmForDay(i, alarm.id, calendar.timeInMillis, intent)
                    }
                }
            }
        }
    }

    private fun createAlarmForDay(day: Int, id: Int, timeInMillis: Long, intent: Intent) {
        pendingIntent = PendingIntent.getBroadcast(
                applicationContext, id * (day + 1), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager?.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis,
                AlarmManager.INTERVAL_DAY * 7, pendingIntent)

        Log.d(myTAG, "Request code: ${id * (day + 1)}" +
                " goes off first in ${timeInMillis - Calendar.getInstance().timeInMillis}")
    }

    private fun cancelAllPendingIntents(id: Int, intent: Intent?) {
        for(i in 0..7) {
            val pendingIntent = PendingIntent.getBroadcast(
                    applicationContext,
                    id * (i + 1),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager?.cancel(pendingIntent)
            Log.d(myTAG, "Cancelled, request code: ${id * (i + 1)}")
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mp?.start()
    }
}