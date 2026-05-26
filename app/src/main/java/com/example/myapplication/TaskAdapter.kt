package com.example.myapplication

import android.app.AlertDialog
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAdapter(
    private val list: MutableList<Person>,
    private val db: AppDatabase,
    private val onStatusChange: () -> Unit,
    private val onDelete: (Person) -> Unit
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val btnLive: Button = view.findViewById(R.id.btnLive)
        val btnStatus: Button = view.findViewById(R.id.btnStatus)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
        val etAbsentReason: EditText = view.findViewById(R.id.etAbsentReason)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = list[position]

        holder.name.text = person.name

        if (person.isLive) {
            holder.btnLive.text = "ACTIVE"
            holder.btnLive.setBackgroundColor(Color.parseColor("#2196F3"))

            holder.btnStatus.isEnabled = true
            holder.btnStatus.alpha = 1f

            if (person.isPresent) {
                holder.btnStatus.text = "PRESENT"
                holder.btnStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
                holder.etAbsentReason.visibility = View.GONE
            } else {
                holder.btnStatus.text = "ABSENT"
                holder.btnStatus.setBackgroundColor(Color.parseColor("#F44336"))
                holder.etAbsentReason.visibility = View.VISIBLE
            }

        } else {
            holder.btnLive.text = "INACTIVE"
            holder.btnLive.setBackgroundColor(Color.parseColor("#9E9E9E"))

            holder.btnStatus.text = "LOCKED"
            holder.btnStatus.setBackgroundColor(Color.parseColor("#BDBDBD"))
            holder.btnStatus.isEnabled = false
            holder.btnStatus.alpha = 0.5f

            holder.etAbsentReason.visibility = View.GONE
        }

        holder.etAbsentReason.setText(person.absentReason)

        holder.etAbsentReason.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                person.absentReason = s.toString()

                CoroutineScope(Dispatchers.IO).launch {
                    db.personDao().update(person)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        holder.btnLive.setOnClickListener {
            val pos = holder.bindingAdapterPosition

            if (pos != RecyclerView.NO_POSITION) {
                val updatedPerson = list[pos]

                updatedPerson.isLive = !updatedPerson.isLive

                if (!updatedPerson.isLive) {
                    updatedPerson.absentReason = ""
                }

                CoroutineScope(Dispatchers.IO).launch {
                    db.personDao().update(updatedPerson)
                }

                notifyItemChanged(pos)
                onStatusChange()
            }
        }

        holder.btnStatus.setOnClickListener {
            val pos = holder.bindingAdapterPosition

            if (pos != RecyclerView.NO_POSITION) {
                val updatedPerson = list[pos]

                if (!updatedPerson.isLive) return@setOnClickListener

                updatedPerson.isPresent = !updatedPerson.isPresent

                if (updatedPerson.isPresent) {
                    updatedPerson.absentReason = ""
                }

                CoroutineScope(Dispatchers.IO).launch {
                    db.personDao().update(updatedPerson)
                }

                notifyItemChanged(pos)
                onStatusChange()
            }
        }

        holder.btnDelete.setOnClickListener {
            val pos = holder.bindingAdapterPosition

            if (pos != RecyclerView.NO_POSITION) {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Delete Person")
                    .setMessage("Do you want to delete ${list[pos].name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        onDelete(list[pos])
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}