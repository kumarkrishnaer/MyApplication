package com.example.myapplication
import android.widget.LinearLayout
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
class ViewReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_report)




        val txtDate = findViewById<TextView>(R.id.txtDate)
        val txtName = findViewById<TextView>(R.id.txtName)
        val txtId = findViewById<TextView>(R.id.txtId)
        val txtShift = findViewById<TextView>(R.id.txtShift)
        val txtStationID = findViewById<TextView>(R.id.txtStationID)
        val txtTeam = findViewById<TextView>(R.id.txtTeam)
        val txtLine = findViewById<TextView>(R.id.txtLine)
        val txtDowntime = findViewById<TextView>(R.id.txtDowntime)
        val txtVendor = findViewById<TextView>(R.id.txtVendor)
        val txtdescription = findViewById<TextView>(R.id.txtdescription)
        val txtmachine = findViewById<TextView>(R.id.txtmachine)
        val txtanalysis = findViewById<TextView>(R.id.txtanalysis)
        val txtrootCause = findViewById<TextView>(R.id.txtrootCause)
        val txtcorrective = findViewById<TextView>(R.id.txtcorrective)
        val txtpreventiveAction = findViewById<TextView>(R.id.txtpreventiveAction)

        val txtissueStart = findViewById<TextView>(R.id.txtissueStart)
        val txtissueEnd = findViewById<TextView>(R.id.txtissueEnd)
        val txttotalDowntime = findViewById<TextView>(R.id.txttotalDowntime)
        val txthandledBy = findViewById<TextView>(R.id.txthandledBy)
        val txtspareChanged = findViewById<TextView>(R.id.txtspareChanged)
        val txtissueStatus = findViewById<TextView>(R.id.txtissueStatus)
        val txtskillLevel = findViewById<TextView>(R.id.txtskillLevel)




        val date = intent.getStringExtra("date")
        val name = intent.getStringExtra("name")
        val id = intent.getStringExtra("id")
        val shift = intent.getStringExtra("shift")
        val stationId = intent.getStringExtra("stationId")
        val team = intent.getStringExtra("team")
        val line = intent.getStringExtra("line")
        val downtime = intent.getStringExtra("downtime")
        val vendor = intent.getStringExtra("vendor")
        val description = intent.getStringExtra("description")
        val machine = intent.getStringExtra("machine")
        val analysis = intent.getStringExtra("analysis")
        val rootCause = intent.getStringExtra("rootCause")
        val corrective = intent.getStringExtra("corrective")
        val startTime = intent.getStringExtra("startTime")
        val endTime = intent.getStringExtra("endTime")
        val totalTime = intent.getStringExtra("totalTime")
        val handledBy = intent.getStringExtra("handledBy")
        val spareChanged = intent.getStringExtra("spareChanged")
        val issueStatus = intent.getStringExtra("issueStatus")
        val preventiveAction = intent.getStringExtra("preventiveAction")
        val skillLevel = intent.getStringExtra("skillLevel")

        txtDate.text = ":$date"
        txtName.text = ":$name"
        txtId.text = ":$id"
        txtShift.text = ":$shift"
        txtStationID.text = ":$stationId"
        txtTeam.text = ":$team"
        txtLine.text = ":$line"
        txtDowntime.text = ":$downtime"
        txtVendor.text = ":$vendor"
        txtdescription.text = ":$description"
        txtmachine.text = ":$machine"
        txtanalysis.text = ":$analysis"
        txtrootCause.text = ":$rootCause"
        txtcorrective.text = ":$corrective"
        txtpreventiveAction.text = ":$preventiveAction"
        txtissueStart.text = ":$startTime"
        txtissueEnd.text = ":$endTime"
        txttotalDowntime.text = ":$totalTime"
        txthandledBy.text = ":$handledBy"
        txtspareChanged.text = ":$spareChanged"
        txtissueStatus.text = ":$issueStatus"
        txtskillLevel.text = ":$skillLevel"





        // ✅ SHOW / HIDE SECTION

        val downtimeLayout = findViewById<LinearLayout>(R.id.layoutDowntime)

        if (downtime == "Yes") {
            downtimeLayout.visibility = View.VISIBLE
        } else {
            downtimeLayout.visibility = View.GONE
        }

        // ✅ END  / HIDE SECTION

    }



}