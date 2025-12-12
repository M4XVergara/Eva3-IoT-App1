package com.example.eva3.data.remote

import com.example.eva3.data.remote.dto.BarrierRequest
import com.example.eva3.data.remote.dto.BarrierStateDto
import com.example.eva3.data.remote.dto.SensorDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface IotApi {
    // --- GESTIÓN DE SENSORES ---
    // Obtener lista de sensores registrados [cite: 206, 270]
    @GET("api/sensores")
    suspend fun getSensores(): List<SensorDto>

    // Agregar un nuevo sensor (Requisito Admin) [cite: 192]
    @POST("api/sensores")
    suspend fun addSensor(@Body sensor: SensorDto): SensorDto

    // Cambiar estado de sensor (Activar/Desactivar/Bloquear) [cite: 205-206]
    @PUT("api/sensores/{id}")
    suspend fun updateSensorState(@Path("id") id: Int, @Body sensor: SensorDto): SensorDto

    // --- CONTROL DE BARRERA ---
    // Enviar orden de abrir/cerrar [cite: 211]
    @POST("api/barrera/control")
    suspend fun controlBarrier(@Body request: BarrierRequest): BarrierStateDto

    // Consultar si está abierta o cerrada [cite: 212]
    @GET("api/barrera/estado")
    suspend fun getBarrierState(): BarrierStateDto
}