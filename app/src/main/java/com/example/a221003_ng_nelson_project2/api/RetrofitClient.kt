package com.example.a221003_ng_nelson_project2.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://api.open-meteo.com/"

    val api: WeatherApi by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(WeatherApi::class.java)
    }
}