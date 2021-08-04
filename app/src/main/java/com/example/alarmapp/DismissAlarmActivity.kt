package com.example.alarmapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_dismiss_alarm.*
import kotlin.math.min

class DismissAlarmActivity: AppCompatActivity() {
    val CANCEL_ALARM = "action to cancel alarm"
    val NAVIGATE_TO_MAIN = "navigate back to main"
    val MESSAGE_KEY = "extra message"
    val NAME_KEY = "alarm name"
    val TIME_KEY = "time"
    val ID_KEY = "alarm id"
    val DIFFICULTY_KEY = "problem difficulty"

    private val TAG = "DismissAlarmActivity"
    private val target = 5
    private var correctAnswer = 0
    private var problemNumber = 0
    // Target is the number of questions to be solved

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dismiss_alarm)
        var message = intent.extras?.getString(MESSAGE_KEY)
        val name = intent.extras?.getString(NAME_KEY)
        val difficulty = intent.extras?.getInt(DIFFICULTY_KEY, 0)
        val id = intent.extras?.getInt(ID_KEY)

        message = message?.substring(0, min(30, message.length))
        dismiss_message.text = message
        dismiss_name.text = name

        if(difficulty == 0) {
            cancelAlarmRoutine()
        } else {
            correctAnswer = displayProblem(difficulty!!, problemNumber)
            problemNumber += 1
        }

        next_button.setOnClickListener {
            if(difficulty == 0 || problemNumber == target + 1) {
                val intent = Intent(this, AlarmReceiver::class.java)
                intent.action = CANCEL_ALARM
                sendBroadcast(intent)

                val navigationIntent = Intent(this, MainActivity::class.java)
                navigationIntent.action = NAVIGATE_TO_MAIN
                navigationIntent.putExtra(ID_KEY, id)
                Log.d(TAG, "Alarm id: $id")

                AlarmState.updateState()
                startActivity(navigationIntent)
            } else {
                val currentAnswer = answer_et.text
                if(currentAnswer != null && currentAnswer.toString() != "") {
                    if(currentAnswer.toString().toInt() == correctAnswer) {
                        correctAnswer = displayProblem(difficulty, problemNumber)
                        problemNumber += 1
                        Toast.makeText(
                                this,
                                "Correct",
                                Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                                this,
                                "Incorrect! Try Again.",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                            this,
                            "Enter integers only!",
                            Toast.LENGTH_SHORT
                    ).show()
                }

                // Using target + 1 because it will show 1 lesser problem otherwise
                if(problemNumber == target + 1) {
                    cancelAlarmRoutine()
                }
            }
        }
    }

    private fun cancelAlarmRoutine() {
        problems_title_tv.text = "Click CANCEL to dismiss"
        problem_statement_tv.text = "No more problems"
        answer_et.visibility = View.GONE
        next_button.text = "CANCEL"
    }

    @SuppressLint("SetTextI18n")
    private fun displayProblem(difficulty: Int, number: Int): Int {
        Log.d(TAG, "current problem no: $problemNumber")
        answer_et.text = null
        answer_et.requestFocus()
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        return if(difficulty == 1) {
            val rand1 = (10..50).random()
            val rand2 = (10..50).random()
            problem_statement_tv.text = "${number + 1})  $rand1 x $rand2 = "
            rand1 * rand2
        } else {
            val rand1 = (50..100).random()
            val rand2 = (50..100).random()
            problem_statement_tv.text = "${number + 1})  $rand1 x $rand2 = "
            rand1 * rand2
        }
    }
}