package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TaskActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private val list = mutableListOf<Person>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.attendance)

        val etName = findViewById<EditText>(R.id.etName)
        val etRemark = findViewById<EditText>(R.id.etRemark)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val tvPresent = findViewById<TextView>(R.id.tvPresent)
        val tvAbsent = findViewById<TextView>(R.id.tvAbsent)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val btnShare = findViewById<MaterialCardView>(R.id.btnSharetask)
        val tvPercentage = findViewById<TextView>(R.id.tvPercentage)
        val tvShift = findViewById<TextView>(R.id.tvShift)


        val switchHideName = findViewById<Switch>(R.id.switchHideName)

        tvShift.text = "${getShift()}"
        tvDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

        // ✅ Init DB
        db = AppDatabase.getDatabase(this)

        // ✅ Adapter
        adapter = TaskAdapter(
            list,
            db,
            onStatusChange = {
                updateStats(tvTotal, tvPresent, tvAbsent, tvPercentage)
            },
            onDelete = { person ->

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        db.personDao().delete(person)
                    }

                    list.remove(person)
                    adapter.notifyDataSetChanged()
                    updateStats(tvTotal, tvPresent, tvAbsent, tvPercentage)
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ✅ Load Data from DB (Background)
        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                db.personDao().getAll()
            }
            list.clear()
            list.addAll(data)
            adapter.notifyDataSetChanged()
            updateStats(tvTotal, tvPresent, tvAbsent, tvPercentage)
        }

        // ✅ Add Person
        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()

            if (name.isNotEmpty()) {

                val person = Person(
                    name = name,
                    shift = getShift()
                )

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        db.personDao().insert(person)
                    }

                    list.clear()
                    list.addAll(db.personDao().getAll())
                    adapter.notifyDataSetChanged()
                    updateStats(tvTotal, tvPresent, tvAbsent, tvPercentage)
                }

                etName.text.clear()

            } else {
                Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ Share to WhatsApp
//        btnShare.setOnClickListener {
//
//            if (list.isEmpty()) {
//                Toast.makeText(this, "No data to share", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val message = StringBuilder()
//
//            message.append("📋 Attendance List\n\n")
//            message.append(" Date: ${tvDate.text}\n\n")
//            message.append(" Shift: ${tvShift.text}\n\n")
//
//
//            list.forEachIndexed { index, person ->
//                val status = if (person.isPresent) "Present" else "Absent"
//                message.append("${index + 1}. ${person.name} - $status\n")
//            }
//
//            val total = list.size
//            val present = list.count { it.isPresent }
//            val absent = total - present
//            val percent = if (total > 0) (present * 100) / total else 0
//
//            message.append("\nTotal: $total")
//            message.append("\nPresent: $present")
//            message.append("\nAbsent: $absent")
//            message.append("\nPresent: $percent%")
//
//            val remarkText = etRemark.text.toString().trim()
//
//            if (remarkText.isNotEmpty()) {
//                message.append("\nRemarks:- $remarkText")
//            }
//
//            val intent = Intent(Intent.ACTION_SEND)
//            intent.type = "text/plain"
//            intent.putExtra(Intent.EXTRA_TEXT, message.toString())
//            intent.setPackage("com.whatsapp")
//
//            try {
//                startActivity(intent)
//            } catch (e: Exception) {
//                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
//            }
//        }

        btnShare.setOnClickListener {

            if (list.isEmpty()) {
                Toast.makeText(this, "No data to share", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val message = StringBuilder()

            message.append("*📋 Attendance List*\n\n")
            message.append("*Date:* ${tvDate.text}\n")
            message.append("*Shift:* ${tvShift.text}\n\n")

            val hideName = switchHideName.isChecked

            if (!hideName) {
                message.append("```")

                list.forEachIndexed { index, person ->
                    message.append("${index + 1}. ${person.name}\n")
                }

                message.append("```\n")
            }

            val total = list.size
            val present = list.count { it.isPresent }
            val absent = total - present
            val percent = if (total > 0) (present * 100) / total else 0

            message.append("\n```")
            message.append(String.format("%-10s : %d\n", "Total", total))
            message.append(String.format("%-10s : %d\n", "Present", present))
            message.append(String.format("%-10s : %d\n", "Absent", absent))
            message.append(String.format("%-10s : %d%%\n", "Present %", percent))
            message.append("```")

            val remarkText = etRemark.text.toString().trim()
            if (remarkText.isNotEmpty()) {
                message.append("\n\n*Remarks:* $remarkText")
            }

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, message.toString())
            intent.setPackage("com.whatsapp")

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun getShift(): String {

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return if (hour in 6..17) {
            "GD"
        } else {
            "GN"
        }
    }

    private fun updateStats(
        tvTotal: TextView,
        tvPresent: TextView,
        tvAbsent: TextView,
        tvPercentage: TextView
    ) {
        val total = list.size
        val present = list.count { it.isPresent }
        val absent = total - present

        tvTotal.text = "Total: $total"
        tvPresent.text = "Present: $present"
        tvAbsent.text = "Absent: $absent"

        val percent = if (total > 0) (present * 100) / total else 0
        tvPercentage.text = "Present: $percent%"
    }
}