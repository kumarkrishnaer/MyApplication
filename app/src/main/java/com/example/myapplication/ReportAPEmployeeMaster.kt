package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employee_master_table")
data class ReportAPEmployeeMaster(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val empName: String,
    val empId: String
)