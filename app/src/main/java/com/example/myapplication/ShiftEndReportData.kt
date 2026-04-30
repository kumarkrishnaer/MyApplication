package com.example.myapplication

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shift_end_report",
    indices = [Index(value = ["date", "shift"])]
)
data class ShiftEndReportData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: String,
    val shift: String,

    val name: String,
    val empId: String,
    val station: String,
    val stationNo: String,
    val issue: String,
    val correctiveAction: String,
    val status: String,
    val remarks: String,

    val createdAt: Long = System.currentTimeMillis()
)