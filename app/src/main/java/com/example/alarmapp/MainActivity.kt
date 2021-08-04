package com.example.alarmapp

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alarmapp.AlarmState.difficulty
import com.example.alarmapp.AlarmState.id
import com.example.alarmapp.AlarmState.message
import com.example.alarmapp.AlarmState.name
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.list_layout.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity(), View.OnTouchListener {
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var alarmList: ArrayList<Alarm> = ArrayList()
    private var alarmAdapter: AlarmAdapter? = null
    private val myTAG: String = "MainActivity"
    private var gestureDetector: GestureDetector? = null
    private var dX: Float? = null
    private var dY: Float? = null
    private var screenWidth: Float? = null
    private var screenHeight: Float? = null

    val SCHEDULE_ALARM = "action to schedule alarms"
    val ADD_ALARM = "add alarm to list"
    val NAVIGATE_TO_MAIN = "navigate back to main"
    val PLAY_RINGTONE = "action for intent"
    val REPEAT_KEY = "repeatDays"
    val MESSAGE_KEY = "extra message"
    val NAME_KEY = "alarm name"
    val TIME_KEY = "time"
    val ID_KEY = "alarm id"
    val STATE_KEY = "state of alarm"
    val DIFFICULTY_KEY = "problem difficulty"
    val SAVED_LIST_KEY = "ALARM_KEY"
    val SAVED_ID_KEY = "id key"

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity_menu, menu)

        val menuItem1 = menu?.findItem(R.id.add_alarm)
        val menuItem2 = menu?.findItem(R.id.sort_time)
        val menuItem3 = menu?.findItem(R.id.sort_name)

        menuItem1?.setOnMenuItemClickListener {
            navigateToAlarmBuilder(-1)
//            val intent = Intent(this, DismissAlarmActivity::class.java)
//            startActivity(intent)
//            createAlarmForDay(1, 69, Calendar.getInstance().timeInMillis + 2307)
            return@setOnMenuItemClickListener true
        }

        menuItem2?.setOnMenuItemClickListener {
            val tmp = alarmList.sortedWith(
                    compareBy<Alarm> { Utils.timeTo24HrFormat(it.alarmTime) }.thenBy { it.alarmName })
            alarmList.clear()
            alarmList.addAll(tmp)
            alarmAdapter?.notifyDataSetChanged()
            return@setOnMenuItemClickListener true
        }

        menuItem3?.setOnMenuItemClickListener {
            val tmp = alarmList.sortedWith(
                    compareBy<Alarm> { it.alarmName }.thenBy { Utils.timeTo24HrFormat(it.alarmTime) })
            alarmList.clear()
            alarmList.addAll(tmp)
            alarmAdapter?.notifyDataSetChanged()
            return@setOnMenuItemClickListener true
        }

        return super.onCreateOptionsMenu(menu)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_layout)

        @RequiresApi(Build.VERSION_CODES.M)
        if(isAppBlacklisted()) {
            Toast.makeText(this, "Please allow background running", Toast.LENGTH_LONG).show()
            openPowerSettings()
        }

        if(AlarmState.isPlaying) {
            Log.d(myTAG, "Playing ringtone")
            val cancelAlarmIntent = Intent(this, DismissAlarmActivity::class.java)
            cancelAlarmIntent.putExtra(ID_KEY, id)
            cancelAlarmIntent.putExtra(MESSAGE_KEY, message)
            cancelAlarmIntent.putExtra(NAME_KEY, name)
            cancelAlarmIntent.putExtra(DIFFICULTY_KEY, difficulty)

            startActivity(cancelAlarmIntent)
        }


        val receiver = ComponentName(applicationContext, AlarmReceiver::class.java)
        this.packageManager.setComponentEnabledSetting(
                receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        )

        startService(Intent(this, AlarmSchedulerService::class.java))
        // Start the service at the end otherwise the subsequent methods in onCreate may not be called

        gestureDetector = GestureDetector(this, SingleTapConfirm())
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels.toFloat()
        // NOTE: For the animation, we need the screenWidth in pixels not in dp

        loadData()
        if(intent.action == ADD_ALARM)
            getDataFromAlarmBuilder()
        else if(intent.action == NAVIGATE_TO_MAIN) {
            val id = intent.extras?.getInt(ID_KEY)
            for(i in 0 until alarmList.size) {
                if(alarmList[i].id == id && alarmList[i].repeat == "0000000") {
                    alarmList[i].state = false
                    Log.d(myTAG, "Alarm id: $id found")
                    break
                }
            }
        }
        inflateAlarmList()
        assignIDToPreviousAlarms()
        manageAlarms()
        add_alarm_fab.setOnTouchListener(this)
    }

    override fun onPause() {
        super.onPause()
        saveData()
    }

    private fun assignIDToPreviousAlarms() {
        for(item in alarmList) {
            Log.d(myTAG, "Item name: ${item.alarmName} id: ${item.id}")
            if(item.id == -1) {
                item.id = Utils.getUniqueID()!!
            }
        }
    }

    private fun getDataFromAlarmBuilder() {
        val time = intent.extras?.getString(TIME_KEY)

        Log.d(myTAG, "time: $time")
        if(time != "noAlarm" && time != null) {
            val name = intent.extras?.getString(NAME_KEY)
            val message = intent.extras?.getString(MESSAGE_KEY)
            val repeatString = intent.extras?.getString(REPEAT_KEY)
            val id = intent.extras?.getInt(ID_KEY)
            val difficulty = intent.extras?.getInt(DIFFICULTY_KEY)

            Log.d(myTAG, "IDs used so far: " + Utils.usedId)
            Log.d(myTAG, "Current ID: $id")

            var found = false
            for(item in alarmList) {
                if(item.id == id) {
                    item.state = true
                    item.alarmName = name!!
                    item.repeat = repeatString!!
                    item.alarmMessage = message!!
                    item.alarmTime = time
                    item.difficulty = difficulty!!
                    Log.d(myTAG, "ID found")
                    found = true
                    break
                }
            }
            if(!found)
                alarmList.add(Alarm(name!!, time, repeatString!!, message!!, true, id!!, difficulty!!))

            scheduleAlarm(Alarm(name!!, time, repeatString!!, message!!, true, id!!, difficulty!!))
        }
    }

    private fun navigateToAlarmBuilder(position: Int) {
        val intent = Intent(this, AlarmBuilderActivity::class.java)
        if(position != -1) {
            val item: Alarm = alarmList[position]
            intent.putExtra(REPEAT_KEY, item.repeat)
            intent.putExtra(MESSAGE_KEY, item.alarmMessage)
            intent.putExtra(NAME_KEY, item.alarmName)
            intent.putExtra(TIME_KEY, item.alarmTime)
            intent.putExtra(ID_KEY, item.id)
            intent.putExtra(DIFFICULTY_KEY, item.difficulty)
        }
        startActivity(intent)
    }

    private fun inflateAlarmList() {
        linearLayoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = linearLayoutManager
        recycler_view.addItemDecoration(DividerItemDecoration(this,
            DividerItemDecoration.VERTICAL))

        Log.d(myTAG, "onCreate, size: " + alarmList.size)

        alarmAdapter = AlarmAdapter(this, alarmList)
        recycler_view.adapter = alarmAdapter

        alarmAdapter!!.setOnSwitchToggleListener(object : AlarmAdapter.OnSwitchToggleListener {
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun onToggle(position: Int, state: Boolean) {
                // onToggle gets triggered even when a new alarm item is shown when the user scrolls
                if(state != alarmList[position].state) {
                    alarmList[position].state = state
                    scheduleAlarm(alarmList[position])
                    Log.d(myTAG, "at Position: $position")
                }
            }
        })

        // NOTE: setOnLongClickListener can't be called directly for recycler view, so we need
        // to create another interface for that

        alarmAdapter!!.setOnLongPressListener(object : AlarmAdapter.OnLongPressListener {
            override fun onClick(position: Int) {
                createOptionsDialog(position)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun manageAlarms() {
        for(i in 0 until alarmList.size) {
            val alarm = alarmList[i]
            if(alarm.state) {
                scheduleAlarm(alarm)
            }
        }
    }

    private fun scheduleAlarm(alarm: Alarm) {
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.action = SCHEDULE_ALARM
        intent.putExtra(TIME_KEY, alarm.alarmTime)
        intent.putExtra(NAME_KEY, alarm.alarmName)
        intent.putExtra(REPEAT_KEY, alarm.repeat)
        intent.putExtra(MESSAGE_KEY, alarm.alarmMessage)
        intent.putExtra(DIFFICULTY_KEY, alarm.difficulty)
        intent.putExtra(STATE_KEY, alarm.state)
        intent.putExtra(ID_KEY, alarm.id)

        sendBroadcast(intent)
    }

    private fun createOptionsDialog(position: Int) {
        val builder = AlertDialog.Builder(this@MainActivity, R.style.AlertDialog)

        val options = arrayOf(Utils.setColoredText("Delete Alarm", "#FFFFFF"),
                Utils.setColoredText("Edit Alarm", "#FFFFFF"))

        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    deleteItem(position)
                }
                1 -> {
                    navigateToAlarmBuilder(position)
                }
            }
        }

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.color.dark_grey)

        dialog.show()
    }

    private fun deleteItem(position: Int) {
        scheduleAlarm(alarmList[position])
        val id = alarmList[position].id

        alarmList.removeAt(position)
        alarmAdapter?.notifyItemRemoved(position)
        alarmAdapter?.notifyItemRangeChanged(position, alarmList.size)

        Utils.usedId.remove(id)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun openPowerSettings() {
        val myIntent = Intent()
        myIntent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        startActivity(myIntent)
    }

    private fun isAppBlacklisted(): Boolean {
        val pwrm = this.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = this.packageName
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !pwrm.isIgnoringBatteryOptimizations(name)
        } else {
            TODO("VERSION.SDK_INT < M")
        }
    }

    private fun saveData() {
        val sharedPreferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this)

        val editor = sharedPreferences.edit()

        val gson = Gson()
        val json1 = gson.toJson(alarmList)
        editor.putString(SAVED_LIST_KEY, json1)
        val json2 = gson.toJson(Utils.usedId)
        editor.putString(SAVED_ID_KEY, json2)

        editor.apply()
        Log.d(myTAG, "Saved Data: current HashSet = ${Utils.usedId}")
        Log.d(myTAG, "Saved Data; current alarmList size: " + alarmList.size)
    }

    private fun loadData() {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val json1 = sharedPreferences.getString(SAVED_LIST_KEY, null)
        val json2 = sharedPreferences.getString(SAVED_ID_KEY, null)


        val type1 = object : TypeToken<List<Alarm>>() {}.type
        val type2 = object : TypeToken<HashSet<Int>>() {}.type

        alarmList.clear()
        if(json1 != null)
            alarmList = gson.fromJson(json1, type1)

//        Utils.usedId.clear()
        if(json2 != null)
            Utils.usedId.addAll(gson.fromJson(json2, type2))

        if(alarmList.isEmpty()) {
            alarmList = ArrayList()
        }

        Log.d(myTAG, "Loaded Data; current alarmList size: " + alarmList.size)
        Log.d(myTAG, "Loaded Data; current hash set size: " + Utils.usedId.size)
    }

    // onTouch method handles both click and drag events using GestureDetector
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if (gestureDetector!!.onTouchEvent(event)) {
            if(alarmList.size < 100) {
                navigateToAlarmBuilder(-1)
            } else {
                Toast.makeText(this, "Alarm Limit Exceeded!", Toast.LENGTH_LONG).show()
            }
            return true
        } else {
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view!!.x - event.rawX
//                dY = view.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    var newX: Float = event.rawX + dX!!
                    newX = max(0F, newX)
                    val maxX = screenWidth!! - view!!.width
                    newX = min(maxX , newX)
                    view.animate()
                            .x(newX)
//                    .y(event.rawY + dY!!)
                            .setDuration(0)
                            .start()
                }
                else -> return false
            }
        }
        return false
    }

    // This class implements the GestureDetector.SimpleOnGestureListener() interface so we can
    // pass an instance of this class to get the callback function
    private class SingleTapConfirm : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent?): Boolean {
            return true
        }
    }
}


