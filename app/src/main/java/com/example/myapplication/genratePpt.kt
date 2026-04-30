package com.example.myapplication
import androidx.core.widget.addTextChangedListener
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.util.*
import android.widget.AutoCompleteTextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.apache.poi.xslf.usermodel.XMLSlideShow
import android.os.Environment
import org.apache.poi.xslf.usermodel.XSLFTable









class genratePpt : AppCompatActivity() {

    private val client = OkHttpClient()
    lateinit var db: AppDatabase

    private var editId: Int = -1
    private var isEditMode: Boolean = false

    private var isSettingData = false







    // ================= ON CREATE =================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.generate_ppt)

        db = AppDatabase.getDatabase(this)




        editId = intent.getIntExtra("id", -1)
        isEditMode = editId != -1





        // ===== SHIFT DROPDOWN =====

        val dropdown = findViewById<AutoCompleteTextView>(R.id.dropdownReport)

        val items = listOf(
            "GD",
            "GN",
            "GS",

            )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            items
        )

        dropdown.setAdapter(adapter)


        //===== team brakdown =======

        val etTeam = findViewById<AutoCompleteTextView>(R.id.dropdownteam)

        val itemsTeam = listOf(
            "AE",
            "AP",
            "QA",

            )

        val adapterTeam = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            itemsTeam
        )

        etTeam.setAdapter(adapterTeam)



        //===== issue handled by brakdown =======
        val etIssuehandledBy =  findViewById<AutoCompleteTextView>(R.id.etIssuehandledBy)


        val handledBy = listOf(
            "FIH",
            "AP",
            "AP + FIH",
            "VENDOR + FIH",

            )

        val adapterhandleby = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            handledBy
        )

        etIssuehandledBy.setAdapter(adapterhandleby)

//       =======================  Issue status =========================
        val etIssuesStatus =  findViewById<AutoCompleteTextView>(R.id.etIssueStatus)


        val statusby = listOf(
            "OPEN",
            "CLOSED",
            "ONGOING",

            )

        val adapterissueby = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            statusby
        )

        etIssuesStatus.setAdapter(adapterissueby)

//        ==================================  skill level ============================

        val etSkillLevel =  findViewById<AutoCompleteTextView>(R.id.etSkillLevel)


        val skillby = listOf(
            "L1",
            "L2",
            "L3",

            )

        val adapterskill = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            skillby
        )

        etSkillLevel.setAdapter(adapterskill)


//        ============================= machine vendor ============================
        val etVendor =  findViewById<AutoCompleteTextView>(R.id.etVendor)


        val machinevendorby = listOf(
            "JLK",
            "ACCURATE",
            "AVERNA",
            "SFO",
            "VVDN",
            "AMPHENOL",
            "NORDSON",


            )

        val adaptervendor = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            machinevendorby
        )

        etVendor.setAdapter(adaptervendor)

        // ===== FIND VIEWS =====

        val etDate = findViewById<EditText>(R.id.etDate)
        val etIssuestarttime = findViewById<EditText>(R.id.etIssuestarttime)
        val etIssueendtime = findViewById<EditText>(R.id.etIssueendtime)
        val etTotalDowntime = findViewById<EditText>(R.id.etTotalDowntime)






        val etName = findViewById<EditText>(R.id.etName)
        val etIdno = findViewById<EditText>(R.id.etIdno)
        val etStationId = findViewById<EditText>(R.id.etStationId)

        val etLine = findViewById<EditText>(R.id.etLine)

        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etMachine = findViewById<EditText>(R.id.etMachine)
        val etAnalysis = findViewById<EditText>(R.id.etAnalysis)
        val etRootCause = findViewById<EditText>(R.id.etRootCause)
        val etCorrective = findViewById<EditText>(R.id.etCorrective)
        val etPreventiveAction = findViewById<EditText>(R.id.etPreventiveAction)

        val etSparechanged = findViewById<EditText>(R.id.rgetspare)
        val rgSpare = findViewById<RadioGroup>(R.id.SpareChanged)
        val spareLayout = findViewById<LinearLayout>(R.id.sparelayout)




        etIdno.addTextChangedListener {

            if (isEditMode || isSettingData) return@addTextChangedListener

            val id = it.toString().trim()

            if (id.isNotEmpty()) {

                lifecycleScope.launch {

                    val user = db.workDao().getUserByEmpId(id)

                    if (user != null) {

                        etName.setText(user.name)
                        etTeam.setText(user.team)

                        if (dropdown.text.isNullOrEmpty()) {
                            dropdown.setText(user.shift, false)
                        }

                    } else {
                        etName.text.clear()
                        etTeam.text.clear()
                    }
                }
            }
        }



        // ===== DATE PICKER =====
        etDate.setOnClickListener {
            showDatePicker(etDate)
        }

        // ===== TIME PICKERS =====
        etIssuestarttime.setOnClickListener {
            showTimePicker(
                etIssuestarttime,
                etIssuestarttime,
                etIssueendtime,
                etTotalDowntime
            )
        }

        etIssueendtime.setOnClickListener {
            showTimePicker(
                etIssueendtime,
                etIssuestarttime,
                etIssueendtime,
                etTotalDowntime
            )
        }

        // ===== SHOW / HIDE DOWNTIME =====

        // ===== SHOW / HIDE SPARE CHANGED =====
        rgSpare.setOnCheckedChangeListener { _, checkedId ->
            spareLayout.visibility =
                if (checkedId == R.id.SpareradioYes) View.VISIBLE else View.GONE
        }

        // =============================== SAVE BUTTON =============================================











