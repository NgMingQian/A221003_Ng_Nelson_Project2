package com.example.a221003_ng_nelson_project2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IncidentDao {

    @Insert
    suspend fun insertIncident(incident: IncidentEntity)

    @Query("SELECT * FROM incident_table ORDER BY id DESC")
    suspend fun getAllIncidents(): List<IncidentEntity>
}