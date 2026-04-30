package com.example.myapplication
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.*

@Dao
interface WorkDao {



//    @Insert
//    suspend fun insert(data: WorkData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: WorkData)

//    @Query("SELECT * FROM work_table ORDER BY empId DESC")
//    suspend fun getAll(): List<WorkData>

    @Update
    suspend fun update(data: WorkData)


    @Delete
    suspend fun delete(data: WorkData)

    @Query("SELECT * FROM work_table ORDER BY empId DESC LIMIT 1")
    suspend fun getLastEntry(): WorkData?

    @Query("SELECT * FROM work_table WHERE empId = :id LIMIT 1")
    suspend fun getUserByEmpId(id: String): WorkData?

    @Query("SELECT * FROM work_table ORDER BY id DESC")
    suspend fun getAll(): List<WorkData>

    @Query("SELECT * FROM work_table WHERE empId = :empId AND date = :date LIMIT 1")
    suspend fun getByEmpAndDate(empId: String, date: String): WorkData?


}






