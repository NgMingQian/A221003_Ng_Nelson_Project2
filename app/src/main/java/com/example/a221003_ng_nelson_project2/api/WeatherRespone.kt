package com.example.a221003_ng_nelson_project2.api

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("current_weather")
    val current: CurrentWeather
)

data class CurrentWeather(
    @SerializedName("temperature")
    val temperature_2m: Double,
    val windspeed: Double,
    val weathercode: Int
)