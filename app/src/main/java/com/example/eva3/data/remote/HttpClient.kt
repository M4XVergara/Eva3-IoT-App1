package com.example.eva3.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// CORRECCIÓN: El nombre del objeto es solo "HttpClient", sin .kt ni comillas
object HttpClient {
    // IMPORTANTE: Cambia "192.168.1.X" por la IP real de tu computador
    private const val BASE_URL = "http://192.168.1.X:3000/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val iotApi: IotApi = retrofit.create(IotApi::class.java)
}