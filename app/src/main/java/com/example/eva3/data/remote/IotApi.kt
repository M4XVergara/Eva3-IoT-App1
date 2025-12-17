package com.example.eva3.data.remote

import com.example.eva3.data.remote.dto.*
import retrofit2.Response // <--- ESTE ES EL IMPORT QUE ARREGLA EL ERROR
import retrofit2.http.*

interface IotApi {

    // --- 1. AUTENTICACIÓN ---
    @POST("login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    // --- 2. SENSORES ---
    @GET("sensores")
    suspend fun getSensores(): List<SensorDto>

    @POST("sensores")
    suspend fun addSensor(@Body sensor: SensorDto): SensorDto

    // Este sirve tanto para cambiar estado como para editar datos (PUT)
    @PUT("sensores/{id}")
    suspend fun updateSensorState(@Path("id") id: Int, @Body sensor: SensorDto): SensorDto

    // Esta es la función que daba error (ahora funcionará con el import correcto)
    @DELETE("sensores/{id}")
    suspend fun deleteSensor(@Path("id") id: Int): Response<Unit>

    // --- 3. BARRERA ---
    @POST("barrera")
    suspend fun controlBarrier(@Body request: BarrierRequest): BarrierStateDto

    @GET("barrera")
    suspend fun getBarrierState(): BarrierStateDto

    // --- 4. HISTORIAL ---
    @GET("eventos") // Asegúrate que tu endpoint en Node.js se llame así (o "historial")
    suspend fun getHistorial(): List<EventoDto>

    // --- 5. USUARIOS (VECINOS) ---
    @GET("usuarios/{deptoId}")
    suspend fun getUsuarios(@Path("deptoId") deptoId: Int): List<UsuarioDto>

    @POST("usuarios")
    suspend fun addUsuario(@Body request: UsuarioRequest): LoginResponseDto

    @PUT("usuarios/{id}/estado")
    suspend fun updateUsuarioEstado(@Path("id") id: Int, @Body request: EstadoUsuarioRequest): LoginResponseDto

    // Simular uso de llavero
    @POST("validar-rfid")
    suspend fun validarRfid(@Body request: ValidacionRequest): ValidacionResponse
}