//================================== btn share ==============================



//================================== Fa report  ==============================




        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )

        val btnreport = findViewById<Button>(R.id.btnGenerateReport)

        btnreport.setOnClickListener {

            try {

                // ---------- Helper ----------
                fun setCell(
                    cell: org.apache.poi.xslf.usermodel.XSLFTableCell,
                    value: String,
                    size: Double = 20.0
                ) {

                    cell.text = value

                    // Vertical center
                    cell.verticalAlignment =
                        org.apache.poi.sl.usermodel.VerticalAlignment.MIDDLE

                    for (p in cell.textParagraphs) {

                        // Horizontal center
                        p.textAlign =
                            org.apache.poi.sl.usermodel.TextParagraph.TextAlign.CENTER

                        for (run in p.textRuns) {
                            run.fontSize = size
                        }
                    }
                }

                // ---------- Open template ----------
                val ppt = assets.open("FA_Template.pptx").use {
                    XMLSlideShow(it)
                }

                val slide = ppt.slides[0]

                for (shape in slide.shapes) {

                    if (shape is XSLFTable) {

                        val table = shape

                        if (table.numberOfRows > 0)
                            setCell(
                                table.rows[0].cells[1],
                                etMachine.text.toString(),
                                20.0
                            )

                        if (table.numberOfRows > 1)
                            setCell(
                                table.rows[1].cells[1],
                                etDate.text.toString(),
                                20.0
                            )

                        if (table.numberOfRows > 2)
                            setCell(
                                table.rows[2].cells[1],
                                etName.text.toString(),
                                18.0
                            )
                        if (table.numberOfRows > 2)
                            setCell(
                                table.rows[2].cells[4],

                                etStationId.text.toString(),
                                20.0
                            )

                        if (table.numberOfRows > 3)
                            setCell(
                                table.rows[3].cells[1],
                                etIssuestarttime.text.toString(),
                                20.0

                            )
                        if (table.numberOfRows > 3)
                            setCell(
                                table.rows[3].cells[2],

                                etIssueendtime.text.toString(),
                                20.0
                            )

                        if (table.numberOfRows > 4)
                            setCell(
                                table.rows[4].cells[1],
                                etTotalDowntime.text.toString(),
                                20.0
                            )
                        if (table.numberOfRows > 5)
                            setCell(
                                table.rows[5].cells[4],
                                etSkillLevel.text.toString(),
                                20.0
                            )

                        if (table.numberOfRows > 6)
                            setCell(
                                table.rows[6].cells[1],
                                etIssuesStatus.text.toString(),
                                20.0
                            )

                        if (table.numberOfRows > 6)
                            setCell(
                                table.rows[6].cells[4],
                                etStationId.text.toString(),
                                20.0
                            )

                        if (table.numberOfRows > 7)
                            setCell(
                                table.rows[7].cells[1],
                                etDescription.text.toString(),
                                20.0
                            )

                        if (table.numberOfRows > 8)
                            setCell(
                                table.rows[8].cells[1],
                                etAnalysis.text.toString(),
                                20.0
                            )

                        if (table.numberOfRows > 9)
                            setCell(
                                table.rows[9].cells[1],
                                etRootCause.text.toString(),
                                20.0
                            )

                        if (table.numberOfRows > 10)
                            setCell(
                                table.rows[10].cells[1],
                                etCorrective.text.toString(),
                                20.0
                            )

                        if (table.numberOfRows > 11)
                            setCell(
                                table.rows[11].cells[1],
                                etPreventiveAction.text.toString(),
                                20.0
                            )
                    }
                }

                // ---------- Unique filename ----------
                val timestamp = java.text.SimpleDateFormat(
                    "ddMMyyyy_HHmmss",
                    java.util.Locale.getDefault()
                ).format(java.util.Date())

                val fileName = "FA_Report_$timestamp.pptx"

                val file = java.io.File(
                    android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_DOWNLOADS
                    ),
                    fileName
                )

                java.io.FileOutputStream(file).use {
                    ppt.write(it)
                }

                ppt.close()

                Toast.makeText(
                    this,
                    "Saved:\n${file.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                e.printStackTrace()
            }
        }
