package com.example.a221003_ng_nelson_project2.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val api: WeatherApi by lazy {

        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(WeatherApi::class.java)
    }
}