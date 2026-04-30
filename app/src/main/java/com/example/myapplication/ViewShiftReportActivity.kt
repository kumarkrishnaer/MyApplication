package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import android.graphics.Color
import android.graphics.Paint

class ViewShiftReportActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tvViewHeader: TextView

    private lateinit var btnViewBack: Button
    private lateinit var btnShareImage: Button
    private lateinit var btnSharePdf: Button
    private lateinit var recyclerViewReport: RecyclerView
    private lateinit var reportContainer: View
    private lateinit var db: AppDatabase

    private var reportList: List<ShiftEndReportData> = emptyList()
    private var date: String = ""
    private var shift: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_shift_report)

        toolbar = findViewById(R.id.viewToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvViewHeader = findViewById(R.id.tvViewHeader)
        btnViewBack = findViewById(R.id.btnViewBack)
        btnShareImage = findViewById(R.id.btnShareImage)
        btnSharePdf = findViewById(R.id.btnSharePdf)
        recyclerViewReport = findViewById(R.id.recyclerViewReport)
        reportContainer = findViewById(R.id.reportContainer)

        recyclerViewReport.layoutManager = LinearLayoutManager(this)
        db = AppDatabase.getDatabase(this)

        date = intent.getStringExtra("date").orEmpty()
        shift = intent.getStringExtra("shift").orEmpty()

        tvViewHeader.text = "Date: $date    Shift: $shift"

        loadReport()

        btnViewBack.setOnClickListener {
            finish()
        }


        btnShareImage.setOnClickListener {
            if (reportList.isEmpty()) {
                Toast.makeText(this, "No records to share", Toast.LENGTH_SHORT).show()
            } else {
                shareReportAsImage()
            }
        }

        btnSharePdf.setOnClickListener {
            if (reportList.isEmpty()) {
                Toast.makeText(this, "No records to share", Toast.LENGTH_SHORT).show()
            } else {
                shareReportAsPdf()
            }
        }
    }

    private fun loadReport() {
        lifecycleScope.launch {
            reportList = db.shiftEndReportDao().getByDateShift(date, shift)
            recyclerViewReport.adapter = ViewShiftReportAdapter(reportList)
        }
    }





    private fun shareReportAsImage() {
        try {
            val bitmap = createTableBitmap()

            val file = File(cacheDir, "shift_end_report.png")
            val outputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/png"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            startActivity(Intent.createChooser(intent, "Share Image"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Image share failed", Toast.LENGTH_SHORT).show()
        }
    }



    private fun shareReportAsPdf() {
        try {
            val bitmap = createTableBitmap()

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(
                bitmap.width,
                bitmap.height,
                1
            ).create()

            val page = document.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            document.finishPage(page)

            val file = File(cacheDir, "shift_end_report.pdf")
            val fos = FileOutputStream(file)

            document.writeTo(fos)
            fos.flush()
            fos.close()
            document.close()

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/pdf"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            startActivity(Intent.createChooser(intent, "Share PDF"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "PDF share failed", Toast.LENGTH_SHORT).show()
        }
    }



    private fun createTableBitmap(): Bitmap {

        val rowHeight = 70f
        val startX = 30f
        var startY = 70f

        val columnWidths = floatArrayOf(
            70f,   // Rec
            150f,  // Name
            120f,  // ID No
            150f,  // Station
            140f,  // Station No
            180f,  // Issue
            230f,  // Corrective Action
            140f,  // Status
            160f   // Remarks
        )

        val totalWidth = columnWidths.sum().toInt() + 60
        val totalHeight = (230 + ((reportList.size + 1) * rowHeight)).toInt()

        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 38f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 26f
            isAntiAlias = true
        }

        val headerTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(220, 220, 220)
            style = Paint.Style.FILL
        }

        // Title
        canvas.drawText("SHIFT END REPORT", startX, startY, titlePaint)

        startY += 45f
        canvas.drawText("Date: $date", startX, startY, textPaint)

        startY += 35f
        canvas.drawText("Shift: $shift", startX, startY, textPaint)

        startY += 45f

        val headers = arrayOf(
            "Rec",
            "Name",
            "ID No",
            "Station",
            "Station No",
            "Issue",
            "Corrective",
            "Status",
            "Remarks"
        )

        // Header row
        var x = startX
        val headerTop = startY
        val headerBottom = startY + rowHeight

        headers.forEachIndexed { i, title ->
            val cellWidth = columnWidths[i]

            canvas.drawRect(x, headerTop, x + cellWidth, headerBottom, headerPaint)
            canvas.drawRect(x, headerTop, x + cellWidth, headerBottom, linePaint)
            canvas.drawText(title, x + 8f, headerTop + 42f, headerTextPaint)

            x += cellWidth
        }

        startY += rowHeight

        // Data rows
        reportList.forEachIndexed { index, row ->

            val values = arrayOf(
                (index + 1).toString(),
                row.name,
                row.empId,
                row.station,
                row.stationNo,
                row.issue,
                row.correctiveAction,
                row.status,
                row.remarks
            )

            var cellX = startX
            val rowTop = startY
            val rowBottom = startY + rowHeight

            values.forEachIndexed { i, value ->
                val cellWidth = columnWidths[i]

                canvas.drawRect(cellX, rowTop, cellX + cellWidth, rowBottom, linePaint)

                canvas.drawText(
                    value.take(13),
                    cellX + 8f,
                    rowTop + 42f,
                    textPaint
                )

                cellX += cellWidth
            }

            startY += rowHeight
        }

        return bitmap
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}