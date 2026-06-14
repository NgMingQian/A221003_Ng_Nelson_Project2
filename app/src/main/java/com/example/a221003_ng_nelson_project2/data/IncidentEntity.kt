package com.example.a221003_ng_nelson_project2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incident_table")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val username: String,
    val type: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)