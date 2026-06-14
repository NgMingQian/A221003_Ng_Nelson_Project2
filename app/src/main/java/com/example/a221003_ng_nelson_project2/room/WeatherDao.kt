package com.example.a221003_ng_nelson_project2.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Insert
    suspend fun insert(weather: WeatherEntity)

    @Query("SELECT * FROM weather_table ORDER BY timestamp DESC")
    fun getAll(): Flow<List<WeatherEntity>>
}