package com.example.myapplication

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ShiftHistoryAdapter(
    private val list: List<ShiftHistoryModel>
) : RecyclerView.Adapter<ShiftHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHistoryDate: TextView = itemView.findViewById(R.id.tvHistoryDate)
        val tvHistoryShift: TextView = itemView.findViewById(R.id.tvHistoryShift)
        val tvHistoryCount: TextView = itemView.findViewById(R.id.tvHistoryCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shift_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = list[position]

        holder.tvHistoryDate.text = item.date
        holder.tvHistoryShift.text = "Shift: ${item.shift}"
        holder.tvHistoryCount.text = "${item.total} Records"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ViewShiftReportActivity::class.java)
            intent.putExtra("date", item.date)
            intent.putExtra("shift", item.shift)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size
}