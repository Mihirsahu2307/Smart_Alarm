package com.example.alarmapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c[Calendar.HOUR_OF_DAY]
        val minute = c[Calendar.MINUTE]
        val instance = TimePickerDialog(activity,R.style.TimePickerTheme, activity as OnTimeSetListener?, hour, minute, DateFormat.is24HourFormat(activity))

        instance.setButton(TimePickerDialog.BUTTON_POSITIVE, Utils.setColoredText("OK", "#000000"), instance)
        instance.setButton(TimePickerDialog.BUTTON_NEGATIVE, Utils.setColoredText("CANCEL", "#000000"), instance)
        // By using style for the TimePickerDialog, somehow the text on the buttons turned white and
        // there seems to be no working method to change button text color. So, found a hacky way of doing it

        return instance
    }
}