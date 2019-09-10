package com.kevincheng.anserialportexample

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class SpinnerAdapter(context: Context, stringList: List<String>) : ArrayAdapter<String>(context, R.layout.spinner_item, stringList) {
    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val cell = when(convertView) {
            null -> LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
            else -> convertView
        }

        val textView = cell.findViewById(R.id.textView_spinner_item) as TextView
        textView.setTextColor(Color.WHITE)
        textView.text = getItem(position)

        return cell
    }
}