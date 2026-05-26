package com.example.myapplication
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import android.content.ClipData
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.appcompat.app.AlertDialog

class ShiftEndReportActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private lateinit var tvDate: TextView
    private lateinit var tvShift: TextView
    private lateinit var reportContainer: View
    private lateinit var db: AppDatabase
    private lateinit var currentDate: String
    private lateinit var currentShift: String

    private val entryList = mutableListOf<ReportRow>()
    private lateinit var adapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shift_end_report)

        recyclerView = findViewById(R.id.recyclerView)
        tvDate = findViewById(R.id.tvDate)
        tvShift = findViewById(R.id.tvShift)
        reportContainer = findViewById(R.id.reportContainer)

        val btnAddRow = findViewById<MaterialCardView>(R.id.btnAddRow)
        val btnShareImage = findViewById<MaterialCardView>(R.id.btnShareImage)
        val btnSharePdf = findViewById<MaterialCardView>(R.id.btnSharePdf)

        db = AppDatabase.getDatabase(this)

        currentDate = getCurrentDate()
        currentShift = getAutoShift()

        tvDate.text = "Date: $currentDate"
        tvShift.text = "Shift: $currentShift"


        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        adapter = ReportAdapter(entryList) { position ->
            if (entryList.isNotEmpty()) {
                entryList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, entryList.size)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        entryList.add(ReportRow())
        adapter.notifyDataSetChanged()

        btnAddRow.setOnClickListener {
            entryList.add(ReportRow())
            adapter.notifyItemInserted(entryList.size - 1)
            recyclerView.smoothScrollToPosition(entryList.size - 1)
        }

//        btnSaveAll.setOnClickListener {
//            saveAllToRoom()
//        }

//        btnShareWhatsapp.setOnClickListener {
//            shareAllDataAsWhatsappText()
//        }

//        btnShareImage.setOnClickListener {
//            shareReportAsImage()
//        }
//
//        btnSharePdf.setOnClickListener {
//            shareReportAsPdf()
//        }
//=================== btn image share =================================
        btnShareImage.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Submit Report")
                .setMessage("Do you want to save and upload this report to Google Sheet?")

                .setPositiveButton("YES") { _, _ ->

                    lifecycleScope.launch {
                        shareReportAsImage()
                        saveAllToRoom()
                        submitAllToGoogleForm()
                    }
                }

                .setNegativeButton("NO") { _, _ ->

                    shareReportAsImage()
                    saveAllToRoom()
                    saveReportLog("Shift End Report Updated")

                    Toast.makeText(
                        this,
                        "Only image shared",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                .show()
        }

        //======================== btn pdf share =========================

//        btnSharePdf.setOnClickListener {
//            lifecycleScope.launch {
//                shareReportAsPdf()
//                saveAllToRoom()
//                submitAllToGoogleForm()
//
//            }
//        }

        btnSharePdf.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Submit Report")
                .setMessage("Do you want to save and upload this report to Google Sheet?")

                .setPositiveButton("YES") { _, _ ->

                    lifecycleScope.launch {
                        shareReportAsPdf()
                        saveAllToRoom()
                        submitAllToGoogleForm()
                    }
                }

                .setNegativeButton("NO") { _, _ ->

                    shareReportAsPdf()
                    saveAllToRoom()

                    Toast.makeText(
                        this,
                        "Only PDF shared",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                .show()
        }



//        setSupportActionBar(toolbar)


        val rootLayout = findViewById<View>(R.id.rootLayout)
        val shareButtonsLayout = findViewById<View>(R.id.shareButtonsLayout)

        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            rootLayout.getWindowVisibleDisplayFrame(rect)

            val screenHeight = rootLayout.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                shareButtonsLayout.visibility = View.GONE
            } else {
                shareButtonsLayout.visibility = View.VISIBLE
            }
        }



    }

    //=============== share to google form ===================

    private suspend fun submitAllToGoogleForm() {
        val validList = entryList.filter {
            it.col1.isNotBlank() || it.col2.isNotBlank() || it.col5.isNotBlank()
        }

        if (validList.isEmpty()) return

        withContext(Dispatchers.IO) {

            val rowsJson = validList.joinToString(",") { row ->
                """
            {
              "name": "${row.col1}",
              "id": "${row.col2}",
              "stationName": "${row.col3}",
              "stationId": "${row.col4}",
              "issue": "${row.col5}",
              "correctiveAction": "${row.col6}",
              "status": "${row.col7}",
              "remark": "${row.col8}"
            }
            """.trimIndent()
            }

            val json = """
        {
          "reportType": "shift_end_bulk",
          "date": "$currentDate",
          "shift": "$currentShift",
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
            this@ShiftEndReportActivity,
            "All rows uploaded to Sheet2 ✅",
            Toast.LENGTH_SHORT
        ).show()
    }




    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_shift_report, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_history) {
            startActivity(Intent(this, ShiftHistoryActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Calendar.getInstance().time)
    }

    private fun getAutoShift(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 13..17 -> "GS"
            in 14..21 -> "GD"
            else -> "GN"
        }
    }

    private fun saveAllToRoom() {
        val validList = entryList.filter {
            it.col1.isNotBlank() || it.col2.isNotBlank() || it.col5.isNotBlank()
        }

        if (validList.isEmpty()) {
            Toast.makeText(this, "Please enter at least one record", Toast.LENGTH_SHORT).show()
            return
        }

        val saveList = validList.map {
            ShiftEndReportData(
                date = currentDate,
                shift = currentShift,
                name = it.col1,
                empId = it.col2,
                station = it.col3,
                stationNo = it.col4,
                issue = it.col5,
                correctiveAction = it.col6,
                status = it.col7,
                remarks = it.col8
            )
        }

        lifecycleScope.launch {
            db.shiftEndReportDao().deleteByDateShift(currentDate, currentShift)
            db.shiftEndReportDao().insertAll(saveList)
            Toast.makeText(this@ShiftEndReportActivity, "Saved successfully ✅", Toast.LENGTH_SHORT).show()
        }
    }



    private fun shareReportAsImage() {

        try {

            val bitmap = createReportBitmapFromData()

            val watermarkedBitmap =
                addPremiumWatermark(bitmap)

            val file = File(cacheDir, "shift_report.png")

            FileOutputStream(file).use { outputStream ->

                watermarkedBitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    outputStream
                )
            }

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {

                type = "image/png"

                putExtra(
                    Intent.EXTRA_STREAM,
                    uri
                )

                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                clipData = ClipData.newUri(
                    contentResolver,
                    "Shift Report Image",
                    uri
                )
            }

            startActivity(
                Intent.createChooser(
                    shareIntent,
                    "Share Image"
                )
            )

        } catch (e: Exception) {

            e.printStackTrace()

            Toast.makeText(
                this,
                e.message ?: "Image share failed",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun shareReportAsPdf() {
        var document: PdfDocument? = null

        try {
            val bitmap =
                addPremiumWatermark(
                    createReportBitmapFromData()
                )

            document = PdfDocument()

            val pageInfo = PdfDocument.PageInfo.Builder(
                bitmap.width,
                bitmap.height,
                1
            ).create()

            val page = document.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            document.finishPage(page)

            val file = File(cacheDir, "shift_report.pdf")

            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newUri(contentResolver, "Shift Report PDF", uri)
            }

            startActivity(Intent.createChooser(shareIntent, "Share PDF"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, e.message ?: "PDF share failed", Toast.LENGTH_LONG).show()
        } finally {
            document?.close()
        }
    }

    private fun createReportBitmapFromData(): Bitmap {

        val startX = 20f
        val startY = 40f
        val rowHeight = 90f

        val colWidths = floatArrayOf(
            60f,   // Rec
            150f,  // Name
            120f,  // ID
            120f,  // Station
            120f,  // Station No
            220f,  // Issue
            220f,  // Corrective
            120f,  // Status
            120f   // Remarks
        )

        val headers = listOf(
            "Rec",
            "Name",
            "ID No",
            "Station",
            "Station No",
            "Issue",
            "Corrective Action",
            "Status",
            "Remarks"
        )

        val totalWidth = (startX * 2 + colWidths.sum()).toInt()

        val totalHeight = (
                250 + ((entryList.size + 1) * rowHeight)
                ).toInt()

        val bitmap = Bitmap.createBitmap(
            totalWidth,
            totalHeight,
            Bitmap.Config.ARGB_8888
        )

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

        // Title
        canvas.drawText(
            "SHIFT END REPORT",
            startX,
            startY,
            titlePaint
        )

        canvas.drawText(
            "Date: $currentDate",
            startX,
            startY + 40f,
            textPaint
        )

        canvas.drawText(
            "Shift: $currentShift",
            startX,
            startY + 75f,
            textPaint
        )

        var y = startY + 120f

        // Header Row
        var x = startX

        for (i in headers.indices) {

            val w = colWidths[i]

            canvas.drawRect(
                x,
                y,
                x + w,
                y + rowHeight,
                headerPaint
            )

            canvas.drawRect(
                x,
                y,
                x + w,
                y + rowHeight,
                borderPaint
            )

            drawMultilineText(
                canvas = canvas,
                text = headers[i],
                paint = headerTextPaint,
                x = x + 8f,
                y = y + 28f,
                maxWidth = w - 16f,
                lineHeight = 22f
            )

            x += w
        }

        y += rowHeight

        // Data Rows
        entryList.forEachIndexed { index, row ->

            x = startX

            val rowData = listOf(
                (index + 1).toString(),
                row.col1,
                row.col2,
                row.col3,
                row.col4,
                row.col5,
                row.col6,
                row.col7,
                row.col8
            )

            for (i in rowData.indices) {

                val w = colWidths[i]

                canvas.drawRect(
                    x,
                    y,
                    x + w,
                    y + rowHeight,
                    borderPaint
                )

                drawMultilineText(
                    canvas = canvas,
                    text = rowData[i],
                    paint = textPaint,
                    x = x + 8f,
                    y = y + 25f,
                    maxWidth = w - 16f,
                    lineHeight = 20f
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

            val testLine =
                if (line.isEmpty()) word
                else "$line $word"

            if (paint.measureText(testLine) <= maxWidth) {

                line = testLine

            } else {

                canvas.drawText(
                    line,
                    x,
                    currentY,
                    paint
                )

                line = word

                currentY += lineHeight
            }
        }

        if (line.isNotEmpty()) {

            canvas.drawText(
                line,
                x,
                currentY,
                paint
            )
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
            color = Color.GRAY
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