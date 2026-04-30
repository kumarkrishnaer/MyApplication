package com.example.myapplication
import androidx.room.Index
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_table",
    indices = [Index(value = ["empId", "date"], unique = true)] // 🔥 UNIQUE COMBO
)
data class WorkData(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val empId: String,
    val date: String,

    val name: String,
    val stationId: String,
    val shift: String,
    val team: String,
    val line: String,

    val downtime: String,

    val vendor: String?,
    val description: String?,
    val machine: String?,
    val analysis: String?,
    val rootCause: String?,
    val corrective: String?,

    val startTime: String?,
    val endTime: String?,
    val totalTime: String?,

    val handledBy: String?,

    val spareChanged: String,

    val issueStatus:String,
    val preventiveAction:String,
    val skillLevel:String

)