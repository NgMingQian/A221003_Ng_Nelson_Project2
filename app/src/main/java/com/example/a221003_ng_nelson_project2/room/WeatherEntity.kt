package com.example.a221003_ng_nelson_project2.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val city: String,
    val temperature: Double,
    val timestamp: Long = System.currentTimeMillis()
)