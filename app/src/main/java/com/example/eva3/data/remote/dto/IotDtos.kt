package com.example.eva3.data.remote.dto

// 1. DTOs para Login (Esto es lo que le faltaba a tu código)
data class LoginRequestDto(
    val email: String,
    val pass: String
)

data class LoginResponseDto(
    val status: String,      // "ok" o "error"
    val usuario: UsuarioDto?,
    val message: String? = null// null si falla
)

data class UsuarioDto(
    val id: Int,
    val nombre: String?,
    val email: String?,
    val rol: String?,
    val numero_depto: String?,
    val departamento_id: Int?
)
// DTO para crear nuevo usuario
data class UsuarioRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val departamento_id: Int
)

// DTO para cambiar estado
data class EstadoUsuarioRequest(
    val nuevoRol: String
)

// 2. Sensor
data class SensorDto(
    val id: Int = 0,
    val codigo_sensor: String,
    val estado: String,
    val tipo: String,
    val departamento_id: Int? = null,
    val usuario_id: Int? = null,      // <--- NUEVO
    val nombre_dueno: String? = null
)

// 3. Barrera
// ¡AQUÍ ESTÁ LA SOLUCIÓN DEL ERROR DE LÍNEA 65!
// Antes tenías "action", ahora debe ser "accion" para que coincida con el Repositorio
data class BarrierRequest(
    val accion: String
)

data class BarrierStateDto(
    val isOpen: Boolean
)

// 4. Historial (Soluciona el error de getHistorial)
data class EventoDto(
    val id: Int,
    val fecha: String,
    val tipo: String,
    val detalle: String
)

data class ValidacionRequest(val mac: String)
data class ValidacionResponse(val acceso: Boolean, val mensaje: String?)