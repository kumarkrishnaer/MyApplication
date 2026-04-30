package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import kotlinx.coroutines.launch
import android.widget.LinearLayout

class HistoryActivity : AppCompatActivity(),
    HistoryAdapter.OnItemSelectListener {

    private lateinit var db: AppDatabase
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: HistoryAdapter

    private var selectedPosition = -1

    // ✅ Buttons
//    private lateinit var btnView: Button
//    private lateinit var btnEdit: Button
//    private lateinit var btnDelete: Button
//    private lateinit var btnShare: Button

    private lateinit var btnView: LinearLayout
    private lateinit var btnEdit: LinearLayout
    private lateinit var btnDelete: LinearLayout
    private lateinit var btnShare: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recycler = findViewById(R.id.recyclerHistory)
        recycler.layoutManager = LinearLayoutManager(this)

        btnView = findViewById(R.id.btnView)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
        btnShare = findViewById(R.id.btnShare)

        db = AppDatabase.getDatabase(this)

        setupSwipe()
        setupButtons()
    }

    // ✅ ITEM SELECT
    override fun onItemSelected(position: Int) {
        selectedPosition = position
        adapter.updateSelection(position)
    }

    // ✅ BUTTON ACTIONS
    private fun setupButtons() {

        btnView.setOnClickListener {
            val data = getSelectedItem() ?: return@setOnClickListener

            val intent = Intent(this, ViewReportActivity::class.java).apply {

                putExtra("date", data.date)
                putExtra("name", data.name)
                putExtra("id", data.empId)
                putExtra("shift", data.shift)
                putExtra("stationId", data.stationId)
                putExtra("team", data.team)
                putExtra("line", data.line)
                putExtra("downtime", data.downtime)

                putExtra("vendor", data.vendor)
                putExtra("description", data.description)
                putExtra("machine", data.machine)
                putExtra("analysis", data.analysis)
                putExtra("rootCause", data.rootCause)
                putExtra("corrective", data.corrective)

                putExtra("startTime", data.startTime)
                putExtra("endTime", data.endTime)
                putExtra("totalTime", data.totalTime)

                putExtra("handledBy", data.handledBy)
                putExtra("spareChanged", data.spareChanged)

                putExtra("issueStatus", data.issueStatus)
                putExtra("preventiveAction", data.preventiveAction)
                putExtra("skillLevel", data.skillLevel)





            }

            startActivity(intent)
        }

        btnEdit.setOnClickListener {
            val data = getSelectedItem() ?: return@setOnClickListener

            val intent = Intent(this, UpdateActivity::class.java)

            intent.putExtra("id", data.id)              // 🔥 FIX
            intent.putExtra("empId", data.empId)

            intent.putExtra("date", data.date)
            intent.putExtra("name", data.name)
            intent.putExtra("shift", data.shift)
            intent.putExtra("stationId", data.stationId)
            intent.putExtra("team", data.team)
            intent.putExtra("line", data.line)
            intent.putExtra("downtime", data.downtime)

            intent.putExtra("vendor", data.vendor)
            intent.putExtra("description", data.description)
            intent.putExtra("machine", data.machine)
            intent.putExtra("analysis", data.analysis)
            intent.putExtra("rootCause", data.rootCause)
            intent.putExtra("corrective", data.corrective)

            intent.putExtra("startTime", data.startTime)
            intent.putExtra("endTime", data.endTime)
            intent.putExtra("totalTime", data.totalTime)
            intent.putExtra("handledBy", data.handledBy)
            intent.putExtra("spareChanged", data.spareChanged)

            intent.putExtra("issueStatus", data.issueStatus)
            intent.putExtra("preventiveAction", data.preventiveAction)
            intent.putExtra("skillLevel", data.skillLevel)

            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            val pos = selectedPosition

            if (pos == -1) {
                Toast.makeText(this, "Select item first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = adapter.list[pos]

            lifecycleScope.launch {
                db.workDao().delete(data)

                adapter.list.removeAt(pos)
                adapter.notifyItemRemoved(pos)

                selectedPosition = -1
                adapter.updateSelection(-1)

                Toast.makeText(this@HistoryActivity, "Deleted", Toast.LENGTH_SHORT).show()
            }
        }

        btnShare.setOnClickListener {
            val data = getSelectedItem() ?: return@setOnClickListener

            val message = buildString {

                appendLine("*Daily Work Report*")
                appendLine()

                appendLine("Date        : ${data.date}")
                appendLine("Name        : ${data.name}")
                appendLine("ID          : ${data.empId}")
                appendLine("Station     : ${data.stationId}")
                appendLine("Shift       : ${data.shift}")
                appendLine()

                appendLine("Team        : ${data.team}")
                appendLine("Line        : ${data.line}")
                appendLine()

                appendLine("Downtime    : ${data.downtime}")
                appendLine()

                if (data.downtime == "Yes") {
                    appendLine("Vendor      : ${data.vendor}")
                    appendLine("Description : ${data.description}")
                    appendLine("Machine     : ${data.machine}")
                    appendLine()

                    appendLine("Analysis    : ${data.analysis}")
                    appendLine("Root Cause  : ${data.rootCause}")
                    appendLine("Corrective  : ${data.corrective}")
                    appendLine("preventiveAction : ${data.preventiveAction}")
                    appendLine()

                    appendLine("Start       : ${data.startTime}")
                    appendLine("End         : ${data.endTime}")
                    appendLine("Total       : ${data.totalTime}")
                    appendLine()

                    appendLine("Handled By  : ${data.handledBy}")
                    appendLine("Spare Changed : ${data.spareChanged}")
                    appendLine("issueStatus : ${data.issueStatus}")
                    appendLine("skillLevel : ${data.skillLevel}")
                }
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }

            startActivity(Intent.createChooser(intent, "Share via"))
        }
    }

    // ✅ SAFE GET ITEM
    private fun getSelectedItem(): WorkData? {

        if (selectedPosition == -1) {
            Toast.makeText(this, "Select item first", Toast.LENGTH_SHORT).show()
            return null
        }

        if (selectedPosition >= adapter.list.size) {
            Toast.makeText(this, "Invalid selection", Toast.LENGTH_SHORT).show()
            return null
        }

        return adapter.list[selectedPosition]
    }

    // ✅ SWIPE DELETE
    private fun setupSwipe() {

        val swipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val pos = viewHolder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return

                val data = adapter.list[pos]

                lifecycleScope.launch {
                    db.workDao().delete(data)

                    adapter.list.removeAt(pos)
                    adapter.notifyItemRemoved(pos)

                    selectedPosition = -1
                    adapter.updateSelection(-1)

                    Toast.makeText(this@HistoryActivity, "Deleted", Toast.LENGTH_SHORT).show()
                }
            }
        }

        ItemTouchHelper(swipe).attachToRecyclerView(recycler)
    }

    // ✅ LOAD DATA
    private fun loadData() {

        lifecycleScope.launch {

            val list = db.workDao().getAll().toMutableList()

            adapter = HistoryAdapter(list, this@HistoryActivity, selectedPosition)
            recycler.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}