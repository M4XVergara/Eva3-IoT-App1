package com.example.eva3.data.remote

import com.example.eva3.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface IotApi {

    // --- AUTENTICACIÓN (Esto faltaba y causaba el error "Unresolved reference: login") ---
    @POST("login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    // --- SENSORES ---
    @GET("sensores")
    suspend fun getSensores(): List<SensorDto>

    @POST("sensores")
    suspend fun addSensor(@Body sensor: SensorDto): SensorDto

    @PUT("sensores/{id}")
    suspend fun updateSensorState(@Path("id") id: Int, @Body sensor: SensorDto): SensorDto

    // --- BARRERA ---
    @POST("control-barrera")
    suspend fun controlBarrier(@Body request: BarrierRequest): BarrierStateDto

    @GET("estado-barrera")
    suspend fun getBarrierState(): BarrierStateDto

    // --- HISTORIAL (Esto faltaba y causaba error "Unresolved reference: getHistorial") ---
    @GET("eventos")
    suspend fun getHistorial(): List<EventoDto>
}