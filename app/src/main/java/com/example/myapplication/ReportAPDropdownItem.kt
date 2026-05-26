package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "report_ap_dropdown_table")
data class ReportAPDropdownItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val value: String
)