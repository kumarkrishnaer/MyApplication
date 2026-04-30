package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity

class FaReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fa_report)

        val dropdown = findViewById<AutoCompleteTextView>(R.id.dropdownReport)

        val items = listOf(
            "FA Summary",
            "FA Detailed Report",
            "Asset Location Report",
            "Asset Transfer Report"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            items
        )

        dropdown.setAdapter(adapter)
    }
}