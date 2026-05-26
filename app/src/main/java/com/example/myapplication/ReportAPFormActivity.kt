package com.example.myapplication

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

import android.graphics.Typeface
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Calendar
import android.content.ContentValues
import android.provider.MediaStore


class ReportAPFormActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    private var formCount = 1

    private lateinit var etDate: TextInputEditText
    //    private lateinit var currentShift: TextInputEditText
    private var currentShift: String = ""
    private lateinit var dropdownName: AutoCompleteTextView
    private lateinit var etIdNo: AutoCompleteTextView
    private lateinit var dropdownFloor: AutoCompleteTextView
    private lateinit var dropdownStation: AutoCompleteTextView
    private lateinit var etStationId: AutoCompleteTextView
    private lateinit var dropdownIssue: AutoCompleteTextView
    private lateinit var etCorrective: AutoCompleteTextView
    private lateinit var dropdownStatus: AutoCompleteTextView
    private lateinit var etRemark: AutoCompleteTextView

    private lateinit var btnSave: Button
    private lateinit var btnManageDropdown: Button
    private lateinit var btnAddAnotherForm: Button
    private lateinit var formContainer: LinearLayout

    data class ReportRow(
        val name: String,
        val idNo: String,
        val floor: String,
        val station: String,
        val stationId: String,
        val issue: String,
        val corrective: String,
        val status: String,
        val remark: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.report_ap_activity_form)

        db = AppDatabase.getDatabase(this)

        etDate = findViewById(R.id.etDate)
        dropdownName = findViewById(R.id.dropdownName)

        etIdNo = findViewById(R.id.etIdNo)
        dropdownFloor = findViewById(R.id.dropdownFloor)
        dropdownStation = findViewById(R.id.dropdownStation)
        etStationId = findViewById(R.id.etStationId)
        dropdownIssue = findViewById(R.id.dropdownIssue)
        etCorrective = findViewById(R.id.etCorrective)
        dropdownStatus = findViewById(R.id.dropdownStatus)
        etRemark = findViewById(R.id.etRemark)

        btnSave = findViewById(R.id.btnSave)
        btnManageDropdown = findViewById(R.id.btnManageDropdown)
        btnAddAnotherForm = findViewById(R.id.btnAddAnotherForm)
        formContainer = findViewById(R.id.formContainer)

        etDate.setText(getCurrentDate())
        currentShift = getAutoShift()



        loadAllDropdowns()


        loadEmployeeDropdowns(dropdownName, etIdNo)
        setupNameIdPairing(dropdownName, etIdNo)
        setupIssueCorrectivePairing(dropdownIssue, etCorrective)

        btnManageDropdown.setOnClickListener {
            startActivity(Intent(this, ReportAPAddMasterActivity::class.java))
        }

        btnAddAnotherForm.setOnClickListener {
            addNewForm()

        }

