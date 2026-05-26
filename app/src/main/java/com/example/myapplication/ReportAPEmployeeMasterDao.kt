package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReportAPEmployeeMasterDao {

    @Insert
    suspend fun insertEmployee(employee: ReportAPEmployeeMaster)

    @Query("SELECT empName FROM employee_master_table ORDER BY empName ASC")
    suspend fun getAllNames(): List<String>

    @Query("SELECT empId FROM employee_master_table ORDER BY empId ASC")
    suspend fun getAllIds(): List<String>

    @Query("SELECT empId FROM employee_master_table WHERE empName = :name LIMIT 1")
    suspend fun getIdByName(name: String): String?

    @Query("SELECT empName FROM employee_master_table WHERE empId = :id LIMIT 1")
    suspend fun getNameById(id: String): String?

    @Query("DELETE FROM employee_master_table WHERE empName = :name")
    suspend fun deleteEmployeeByName(name: String)

    @Query("DELETE FROM employee_master_table WHERE empId = :id")
    suspend fun deleteEmployeeById(id: String)

    @Query("""DELETE FROM employee_master_table WHERE empName = :name OR empId = :id""")
    suspend fun deleteEmployeeByNameOrId(name: String, id: String)
}