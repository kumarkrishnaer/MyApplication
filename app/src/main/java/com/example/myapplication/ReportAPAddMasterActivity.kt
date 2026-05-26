package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ReportAPAddMasterActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    private lateinit var etName: AutoCompleteTextView
    private lateinit var etId: AutoCompleteTextView
    private lateinit var etFloor: AutoCompleteTextView
    private lateinit var etStation: AutoCompleteTextView
    private lateinit var etStationId: AutoCompleteTextView
    private lateinit var etIssue: AutoCompleteTextView
    private lateinit var etCorrective: AutoCompleteTextView
    private lateinit var etStatus: AutoCompleteTextView
    private lateinit var etRemark: AutoCompleteTextView

    private lateinit var btnAddName: Button
    private lateinit var btnDeleteName: Button
    private lateinit var btnAddFloor: Button
    private lateinit var btnDeleteFloor: Button
    private lateinit var btnAddStation: Button
    private lateinit var btnDeleteStation: Button
    private lateinit var btnAddStationId: Button
    private lateinit var btnDeleteStationId: Button
    private lateinit var btnAddIssue: Button
    private lateinit var btnDeleteIssue: Button
    private lateinit var btnAddStatus: Button
    private lateinit var btnDeleteStatus: Button
    private lateinit var btnAddRemark: Button
    private lateinit var btnDeleteRemark: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.report_ap_activity_add_master)

        db = AppDatabase.getDatabase(this)
        bindViews()
        setupDropdownClick()
        setupButtons()

        loadEmployeeDropdowns()
        loadAllDropdowns()
        setupEmployeePairing()
        setupIssueCorrectivePairing()
    }

    private fun bindViews() {
        etName = findViewById(R.id.etName)
        etId = findViewById(R.id.etId)
        etFloor = findViewById(R.id.etFloor)
        etStation = findViewById(R.id.etStation)
        etStationId = findViewById(R.id.etStationId)
        etIssue = findViewById(R.id.etIssue)
        etCorrective = findViewById(R.id.etCorrective)
        etStatus = findViewById(R.id.etStatus)
        etRemark = findViewById(R.id.etRemark)

        btnAddName = findViewById(R.id.btnAddName)
        btnDeleteName = findViewById(R.id.btnDeleteName)
        btnAddFloor = findViewById(R.id.btnAddFloor)
        btnDeleteFloor = findViewById(R.id.btnDeleteFloor)
        btnAddStation = findViewById(R.id.btnAddStation)
        btnDeleteStation = findViewById(R.id.btnDeleteStation)
        btnAddStationId = findViewById(R.id.btnAddStationId)
        btnDeleteStationId = findViewById(R.id.btnDeleteStationId)
        btnAddIssue = findViewById(R.id.btnAddIssue)
        btnDeleteIssue = findViewById(R.id.btnDeleteIssue)
        btnAddStatus = findViewById(R.id.btnAddStatus)
        btnDeleteStatus = findViewById(R.id.btnDeleteStatus)
        btnAddRemark = findViewById(R.id.btnAddRemark)
        btnDeleteRemark = findViewById(R.id.btnDeleteRemark)
    }

    private fun setupButtons() {
        btnAddName.setOnClickListener { saveEmployeePair() }
        btnDeleteName.setOnClickListener { deleteEmployeePair() }

        btnAddFloor.setOnClickListener { saveSingleValue("Floor", etFloor) }
        btnDeleteFloor.setOnClickListener { deleteSingleValue("Floor", etFloor) }

        btnAddStation.setOnClickListener { saveSingleValue("Station", etStation) }
        btnDeleteStation.setOnClickListener { deleteSingleValue("Station", etStation) }

        btnAddStationId.setOnClickListener { saveSingleValue("Station ID", etStationId) }
        btnDeleteStationId.setOnClickListener { deleteSingleValue("Station ID", etStationId) }

        // Issue + Corrective are saved/deleted together from the same ADD/DEL buttons.
        btnAddIssue.setOnClickListener { saveIssueCorrectivePair() }
        btnDeleteIssue.setOnClickListener { deleteIssueCorrectivePair() }

        btnAddStatus.setOnClickListener { saveSingleValue("Status", etStatus) }
        btnDeleteStatus.setOnClickListener { deleteSingleValue("Status", etStatus) }

        btnAddRemark.setOnClickListener { saveSingleValue("Remark", etRemark) }
        btnDeleteRemark.setOnClickListener { deleteSingleValue("Remark", etRemark) }
    }

    private fun setupDropdownClick() {
        listOf(etName, etId, etFloor, etStation, etStationId, etIssue, etCorrective, etStatus, etRemark)
            .forEach { view ->
                view.threshold = 0
                view.setOnClickListener { view.showDropDown() }
                view.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) view.showDropDown() }
            }
    }

    private fun setupEmployeePairing() {
        etName.setOnItemClickListener { _, _, _, _ ->
            val selectedName = etName.text.toString().trim()
            lifecycleScope.launch {
                val id = db.reportAPEmployeeMasterDao().getIdByName(selectedName)
                if (!id.isNullOrBlank()) etId.setText(id, false)
            }
        }

        etId.setOnItemClickListener { _, _, _, _ ->
            val selectedId = etId.text.toString().trim()
            lifecycleScope.launch {
                val name = db.reportAPEmployeeMasterDao().getNameById(selectedId)
                if (!name.isNullOrBlank()) etName.setText(name, false)
            }
        }
    }

    private fun setupIssueCorrectivePairing() {
        etIssue.setOnItemClickListener { _, _, _, _ ->
            val selectedIssue = etIssue.text.toString().trim()
            lifecycleScope.launch {
                val corrective = db.reportAPDropdownDao().getValuesByCategory(issueCorrectiveCategory(selectedIssue)).firstOrNull()
                if (!corrective.isNullOrBlank()) etCorrective.setText(corrective, false)
            }
        }
    }

    private fun saveEmployeePair() {
        val name = etName.text.toString().trim()
        val id = etId.text.toString().trim()

        if (name.isEmpty() || id.isEmpty()) {
            Toast.makeText(this, "Enter both Name and ID", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            db.reportAPEmployeeMasterDao().insertEmployee(ReportAPEmployeeMaster(empName = name, empId = id))
            etName.setText("", false)
            etId.setText("", false)
            loadEmployeeDropdowns()
            Toast.makeText(this@ReportAPAddMasterActivity, "Employee saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteEmployeePair() {
        val name = etName.text.toString().trim()
        val id = etId.text.toString().trim()

        if (name.isEmpty() && id.isEmpty()) {
            Toast.makeText(this, "Select Name or ID to delete", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            db.reportAPEmployeeMasterDao().deleteEmployeeByNameOrId(name, id)
            etName.setText("", false)
            etId.setText("", false)
            etName.dismissDropDown()
            etId.dismissDropDown()
            loadEmployeeDropdowns()
            Toast.makeText(this@ReportAPAddMasterActivity, "Employee deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveIssueCorrectivePair() {
        val issue = etIssue.text.toString().trim()
        val corrective = etCorrective.text.toString().trim()

        if (issue.isEmpty() || corrective.isEmpty()) {
            Toast.makeText(this, "Enter both Issue and Corrective", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            db.reportAPDropdownDao().deleteValue("Issue", issue)
            db.reportAPDropdownDao().deleteValue(issueCorrectiveCategory(issue), corrective)

            db.reportAPDropdownDao().insert(ReportAPDropdownItem(category = "Issue", value = issue))
            db.reportAPDropdownDao().insert(ReportAPDropdownItem(category = issueCorrectiveCategory(issue), value = corrective))

            etIssue.setText("", false)
            etCorrective.setText("", false)
            loadAllDropdowns()
            Toast.makeText(this@ReportAPAddMasterActivity, "Issue & Corrective saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteIssueCorrectivePair() {
        val issue = etIssue.text.toString().trim()

        if (issue.isEmpty()) {
            Toast.makeText(this, "Select Issue to delete", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            db.reportAPDropdownDao().deleteValue("Issue", issue)
            val correctiveList = db.reportAPDropdownDao().getValuesByCategory(issueCorrectiveCategory(issue))
            correctiveList.forEach { corrective ->
                db.reportAPDropdownDao().deleteValue(issueCorrectiveCategory(issue), corrective)
            }

            etIssue.setText("", false)
            etCorrective.setText("", false)
            loadAllDropdowns()
            Toast.makeText(this@ReportAPAddMasterActivity, "Issue & Corrective deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSingleValue(category: String, view: AutoCompleteTextView) {
        val value = view.text.toString().trim()

        if (value.isEmpty()) {
            Toast.makeText(this, "Please enter $category", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            db.reportAPDropdownDao().deleteValue(category, value)
            db.reportAPDropdownDao().insert(ReportAPDropdownItem(category = category, value = value))
            view.setText("", false)
            loadAllDropdowns()
            Toast.makeText(this@ReportAPAddMasterActivity, "$category saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteSingleValue(category: String, view: AutoCompleteTextView) {
        val value = view.text.toString().trim()

        if (value.isEmpty()) {
            Toast.makeText(this, "Select $category value to delete", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            db.reportAPDropdownDao().deleteValue(category, value)
            view.setText("", false)
            loadAllDropdowns()
            Toast.makeText(this@ReportAPAddMasterActivity, "$category deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadEmployeeDropdowns() {
        lifecycleScope.launch {
            val names = db.reportAPEmployeeMasterDao().getAllNames()
            val ids = db.reportAPEmployeeMasterDao().getAllIds()
            etName.setAdapter(ArrayAdapter(this@ReportAPAddMasterActivity, android.R.layout.simple_dropdown_item_1line, names))
            etId.setAdapter(ArrayAdapter(this@ReportAPAddMasterActivity, android.R.layout.simple_dropdown_item_1line, ids))
        }
    }

    private fun loadAllDropdowns() {
        loadDropdown("Floor", etFloor)
        loadDropdown("Station", etStation)
        loadDropdown("Station ID", etStationId)
        loadDropdown("Issue", etIssue)
        loadDropdown("Status", etStatus)
        loadDropdown("Remark", etRemark)
    }

    private fun loadDropdown(category: String, view: AutoCompleteTextView) {
        lifecycleScope.launch {
            val list = db.reportAPDropdownDao().getValuesByCategory(category)
            view.setAdapter(ArrayAdapter(this@ReportAPAddMasterActivity, android.R.layout.simple_dropdown_item_1line, list))
        }
    }

    private fun issueCorrectiveCategory(issue: String): String = "Corrective::$issue"
}