//        btnSave.setOnClickListener {
//
//
//
//            AlertDialog.Builder(this)
//                .setTitle("Submit Report")
//                .setMessage("Do you want to save and upload this report to Google Sheet?")
//
//                .setPositiveButton("YES") { _, _ ->
//
//                    lifecycleScope.launch {
//                        shareReportAsImage()
//                        submitAllToGoogleSheet()
//                        saveReportLog("Shift Report Generated & Shared")
//                    }
//                }
//
//                .setNegativeButton("NO") { _, _ ->
//
//                    shareReportAsImage()
//                    saveReportLog("Shift Report Generated & Shared")
//
//                }
//
//                .show()
//
//        }
        btnSave.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Submit Report")
                .setMessage("Do you want to upload this report to Google Sheet?")

                .setPositiveButton("YES") { _, _ ->

                    lifecycleScope.launch {

                        showDownloadDialog()

                        submitAllToGoogleSheet()

                        saveReportLog("Shift Report Generated & Shared")
                    }
                }

                .setNegativeButton("NO") { _, _ ->

                    showDownloadDialog()

                    saveReportLog("Shift Report Generated & Shared")
                }

                .show()
        }
    }

    private fun showDownloadDialog() {

        AlertDialog.Builder(this)
            .setTitle("Download Image")
            .setMessage("Do you want to download report image?")

            .setPositiveButton("YES") { _, _ ->

                downloadReportAsImage()


            }

            .setNegativeButton("NO") { _, _ ->

                shareReportAsImage()
            }

            .show()
    }



    private fun downloadReportAsImage() {
        try {
            val bitmap = addPremiumWatermark(createReportBitmapFromData())

            val filename = "Shift_Report_${System.currentTimeMillis()}.png"

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Shift Reports")
            }

            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

            uri?.let {
                contentResolver.openOutputStream(it).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
                }

                Toast.makeText(this, "Image downloaded successfully", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }



    private suspend fun submitAllToGoogleSheet() {

        val rows = collectAllRows().filter {
            it.name.isNotBlank() ||
                    it.idNo.isNotBlank() ||
                    it.issue.isNotBlank()
        }

        if (rows.isEmpty()) {
            Toast.makeText(this, "No data to upload", Toast.LENGTH_SHORT).show()
            return
        }

        withContext(Dispatchers.IO) {

            val rowsJson = rows.joinToString(",") { row ->
                """
            {
              "name": "${row.name}",
              "id": "${row.idNo}",
              "stationName": "${row.station}",

              "stationId": "${row.floor} / ${row.station}",
              "issue": "${row.issue}",
              "correctiveAction": "${row.corrective}",
              "status": "${row.status}",
              "remark": "${row.remark}"
            }
            """.trimIndent()
            }

            val json = """
        {
          "reportType": "shift_end_bulk",
          "date": "${etDate.text}",
          "shift": "${currentShift}",
          "rows": [
            $rowsJson
          ]
        }
        """.trimIndent()

            val body = json.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://script.google.com/macros/s/AKfycbxlFtpP1jKmyN-4mta3vPNyQzqKNo9j-fOgQ8w7LnOywaqFmG1WpGTHN1hmfqFWe7BhtA/exec")
                .post(body)
                .build()

            OkHttpClient().newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Upload failed: ${response.code}")
                }
            }
        }

        Toast.makeText(
            this@ReportAPFormActivity,
            "All rows uploaded ✅",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) {
            loadAllDropdowns()
            loadEmployeeDropdowns(dropdownName, etIdNo)
            setupIssueCorrectivePairing(dropdownIssue, etCorrective)
        }
    }

    private fun addNewForm() {

        formCount++

        val view = layoutInflater.inflate(
            R.layout.report_ap_single_form_item,
            formContainer,
            false
        )

        val tvFormCount = view.findViewById<TextView>(R.id.tvFormCount)
        tvFormCount.text = "Form $formCount"

        val newDate = view.findViewById<EditText>(R.id.etDate)
        val newName = view.findViewById<AutoCompleteTextView>(R.id.dropdownName)
        val newIdNo = view.findViewById<AutoCompleteTextView>(R.id.etIdNo)
        val newFloor = view.findViewById<AutoCompleteTextView>(R.id.dropdownFloor)

        val newStation = view.findViewById<AutoCompleteTextView>(R.id.dropdownStation)
        val newStationId = view.findViewById<AutoCompleteTextView>(R.id.etStationId)
        val newIssue = view.findViewById<AutoCompleteTextView>(R.id.dropdownIssue)
        val newCorrective = view.findViewById<AutoCompleteTextView>(R.id.etCorrective)
        val newStatus = view.findViewById<AutoCompleteTextView>(R.id.dropdownStatus)
        val newRemark = view.findViewById<AutoCompleteTextView>(R.id.etRemark)

        newDate.setText(getCurrentDate())

        loadEmployeeDropdowns(newName, newIdNo)
        setupNameIdPairing(newName, newIdNo)

        loadDropdown("Floor", newFloor)
        loadDropdown("Station", newStation)
        loadDropdown("Station ID", newStationId)
        loadDropdown("Issue", newIssue)
        setupIssueCorrectivePairing(newIssue, newCorrective)
        loadDropdown("Status", newStatus)
        loadDropdown("Remark", newRemark)

        formContainer.addView(view)
    }



    private fun loadAllDropdowns() {
//        loadDropdown("Name", dropdownName)
//        loadDropdown("Id", etIdNo)
        loadDropdown("Floor", dropdownFloor)
        loadDropdown("Station", dropdownStation)
        loadDropdown("Station ID", etStationId)
        loadDropdown("Issue", dropdownIssue)
        loadDropdown("Status", dropdownStatus)
        loadDropdown("Remark", etRemark)
    }

    private fun loadDropdown(category: String, view: AutoCompleteTextView) {
        lifecycleScope.launch {
            val list = db.reportAPDropdownDao().getValuesByCategory(category)

            val adapter = ArrayAdapter(
                this@ReportAPFormActivity,
                android.R.layout.simple_dropdown_item_1line,
                list
            )

            view.setAdapter(adapter)
        }
    }

    private fun loadEmployeeDropdowns(
        nameView: AutoCompleteTextView,
        idView: AutoCompleteTextView
    ) {
        lifecycleScope.launch {
            val names = db.reportAPEmployeeMasterDao().getAllNames()
            val ids = db.reportAPEmployeeMasterDao().getAllIds()

            nameView.setAdapter(
                ArrayAdapter(
                    this@ReportAPFormActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    names
                )
            )

            idView.setAdapter(
                ArrayAdapter(
                    this@ReportAPFormActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    ids
                )
            )
        }
    }
    private fun setupNameIdPairing(
        nameView: AutoCompleteTextView,
        idView: AutoCompleteTextView
    ) {
        nameView.setOnItemClickListener { _, _, _, _ ->
            val selectedName = nameView.text.toString()

            lifecycleScope.launch {
                val id = db.reportAPEmployeeMasterDao().getIdByName(selectedName)

                if (!id.isNullOrEmpty()) {
                    idView.setText(id, false)
                }
            }
        }

        idView.setOnItemClickListener { _, _, _, _ ->
            val selectedId = idView.text.toString()

            lifecycleScope.launch {
                val name = db.reportAPEmployeeMasterDao().getNameById(selectedId)

                if (!name.isNullOrEmpty()) {
                    nameView.setText(name, false)
                }
            }
        }
    }


    private fun setupIssueCorrectivePairing(
        issueView: AutoCompleteTextView,
        correctiveView: AutoCompleteTextView
    ) {
        issueView.threshold = 0
        correctiveView.threshold = 0

        issueView.setOnClickListener { issueView.showDropDown() }
        issueView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) issueView.showDropDown()
        }

        issueView.setOnItemClickListener { _, _, _, _ ->
            val selectedIssue = issueView.text.toString().trim()

            lifecycleScope.launch {
                val corrective = db.reportAPDropdownDao()
                    .getValuesByCategory(issueCorrectiveCategory(selectedIssue))
                    .firstOrNull()

                if (!corrective.isNullOrBlank()) {
                    correctiveView.setText(corrective, false)
                }
            }
        }
    }

    private fun issueCorrectiveCategory(issue: String): String = "Corrective::$issue"

    private fun collectAllRows(): List<ReportRow> {
        val rows = mutableListOf<ReportRow>()

        rows.add(
            ReportRow(
                name = dropdownName.text.toString(),
                idNo = etIdNo.text.toString(),
                floor = dropdownFloor.text.toString(),
                station = dropdownStation.text.toString(),
                stationId = etStationId.text.toString(),
                issue = dropdownIssue.text.toString(),
                corrective = etCorrective.text.toString(),
                status = dropdownStatus.text.toString(),
                remark = etRemark.text.toString()
            )
        )

        for (i in 0 until formContainer.childCount) {
            val view = formContainer.getChildAt(i)

            rows.add(
                ReportRow(
                    name = view.findViewById<AutoCompleteTextView>(R.id.dropdownName).text.toString(),
                    idNo = view.findViewById<AutoCompleteTextView>(R.id.etIdNo).text.toString(),
                    floor = view.findViewById<AutoCompleteTextView>(R.id.dropdownFloor).text.toString(),
                    station = view.findViewById<AutoCompleteTextView>(R.id.dropdownStation).text.toString(),
                    stationId = view.findViewById<AutoCompleteTextView>(R.id.etStationId).text.toString(),
                    issue = view.findViewById<AutoCompleteTextView>(R.id.dropdownIssue).text.toString(),
                    corrective = view.findViewById<AutoCompleteTextView>(R.id.etCorrective).text.toString(),
                    status = view.findViewById<AutoCompleteTextView>(R.id.dropdownStatus).text.toString(),
                    remark = view.findViewById<AutoCompleteTextView>(R.id.etRemark).text.toString()
                )
            )
        }

        return rows
    }





    private fun shareReportAsImage() {
        try {
            val originalBitmap = createReportBitmapFromData()

            val watermarkedBitmap = addPremiumWatermark(originalBitmap)

            val imagesDir = File(cacheDir, "images")
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val file = File(imagesDir, "report_ap.png")

            FileOutputStream(file).use { outputStream ->
                watermarkedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                clipData = ClipData.newUri(
                    contentResolver,
                    "Report AP Image",
                    uri
                )
            }

            startActivity(Intent.createChooser(shareIntent, "Share Report Image"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, e.message ?: "Image share failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun createReportBitmapFromData(): Bitmap {
        val rows = collectAllRows()

        val startX = 20f
        val startY = 40f
        val rowHeight = 90f

        val colWidths = floatArrayOf(
            50f,
            150f,
            150f,
            150f,
            150f,
            220f,
            220f,
            140f,
            140f
        )

        val headers = listOf(
            "Rec",
            "Name",
            "ID No",
            "Station",
            "Floor | Station ID",
            "Issue",
            "Corrective Action",
            "Status",
            "Remarks"
        )

        val totalWidth = (startX * 2 + colWidths.sum()).toInt()
        val totalHeight = (250 + ((rows.size + 1) * rowHeight)).toInt()

        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 30f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isAntiAlias = true
        }

        val headerTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.FILL
        }

        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        canvas.drawText("SHIFT END REPORT", startX, startY, titlePaint)
        canvas.drawText("Date: ${etDate.text}", startX, startY + 40f, textPaint)
        canvas.drawText("Shift: ${currentShift}", startX, startY + 75f, textPaint)

        var y = startY + 120f
        var x = startX

        for (i in headers.indices) {
            val w = colWidths[i]

            canvas.drawRect(x, y, x + w, y + rowHeight, headerPaint)
            canvas.drawRect(x, y, x + w, y + rowHeight, borderPaint)

            drawMultilineText(
                canvas,
                headers[i],
                headerTextPaint,
                x + 8f,
                y + 28f,
                w - 16f,
                22f
            )

            x += w
        }

        y += rowHeight

        rows.forEachIndexed { index, row ->
            x = startX

            val rowData = listOf(
                (index + 1).toString(),
                row.name,
                row.idNo,
                row.station,
                "${row.floor} / ${row.stationId}",
                row.issue,
                row.corrective,
                row.status,
                row.remark
            )

            for (i in rowData.indices) {
                val w = colWidths[i]

                canvas.drawRect(x, y, x + w, y + rowHeight, borderPaint)

                drawMultilineText(
                    canvas,
                    rowData[i],
                    textPaint,
                    x + 8f,
                    y + 25f,
                    w - 16f,
                    20f
                )

                x += w
            }

            y += rowHeight
        }

        return bitmap
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        paint: Paint,
        x: Float,
        y: Float,
        maxWidth: Float,
        lineHeight: Float
    ) {
        val words = text.split(" ")
        var line = ""
        var currentY = y

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"

            if (paint.measureText(testLine) <= maxWidth) {
                line = testLine
            } else {
                canvas.drawText(line, x, currentY, paint)
                line = word
                currentY += lineHeight
            }
        }

        if (line.isNotEmpty()) {
            canvas.drawText(line, x, currentY, paint)
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).format(Date())
    }

    private fun clearForm() {
        dropdownName.text.clear()
        etIdNo.text.clear()
        dropdownFloor.text.clear()
        dropdownStation.text.clear()
        etStationId.text.clear()
        dropdownIssue.text.clear()
        etCorrective.text.clear()
        dropdownStatus.text.clear()
        etRemark.text.clear()
    }

    private fun getAutoShift(): String {

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return when (hour) {

            in 13..23 -> "GD"

            else -> "GN"
        }
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

    private fun addPremiumWatermark(original: Bitmap): Bitmap {

        val result = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val watermarkText = "REPORT GENERATED BY WORK EASY APP"

        val marginRight = 25f
        val marginBottom = 25f

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
            alpha = 150
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }

        val x = result.width - marginRight

        val y = result.height - marginBottom -
                ((textPaint.descent() + textPaint.ascent()) / 2)

        canvas.drawText(
            watermarkText,
            x,
            y,
            textPaint
        )

        return result
    }
}