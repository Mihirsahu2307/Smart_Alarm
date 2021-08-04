package com.example.alarmapp

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.log


class AlarmAdapter(val context: Context, val alarmList: ArrayList<Alarm>) :
        RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    private val myTAG: String = "AlarmAdapter"
    private var repeatDays: String? = "Repeats: "
    private var mSwitchListener: OnSwitchToggleListener? = null
    private var mPressListener: OnLongPressListener? = null

    interface OnSwitchToggleListener {
        fun onToggle(position: Int, state: Boolean)
    }

    interface OnLongPressListener {
        fun onClick(position: Int)
    }

    fun setOnSwitchToggleListener(listener: OnSwitchToggleListener) {
        mSwitchListener = listener
    }

    fun setOnLongPressListener(listener: OnLongPressListener) {
        mPressListener = listener
    }

    class AlarmViewHolder(itemView: View,
                      switchListener: OnSwitchToggleListener,
                      pressListener: OnLongPressListener) : RecyclerView.ViewHolder(itemView) {
        private val myTag = "AlarmAdapter"

        val alarmName: TextView = itemView.findViewById(R.id.alarm_name)
        val formattedTime: TextView = itemView.findViewById(R.id.display_time)
        val repeat: TextView = itemView.findViewById(R.id.repeat_textView)
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        val state: Switch = itemView.findViewById(R.id.alarm_switch)

        init {
            state.setOnCheckedChangeListener { buttonView, isChecked ->
                Log.d(myTag, "Switch toggled to ${state.isChecked}")
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION)
                    switchListener.onToggle(position, state.isChecked)
            }

            itemView.setOnLongClickListener { view ->
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    pressListener.onClick(position)
                }

                return@setOnLongClickListener true
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
                R.layout.alarm_list_item, parent, false)
        return AlarmViewHolder(v, mSwitchListener!!, mPressListener!!)
    }

    override fun getItemCount() = alarmList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        var alarmName = alarmList[position].alarmName
        if(alarmList[position].alarmName.length > 11) {
            alarmName = alarmList[position].alarmName.substring(0, 10) + "..."
        }

        holder.alarmName.text = "-${alarmName}"
        holder.formattedTime.text = alarmList[position].alarmTime

        val addDays = Utils.getRepeatingDaysString(alarmList[position].repeat)
        repeatDays = "Repeats: $addDays"
        if(repeatDays!!.endsWith(", ")) {
            repeatDays = repeatDays!!.substring(0, repeatDays!!.length - 2)
        }
        holder.repeat.text = repeatDays

        holder.state.isChecked = alarmList[position].state
    }
}