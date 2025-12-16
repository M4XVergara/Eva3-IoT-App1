package com.example.eva3.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object HttpClient {
    // CAMBIO CRÍTICO: Usamos la IP de AWS que me acabas de dar
    // Nota: Si reinician el servidor y la IP cambia, solo debes actualizar este número aquí.
    private const val BASE_URL = "http://98.91.42.250:3000/"

    val iotApi: IotApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IotApi::class.java)
    }
}