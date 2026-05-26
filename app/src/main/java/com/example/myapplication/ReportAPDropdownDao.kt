package com.example.myapplication

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReportAPDropdownDao {

    @Insert
    suspend fun insert(item: ReportAPDropdownItem)

    @Delete
    suspend fun delete(item: ReportAPDropdownItem)

    @Query(
        "SELECT DISTINCT value FROM report_ap_dropdown_table " +
                "WHERE category = :category ORDER BY value ASC"
    )
    suspend fun getValuesByCategory(
        category: String
    ): List<String>

    @Query(
        "DELETE FROM report_ap_dropdown_table " +
                "WHERE category = :category AND value = :value"
    )
    suspend fun deleteValue(
        category: String,
        value: String
    )
}