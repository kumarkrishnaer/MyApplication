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
import android.widget.Button
import android.widget.EditText
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import android.text.TextWatcher
import android.text.Editable



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

        // ========================== HEADER USER LIST =================

        val etNewHeader = findViewById<EditText>(R.id.etNewHeader)
        val btnAddHeader = findViewById<Button>(R.id.btnAddHeader)
        val dropdownHeader = findViewById<AutoCompleteTextView>(R.id.dropdownHeader)
        val chipGroupHeader = findViewById<ChipGroup>(R.id.chipGroupHeader)
        val prefs = getSharedPreferences("HeaderPrefs", MODE_PRIVATE)
        val savedHeaders = prefs.getStringSet("headers", emptySet()) ?: emptySet()
        val headerList = savedHeaders.toMutableList()
        val headerDropdownAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            headerList
        )

        dropdownHeader.setAdapter(headerDropdownAdapter)

        fun saveHeaders() {
            prefs.edit()
                .putStringSet("headers", headerList.toSet())
                .apply()
        }

        fun refreshHeaderChips() {
            chipGroupHeader.removeAllViews()

            headerList.forEach { header ->
                val chip = Chip(this)
                chip.text = header
                chip.isCloseIconVisible = true
                chip.isCheckable = false

                chip.setOnClickListener {
                    dropdownHeader.setText(header, false)
                }

                chip.setOnCloseIconClickListener {
                    headerList.remove(header)
                    saveHeaders()

                    headerDropdownAdapter.notifyDataSetChanged()

                    if (dropdownHeader.text.toString() == header) {
                        dropdownHeader.text.clear()
                    }

                    refreshHeaderChips()
                }

                chipGroupHeader.addView(chip)
            }
        }

        refreshHeaderChips()

        btnAddHeader.setOnClickListener {
            val header = etNewHeader.text.toString().trim()

            if (header.isEmpty()) {
                etNewHeader.error = "Enter header"
                return@setOnClickListener
            }

            if (headerList.contains(header)) {
                etNewHeader.error = "Already added"
                return@setOnClickListener
            }

            headerList.add(header)
            saveHeaders()

            headerDropdownAdapter.notifyDataSetChanged()
            dropdownHeader.setText(header, false)

            refreshHeaderChips()
            etNewHeader.text.clear()
        }


        // ====================btn share ===============================


        btnShare.setOnClickListener {

            val liveList = list.filter { it.isLive }

            if (liveList.isEmpty()) {
                Toast.makeText(this, "No active data to share", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val message = StringBuilder()
            val selectedHeader = dropdownHeader.text.toString().trim()

//            message.append("```")
//            message.append("\n============================\n")
//
//            if (selectedHeader.isNotEmpty()) {
//                message.append("$selectedHeader\n")
//            } else {
//                message.append("ATTENDANCE LIST\n")
//            }
//
//            message.append("============================\n")
//            message.append("```\n\n")

            if (selectedHeader.isNotEmpty()) {

                val lines = selectedHeader
                    .trim()
                    .lines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                if (lines.isNotEmpty()) {

                    message.append("*${lines[0]}*\n\n")
                }

                for (i in 1 until lines.size) {
                    message.append("${lines[i]}\n")
                }

                message.append("\n")

            } else {
                message.append("*ATTENDANCE LIST*\n\n")
            }
            message.append("\n")
            message.append("*Date:* ${tvDate.text}\n")
            message.append("*Shift:* ${tvShift.text}\n\n")

            val hideName = switchHideName.isChecked

            if (!hideName) {

                val presentList = liveList.filter { it.isPresent }
                val absentList = liveList.filter { !it.isPresent }

                var count = 1

                if (presentList.isNotEmpty()) {
                    message.append("*✅ Present List*\n")
                    presentList.forEach {
                        message.append("${count++}. ${it.name}\n")
                    }
                    message.append("\n")
                }

                if (absentList.isNotEmpty()) {

                    message.append("*❌ Absent List*\n")

                    absentList.forEach {

                        message.append("${count++}. ${it.name}\n")

                        if (it.absentReason.isNotEmpty()) {
                            message.append("   └ Deputy: ${it.absentReason}\n")
                        }

                        message.append("\n")
                    }
                }
            }

            val total = liveList.size
            val present = liveList.count { it.isPresent }
            val absent = total - present
            val percent = if (total > 0) (present * 100) / total else 0

            message.append("```")
            message.append(String.format("%-10s : %d\n", "Total", total))
            message.append(String.format("%-10s : %d\n", "Present", present))
            message.append(String.format("%-10s : %d\n", "Absent", absent))
            message.append(String.format("%-10s : %d%%\n", "Present %", percent))
            message.append("```")

            val remarkText = etRemark.text.toString().trim()
            if (remarkText.isNotEmpty()) {
                message.append("\n\n $remarkText")
            }

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, message.toString())
            intent.setPackage("com.whatsapp")

            try {
                startActivity(intent)
                saveReportLog("Attendance Updated")
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()

            }
        }

//        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
//
//        setSupportActionBar(toolbar)
//
//        toolbar.setNavigationOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }

        val remarkPrefs = getSharedPreferences("AttendanceRemarkPrefs", MODE_PRIVATE)

        etRemark.setText(
            remarkPrefs.getString("remark_text", "")
        )

        etRemark.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                remarkPrefs.edit()
                    .putString("remark_text", s.toString())
                    .apply()
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })


    }

    private fun saveReportLog(message: String) {

        val prefs = getSharedPreferences("ReportLogs", MODE_PRIVATE)

        val oldLogs = prefs.getStringSet("logs", mutableSetOf())
            ?.toMutableSet() ?: mutableSetOf()

        val time = java.text.SimpleDateFormat(
            "dd/MM hh:mm a",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        oldLogs.add("$message|$time")

        prefs.edit()
            .putStringSet("logs", oldLogs)
            .apply()
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

        val liveList = list.filter { it.isLive }

        val total = liveList.size
        val present = liveList.count { it.isPresent }
        val absent = total - present

        tvTotal.text = "Total: $total"
        tvPresent.text = "Present: $present"
        tvAbsent.text = "Absent: $absent"

        val percent = if (total > 0) {
            (present * 100) / total
        } else {
            0
        }

        tvPercentage.text = "Present: $percent%"
    }

}