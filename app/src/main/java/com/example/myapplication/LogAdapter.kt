package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LogAdapter(
    private val logs: List<String>
) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvMessage: TextView =
            view.findViewById(R.id.tvMessage)

        val tvTime: TextView =
            view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LogViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log, parent, false)

        return LogViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: LogViewHolder,
        position: Int
    ) {

        val parts = logs[position].split("|")

        holder.tvMessage.text =
            parts.getOrNull(0) ?: ""

        holder.tvTime.text =
            parts.getOrNull(1) ?: ""
    }

    override fun getItemCount(): Int {
        return logs.size
    }
}