/*
 * This is the second version of the app. It doesn't differ much from V.1 on the frontend
 *
 * PROGRESS & TASKS:
 * 30/4/21: BEGINNING (Learnt Kotlin and programmed basic stuff)
 * 1/5/21: Implemented OnSwitchToggledInterface
 * 9/5/21: Created the basic layouts for MainActivity and AlarmBuilder
 * 14/5/21: Made the FAB in MainActivity draggable as it used to interfere with other views
 * 14/5/21: Added a customized spinner to set difficulty of problems
 * 26/5/21: App is completely functional as an alarm app, but doesn't work when it is closed
 * 27/5/21: Completed the DismissAlarmActivity. App works completely fine unless it's killed (V.1 built)
 * 29/5/21: Shifted all alarm scheduling functions to the Service class.
 * 4/6/21: App displays a notification to run a foreground service and asks user to not close the app.
 * 4/6/21: Completed final state of V.2
 *
 * Task1: Correctly toggle an Alarm item's state using the interface (CLEARED, 2/5/21)
 * Task2: Create another activity so that user can add a new alarm (CLEARED, 4/5/21)
 * Task3: Connect MainActivity, Receiver and Service classes to play alarm ringtone (CLEARED, 12/5/21)
 * Task4: Create another activity to cancel alarms and display math problems (CLEARED, 27/5/21)
 * Task5 (IMP): Write methods to make alarms repeat correctly (CLEARED, 25/5/21)
 * Task6: Make the AlarmSchedulerService a background service so that alarms go off even when
 *        the app is closed (NOT CLEARED, Used a foreground service with notification instead, 6/4/21)
 * Task7: Create a function that iterates over all alarms and calls startAlert() for each
 *        alarm that is on. (CLEARED, 25/5/21)
 * Task8: Make the ID generator in Utils to only return prime numbers >= 11 (CLEARED, 25/5/21)
 * Task9: Alarms are not getting cancelled although ID is received and passed correctly. Error lies
 *        in the cancelAllPendingIntents() method. Fix it. (CLEARED, 25/5/21)
 *        Comments: Forgot to cancel non repeating alarms; removed the cancelAllPendingIntents() method
 */