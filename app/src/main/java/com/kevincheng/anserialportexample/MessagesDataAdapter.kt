package com.kevincheng.anserialportexample

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class MessagesDataAdapter(private val dataSet: ArrayList<Message>, private val textColor: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.viewgroup_message, p0, false)
        val holder = MessageViewHolder(view)

        holder.time.setTextColor(textColor)
        holder.message.setTextColor(textColor)

        return holder
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        val message = getItem(p1) ?: return
        when(p0) {
            is MessageViewHolder -> {
                p0.time.text = message.date
                p0.message.text = String(message.byteArray)
            }
        }
    }

    private fun getItem(position: Int): Message? = when {
        position < 0 -> null
        dataSet.isEmpty() -> null
        position >= dataSet.size -> null
        else -> dataSet[position]
    }

    //================================================================================
    // ViewHolder
    //================================================================================

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val time: TextView = view.findViewById(R.id.textView_message_time)
        val message: TextView = view.findViewById(R.id.textView_message)
    }
}