package com.example.myapplication
import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
        val btnStatus: Button = view.findViewById(R.id.btnStatus)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = list[position]

        holder.name.text = person.name

        // Set Status UI
        if (person.isPresent) {
            holder.btnStatus.text = "Present"
            holder.btnStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
        } else {
            holder.btnStatus.text = "Absent"
            holder.btnStatus.setBackgroundColor(Color.parseColor("#F44336"))
        }


        holder.btnStatus.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val updatedPerson = list[pos]
                updatedPerson.isPresent = !updatedPerson.isPresent

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
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to delete this person?")
                    .setPositiveButton("Yes") { _, _ ->
                        val personToDelete = list[pos]

                        onDelete(personToDelete)   // ✅ CALL ACTIVITY

                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }





    }
}