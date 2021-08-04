package com.example.alarmapp

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.log


// Object in kotlin denotes a single static instance which is what we need for a utility class
object Utils {
    private const val myTAG = "Utils"
    private val repeatDaysList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val primesList = listOf(11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71,
        73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167,
        173, 179, 181, 191, 193, 197, 199, 211, 223, 227)

    @JvmStatic
    var usedId: HashSet<Int> = HashSet()

    @JvmStatic
    fun timeTo12HrFormat(time: String): String {
        var tenTo12 = false
        val lessThan12 = if(time[1] == ':') {
             true
        } else {
            val hr = time.substring(0, 2).toInt()
            Log.d(myTAG, "Hr: $hr")
            tenTo12 = hr in 10..12
            hr < 12
        }
        Log.d(myTAG, "Hr 10 to 12?: $tenTo12")

        val formattedTime = if(!lessThan12) {
            val hr = time.substring(0, 2).toInt()
            val formattedHour = hr - 12
            if(hr == 12) {
                return "$time PM"
            } else if(formattedHour in 10..12) {
                formattedHour.toString() + ":" + time.substring(3, 5) + " PM"
            } else {
                "0" + formattedHour.toString() + ":" + time.substring(3, 5) + " PM"
            }
        } else {
            if(!tenTo12) {
                "0$time AM"
            }
            else {
                "$time AM"
            }
        }

        Log.d(myTAG, "formatted time: $formattedTime")
        return formattedTime
    }

    @JvmStatic
    fun timeTo24HrFormat(time: String): String {
        // Input will always be of the form "HH:MM XM"
        var hr = time.substring(0, 2).toInt()
        val min = time.substring(3, 5).toInt()

        if(time[6] == 'P') {
            hr += 12
            if(time.substring(0, 2) == "12")
                hr = 12
        }
        val hourString = if(hr < 10) {
            "0$hr"
        } else {
            hr.toString()
        }
        val minString = if(min < 10) {
            "0$min"
        } else {
            min.toString()
        }

        Log.d(myTAG, "timeTo24HrFormat: $hourString:$minString")
        return "$hourString:$minString"
    }

    @JvmStatic
    fun getRepeatingDaysString(str: String): String {
        val repeatBooleanString: String = str
        var addDay: String?
        var addDays = ""

        when(repeatBooleanString) {
            "1111111" -> {
                addDays = "Everyday"
            }
            "0000000" -> {
                addDays = "Never"
            }
            "1111100" -> {
                addDays = "Weekdays"
            }
            "0000011" -> {
                addDays = "Weekends"
            }
            else -> {
                for(i in 0..6) {
                    if (repeatBooleanString[i] == '1') {
                        addDay = repeatDaysList[i]
                        addDays += "$addDay, "
                    }
                }
            }
        }

        Log.d(myTAG, "getRepeatingDaysString: $addDays")
        return addDays
    }

    @JvmStatic
    fun setColoredText(html: String, color: String): Spanned {
        // Color should be of form "#AF3FBF" (hexadecimal) only
        val result: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml("<font color='$color'>$html</font>", Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml("<font color='$color'>$html</font>")
        }

        return result
    }

    @JvmStatic
    fun dpFromPx(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }

    // Generates a prime number > 7 (Primes are used for the uniqueness of pendingIntent IDs)
    @JvmStatic
    fun getUniqueID(): Int? {
        var n = 11
        for(i in primesList.indices) {
            if(!usedId.contains(primesList[i])) {
                Log.d(myTAG, "getUniqueID: ${primesList[i]}")
                n = primesList[i]
                break
            }
        }
        usedId.add(n)

        return n
    }
}