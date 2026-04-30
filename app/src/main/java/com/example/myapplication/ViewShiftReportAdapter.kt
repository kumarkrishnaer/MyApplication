package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViewShiftReportAdapter(
    private val list: List<ShiftEndReportData>
) : RecyclerView.Adapter<ViewShiftReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvViewTitle: TextView = itemView.findViewById(R.id.tvViewTitle)
        val tvViewDetails: TextView = itemView.findViewById(R.id.tvViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_shift_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = list[position]

        holder.tvViewTitle.text = "Record ${position + 1}"

        holder.tvViewDetails.text = buildString {
            append("Name: ${item.name}\n")
            append("ID No: ${item.empId}\n")
            append("Station: ${item.station}\n")
            append("Station No: ${item.stationNo}\n")
            append("Issue: ${item.issue}\n")
            append("Corrective Action: ${item.correctiveAction}\n")
            append("Status: ${item.status}\n")
            append("Remarks: ${item.remarks}")
        }
    }

    override fun getItemCount(): Int = list.size
}