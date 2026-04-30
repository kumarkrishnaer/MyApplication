package com.example.myapplication

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView

class ReportAdapter(
    private val list: MutableList<ReportRow>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<ReportAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvRecordNo: EditText = v.findViewById(R.id.tvRecordNo)
        val etCol1: EditText = v.findViewById(R.id.etCol1)
        val etCol2: EditText = v.findViewById(R.id.etCol2)
        val etCol3: EditText = v.findViewById(R.id.etCol3)
        val etCol4: EditText = v.findViewById(R.id.etCol4)
        val etCol5: EditText = v.findViewById(R.id.etCol5)
        val etCol6: EditText = v.findViewById(R.id.etCol6)
        val etCol7: EditText = v.findViewById(R.id.etCol7)
        val etCol8: EditText = v.findViewById(R.id.etCol8)
        val btnRemove: Button = v.findViewById(R.id.btnRemove)

        var watcher1: TextWatcher? = null
        var watcher2: TextWatcher? = null
        var watcher3: TextWatcher? = null
        var watcher4: TextWatcher? = null
        var watcher5: TextWatcher? = null
        var watcher6: TextWatcher? = null
        var watcher7: TextWatcher? = null
        var watcher8: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val row = list[position]

        holder.tvRecordNo.setText((position + 1).toString())

        removeOldWatchers(holder)

        holder.etCol1.setText(row.col1)
        holder.etCol2.setText(row.col2)
        holder.etCol3.setText(row.col3)
        holder.etCol4.setText(row.col4)
        holder.etCol5.setText(row.col5)
        holder.etCol6.setText(row.col6)
        holder.etCol7.setText(row.col7)
        holder.etCol8.setText(row.col8)

        holder.watcher1 = SimpleWatcher {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) list[pos].col1 = it
        }
        holder.watcher2 = SimpleWatcher {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) list[pos].col2 = it
        }
        holder.watcher3 = SimpleWatcher {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) list[pos].col3 = it
        }
        holder.watcher4 = SimpleWatcher {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) list[pos].col4 = it
        }
        holder.watcher5 = SimpleWatcher {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) list[pos].col5 = it
        }
        holder.watcher6 = SimpleWatcher {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) list[pos].col6 = it
        }
        holder.watcher7 = SimpleWatcher {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) list[pos].col7 = it
        }
        holder.watcher8 = SimpleWatcher {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) list[pos].col8 = it
        }

        holder.etCol1.addTextChangedListener(holder.watcher1)
        holder.etCol2.addTextChangedListener(holder.watcher2)
        holder.etCol3.addTextChangedListener(holder.watcher3)
        holder.etCol4.addTextChangedListener(holder.watcher4)
        holder.etCol5.addTextChangedListener(holder.watcher5)
        holder.etCol6.addTextChangedListener(holder.watcher6)
        holder.etCol7.addTextChangedListener(holder.watcher7)
        holder.etCol8.addTextChangedListener(holder.watcher8)

        holder.btnRemove.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onRemove(pos)
            }
        }
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        removeOldWatchers(holder)
    }

    override fun getItemCount(): Int = list.size

    private fun removeOldWatchers(holder: VH) {
        holder.watcher1?.let { holder.etCol1.removeTextChangedListener(it) }
        holder.watcher2?.let { holder.etCol2.removeTextChangedListener(it) }
        holder.watcher3?.let { holder.etCol3.removeTextChangedListener(it) }
        holder.watcher4?.let { holder.etCol4.removeTextChangedListener(it) }
        holder.watcher5?.let { holder.etCol5.removeTextChangedListener(it) }
        holder.watcher6?.let { holder.etCol6.removeTextChangedListener(it) }
        holder.watcher7?.let { holder.etCol7.removeTextChangedListener(it) }
        holder.watcher8?.let { holder.etCol8.removeTextChangedListener(it) }
    }
}