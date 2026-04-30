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

        btnShareImage.setOnClickListener {
            lifecycleScope.launch {
                saveAllToRoom()
                submitAllToGoogleForm()
                shareReportAsImage()
            }
        }

        btnSharePdf.setOnClickListener {
            lifecycleScope.launch {
                saveAllToRoom()
                submitAllToGoogleForm()
                shareReportAsPdf()
            }


        }


        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


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
                .url("https://script.google.com/macros/s/AKfycbzlMRiTBYM1xHFRvFeBQoE2wEUsTvh0sbyNQNpBavoOgE72hv62LzzF4QyYmCkNXKCd/exec")
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
            in 6..13 -> "GD"
            in 14..21 -> "GS"
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

//    private fun shareAllDataAsWhatsappText() {
//        val sb = StringBuilder()
//        sb.append("*SHIFT END REPORT*\n")
//        sb.append("Date: $currentDate    Shift: $currentShift\n\n")
//        sb.append("*ALL DATA (ROW & COLUMN)*\n\n")
//        sb.append("Record | Name | ID | Station | Station No | Issue | Corrective Action | Status | Remarks\n\n")
//
//        entryList.forEachIndexed { index, row ->
//            sb.append(
//                "${index + 1} | ${row.col1} | ${row.col2} | ${row.col3} | ${row.col4} | ${row.col5} | ${row.col6} | ${row.col7} | ${row.col8}\n"
//            )
//        }
//
//        val intent = Intent(Intent.ACTION_SEND)
//        intent.type = "text/plain"
//        intent.setPackage("com.whatsapp")
//        intent.putExtra(Intent.EXTRA_TEXT, sb.toString())
//
//        try {
//            startActivity(intent)
//        } catch (e: Exception) {
//            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun shareReportAsImage() {
        try {
            val bitmap = createReportBitmapFromData()

            val file = File(cacheDir, "shift_report.png")
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
            val bitmap = createReportBitmapFromData()

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(
                bitmap.width,
                bitmap.height,
                1
            ).create()

            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            document.finishPage(page)

            val file = File(cacheDir, "shift_report.pdf")
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



    private fun createReportBitmapFromData(): Bitmap {
        val startX = 20f
        val startY = 40f
        val rowHeight = 60f

        val colWidths = floatArrayOf(
            60f,   // Rec
            130f,  // Name
            120f,  // ID
            120f,  // Station
            120f,  // Station No
            180f,  // Issue
            220f,  // Corrective
            120f,  // Status
            140f   // Remarks
        )

        val headers = listOf(
            "Rec", "Name", "ID No", "Station", "Station No",
            "Issue", "Corrective Action", "Status", "Remarks"
        )

        val totalWidth = (startX * 2 + colWidths.sum()).toInt()
        val totalHeight = (220 + ((entryList.size + 1) * rowHeight)).toInt()

        val bitmap = Bitmap.createBitmap(
            totalWidth,
            totalHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 28f
            isFakeBoldText = true
        }

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 18f
        }

        val headerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            style = android.graphics.Paint.Style.FILL
        }

        val borderPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2f
        }

        canvas.drawText("SHIFT END REPORT", startX, startY, titlePaint)
        canvas.drawText("Date: $currentDate", startX, startY + 35f, textPaint)
        canvas.drawText("Shift: $currentShift", startX, startY + 65f, textPaint)

        var y = startY + 100f
        var x = startX

        for (i in headers.indices) {
            val w = colWidths[i]
            canvas.drawRect(x, y, x + w, y + rowHeight, headerPaint)
            canvas.drawRect(x, y, x + w, y + rowHeight, borderPaint)
            canvas.drawText(headers[i], x + 8f, y + 35f, textPaint)
            x += w
        }

        y += rowHeight

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
                canvas.drawRect(x, y, x + w, y + rowHeight, borderPaint)
                canvas.drawText(rowData[i].take(18), x + 8f, y + 35f, textPaint)
                x += w
            }

            y += rowHeight
        }

        return bitmap
    }

}