package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ShiftEndReportDao {

    @Insert
    suspend fun insertAll(list: List<ShiftEndReportData>)

    @Query("SELECT * FROM shift_end_report WHERE date = :date AND shift = :shift ORDER BY id ASC")
    suspend fun getByDateShift(date: String, shift: String): List<ShiftEndReportData>

    @Query("""
        SELECT date, shift, COUNT(*) as total
        FROM shift_end_report
        GROUP BY date, shift
        ORDER BY createdAt DESC
    """)
    suspend fun getHistory(): List<ShiftHistoryModel>

    @Query("DELETE FROM shift_end_report WHERE date = :date AND shift = :shift")
    suspend fun deleteByDateShift(date: String, shift: String)
}