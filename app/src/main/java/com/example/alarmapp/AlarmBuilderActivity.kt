package com.example.alarmapp

import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.activity_alarm_builder.*


class AlarmBuilderActivity: AppCompatActivity(),
        TimePickerDialog.OnTimeSetListener,
        AdapterView.OnItemSelectedListener {
    private val myTAG: String = "AlarmBuilder"
    private var formattedTime = "12:00 AM"
    private var difficulty = 0
    private val difficultyArray = arrayOf(
            "No Problems",
            "EASY: 5 easy problems eg (23 x 19)",
            "HARD: 5 difficult problems eg (59 x 68)"
    )

    val ADD_ALARM = "add alarm to list"
    val REPEAT_KEY = "repeatDays"
    val MESSAGE_KEY = "extra message"
    val NAME_KEY = "alarm name"
    val TIME_KEY = "time"
    val ID_KEY = "alarm id"
    val DIFFICULTY_KEY = "problem difficulty"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_builder)

        val id = getDataFromMainActivity()

        cancel_button.setOnClickListener {view ->
            val intent = Intent(this, MainActivity::class.java)
            intent.action = ADD_ALARM
            intent.putExtra(TIME_KEY, "noAlarm")
            startActivity(intent)
        }

        add_button.setOnClickListener {view ->
            sendDataToMainActivity(id)
        }

        time_picker_btn.setOnClickListener {view ->
            displayTimePicker()
        }

        display_time_tv.setOnClickListener {view ->
            displayTimePicker()
        }

        set_difficulty_spinner.onItemSelectedListener = this
        val spinnerAdapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item, difficultyArray)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        set_difficulty_spinner.adapter = spinnerAdapter
    }

    private fun getDataFromMainActivity(): Int {
        val time = intent.extras?.getString(TIME_KEY)
        var id: Int? = -1

        Log.d(myTAG, "time: $time")
        if(time != null) {
            val name = intent.extras?.getString(NAME_KEY)
            val message = intent.extras?.getString(MESSAGE_KEY)
            val repeatString = intent.extras?.getString(REPEAT_KEY)

            id = intent.extras?.getInt(ID_KEY, -1)
            difficulty = intent.extras?.getInt(DIFFICULTY_KEY)!!

            display_time_tv.text = time
            alarm_message_et.setText(message)
            alarm_tag_et.setText(name)
            initializeCheckBoxes(repeatString!!)
            set_difficulty_spinner.post(Runnable { set_difficulty_spinner.setSelection(difficulty) })

            add_button.text = "SAVE"
        }

        return id!!
    }

    private fun sendDataToMainActivity(id: Int) {
        val name = alarm_tag_et.text.toString()
        if(name != "") {
            val intent = Intent(this, MainActivity::class.java)
            intent.action = ADD_ALARM
            intent.putExtra(REPEAT_KEY, getRepeatString())
            intent.putExtra(MESSAGE_KEY, alarm_message_et.text.toString())
            intent.putExtra(NAME_KEY, name)
            intent.putExtra(TIME_KEY, formattedTime)
            intent.putExtra(DIFFICULTY_KEY, difficulty)
            if(id == -1) {
                val newID = Utils.getUniqueID()
                intent.putExtra(ID_KEY, newID)
                Log.d(myTAG, "ID not present in the list, new id: $newID")
            } else {
                intent.putExtra(ID_KEY, id)
            }

            startActivity(intent)
        } else {
            Toast.makeText(this, "Give the alarm a name!", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    private fun initializeCheckBoxes(repeatString: String) {
        if(repeatString == "0000000") {
            never_cb.isChecked = true
        } else {
            if(repeatString[0] == '1') mon_cb.isChecked = true
            if(repeatString[1] == '1') tue_cb.isChecked = true
            if(repeatString[2] == '1') wed_cb.isChecked = true
            if(repeatString[3] == '1') thu_cb.isChecked = true
            if(repeatString[4] == '1') fri_cb.isChecked = true
            if(repeatString[5] == '1') sat_cb.isChecked = true
            if(repeatString[6] == '1') sun_cb.isChecked = true
        }
    }

    private fun displayTimePicker() {
        val timePicker: DialogFragment = TimePickerFragment()
        timePicker.show(supportFragmentManager, "time picker")
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        // Time will be received in 24 hr format
        var time = "$hourOfDay:$minute"

        // minutes < 10 will be not preceded by 0
        if(time[time.length - 2] == ':') {
            time = time.substring(0, time.length - 1) + "0" + time[time.length - 1]
        }
        Log.d(myTAG, "Time: $time")
        formattedTime = Utils.timeTo12HrFormat(time)
        display_time_tv.text = formattedTime
    }

    private fun getRepeatString(): String? {
        var repeatString = ""
        repeatString = addDay(repeatString, mon_cb.isChecked)
        repeatString = addDay(repeatString, tue_cb.isChecked)
        repeatString = addDay(repeatString, wed_cb.isChecked)
        repeatString = addDay(repeatString, thu_cb.isChecked)
        repeatString = addDay(repeatString, fri_cb.isChecked)
        repeatString = addDay(repeatString, sat_cb.isChecked)
        repeatString = addDay(repeatString, sun_cb.isChecked)

        if(never_cb.isChecked) {
            repeatString = "0000000"
        }
        return repeatString
    }

    private fun addDay(s: String, isChecked: Boolean): String {
        var rep = s
        if(isChecked) {
            rep += "1"
        } else {
            rep += "0"
        }

        return rep
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        difficulty = position
        (parent!!.getChildAt(0) as TextView).setTextColor(Color.parseColor("#000000"))
//        (parent.getChildAt(0) as TextView).textSize = 24F
    }
}