package com.example.a221003_ng_nelson_project2.api

class WeatherRepository(private val api: WeatherApi) {

    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return api.getWeather(lat, lon)
    }
}