//============================================ edit text ============================



        isSettingData = true   // 🔥 VERY IMPORTANT

        val intentData = intent

        etName.setText(intentData.getStringExtra("name") ?: "")
        etDate.setText(intentData.getStringExtra("date") ?: "")
        etIdno.setText(intentData.getStringExtra("empId") ?: "")
        etStationId.setText(intentData.getStringExtra("stationId") ?: "")
        dropdown.setText(intentData.getStringExtra("shift") ?: "", false)
        etTeam.setText(intentData.getStringExtra("team") ?: "")
        etLine.setText(intentData.getStringExtra("line") ?: "")
        etVendor.setText(intentData.getStringExtra("vendor") ?: "")
        etDescription.setText(intentData.getStringExtra("description") ?: "")
        etMachine.setText(intentData.getStringExtra("machine") ?: "")
        etAnalysis.setText(intentData.getStringExtra("analysis") ?: "")
        etRootCause.setText(intentData.getStringExtra("rootCause") ?: "")
        etCorrective.setText(intentData.getStringExtra("corrective") ?: "")
        etIssuestarttime.setText(intentData.getStringExtra("startTime") ?: "")
        etIssueendtime.setText(intentData.getStringExtra("endTime") ?: "")
        etTotalDowntime.setText(intentData.getStringExtra("totalTime") ?: "")
        etIssuehandledBy.setText(intentData.getStringExtra("handledBy") ?: "")
        etSparechanged.setText(intentData.getStringExtra("spareChanged") ?: "")

        etIssuesStatus.setText(intentData.getStringExtra("IssuesStatus") ?: "")
        etPreventiveAction.setText(intentData.getStringExtra("PreventiveAction") ?: "")
        etSkillLevel.setText(intentData.getStringExtra("SkillLevel") ?: "")

        isSettingData = false   // 🔥 RELEASE LOCK



    }



    //==================================================================================================
    private fun collectData(): WorkData {

        val downtimeGroup = findViewById<RadioGroup>(R.id.radioDowntime)
        val selectedId = downtimeGroup.checkedRadioButtonId

        val downtime =
            if (selectedId != -1)
                findViewById<RadioButton>(selectedId).text.toString()
            else "No"

        return WorkData(
            id = if (isEditMode) editId else 0,
            date = findViewById<EditText>(R.id.etDate).text.toString(),
            empId = findViewById<EditText>(R.id.etIdno).text.toString(),
            name = findViewById<EditText>(R.id.etName).text.toString(),
            stationId = findViewById<EditText>(R.id.etStationId).text.toString(),
            shift = findViewById<AutoCompleteTextView>(R.id.dropdownReport).text.toString(),
            team = findViewById<AutoCompleteTextView>(R.id.dropdownteam).text.toString(),
            line = findViewById<EditText>(R.id.etLine).text.toString(),
            downtime = downtime,
            vendor = findViewById<AutoCompleteTextView>(R.id.etVendor).text.toString(),
            description = findViewById<EditText>(R.id.etDescription).text.toString(),
            machine = findViewById<EditText>(R.id.etMachine).text.toString(),
            analysis = findViewById<EditText>(R.id.etAnalysis).text.toString(),
            rootCause = findViewById<EditText>(R.id.etRootCause).text.toString(),
            corrective = findViewById<EditText>(R.id.etCorrective).text.toString(),
            startTime = findViewById<EditText>(R.id.etIssuestarttime).text.toString(),
            endTime = findViewById<EditText>(R.id.etIssueendtime).text.toString(),
            totalTime = findViewById<EditText>(R.id.etTotalDowntime).text.toString(),
            handledBy = findViewById<AutoCompleteTextView>(R.id.etIssuehandledBy).text.toString(),
            spareChanged = findViewById<EditText>(R.id.rgetspare).text.toString(),


            issueStatus = findViewById<AutoCompleteTextView>(R.id.etIssueStatus).text.toString(),
            preventiveAction = findViewById<EditText>(R.id.etPreventiveAction).text.toString(),
            skillLevel = findViewById<AutoCompleteTextView>(R.id.etSkillLevel).text.toString(),






            )
    }




    private fun createReportMessage(): String {

        val downtimeValue = findViewById<RadioGroup>(R.id.radioDowntime)
        val selectedDowntimeId = downtimeValue.checkedRadioButtonId
        val downtime = if (selectedDowntimeId != -1)
            findViewById<RadioButton>(selectedDowntimeId).text.toString()
        else "No"

        val spareGroup = findViewById<RadioGroup>(R.id.SpareChanged)
        val selectedSpareId = spareGroup.checkedRadioButtonId
        val spareChanged = if (selectedSpareId != -1)
            findViewById<RadioButton>(selectedSpareId).text.toString()
        else "No"

        // ✅ SAFE VALUE FETCH (NO CRASH)
        fun getText(id: Int): String {
            return findViewById<EditText?>(id)?.text?.toString() ?: ""
        }

        fun getAutoText(id: Int): String {
            return findViewById<AutoCompleteTextView?>(id)?.text?.toString() ?: ""
        }

        val downtimeText = if (downtime == "Yes") {
            """               
Vendor               : ${getText(R.id.etVendor)}
Description       : ${getText(R.id.etDescription)}
Machine            : ${getText(R.id.etMachine)}

Analysis            : ${getText(R.id.etAnalysis)}
Root Cause      : ${getText(R.id.etRootCause)}
Corrective        : ${getText(R.id.etCorrective)}
PreventiveAction       : ${getText(R.id.etPreventiveAction)}
Start                 : ${getText(R.id.etIssuestarttime)}
End                   : ${getText(R.id.etIssueendtime)}
Total                 : ${getText(R.id.etTotalDowntime)}

Handled By       : ${getAutoText(R.id.etIssuehandledBy)}
Spare Changed    :$spareChanged
Sill Level       : ${getAutoText(R.id.etSkillLevel)}
Issue Status       : ${getAutoText(R.id.etIssueStatus)}

"""
        } else {
            ""
        }

        return """
📋 *Daily Work Report*

Date                  : ${getText(R.id.etDate)}
Name               : ${getText(R.id.etName)}
ID                      : ${getText(R.id.etIdno)}
Station             : ${getText(R.id.etStationId)}
Shift                 : ${getAutoText(R.id.dropdownReport)}

Team                : ${getAutoText(R.id.dropdownteam)}
Line                  : ${getText(R.id.etLine)}

Downtime        : $downtime

$downtimeText

""".trimIndent()
    }


    // ================= DATE PICKER =================
    private fun showDatePicker(editText: EditText) {

        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                editText.setText("$day/${month + 1}/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


    private fun showTimePicker(
        editText: EditText,
        startField: EditText,
        endField: EditText,
        resultField: EditText
    ) {

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)   // ✅ CHANGE HERE (12-hour format)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Time")
            .build()

        picker.show(supportFragmentManager, "TIME_PICKER")

        picker.addOnPositiveButtonClickListener {

            var hour = picker.hour
            val minute = picker.minute

            // ✅ Convert to AM/PM format
            val amPm = if (hour >= 12) "PM" else "AM"

            if (hour == 0) {
                hour = 12
            } else if (hour > 12) {
                hour -= 12
            }

            val time = String.format("%02d:%02d %s", hour, minute, amPm)
            editText.setText(time)

            val start = startField.text.toString()
            val end = endField.text.toString()

            if (start.isNotEmpty() && end.isNotEmpty()) {
                calculateDowntime(start, end, resultField)
            }
        }
    }

    // ================= DOWNTIME CALC =================
    private fun calculateDowntime(
        startTime: String,
        endTime: String,
        resultField: EditText
    ) {
        try {

            fun convertToMinutes(time: String): Int {
                val parts = time.split(" ")

                val timePart = parts[0]        // hh:mm
                val amPm = parts[1]           // AM / PM

                val hm = timePart.split(":")
                var hour = hm[0].toInt()
                val minute = hm[1].toInt()

                // ✅ Convert to 24-hour format
                if (amPm == "PM" && hour != 12) {
                    hour += 12
                }
                if (amPm == "AM" && hour == 12) {
                    hour = 0
                }

                return hour * 60 + minute
            }

            val startMinutes = convertToMinutes(startTime)
            val endMinutes = convertToMinutes(endTime)

            var diff = endMinutes - startMinutes

            // ✅ Handle next day case (e.g., 11 PM → 2 AM)
            if (diff < 0) {
                diff += 24 * 60
            }

            resultField.setText("$diff min")

        } catch (e: Exception) {
            resultField.setText("")
        }
    }

    // ================= SEND TO GOOGLE SHEET =================



}