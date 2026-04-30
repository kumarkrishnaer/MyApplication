package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    var list: MutableList<WorkData>,
    private val listener: OnItemSelectListener,
    private var selectedPosition: Int
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    interface OnItemSelectListener {
        fun onItemSelected(position: Int)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = list[position]
        holder.txtDate.text = "Date: ${data.date}"

        // ✅ CLICK
        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                listener.onItemSelected(pos)
            }
        }

        // ✅ HIGHLIGHT
        holder.itemView.setBackgroundColor(
            if (position == selectedPosition) Color.LTGRAY else Color.WHITE
        )
    }

    // ✅ 🔥 THIS WAS MISSING (CAUSE OF ERROR)
    fun updateSelection(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }
}