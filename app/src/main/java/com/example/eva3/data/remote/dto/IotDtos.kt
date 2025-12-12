package com.example.eva3.data.remote.dto

// DTO para representar un Sensor (Tarjeta o Llavero)
// Basado en la estructura de datos sugerida en la Sumativa (pag 5)
data class SensorDto(
    val id: Int,
    val codigo_sensor: String, // UID o MAC de la tarjeta [cite: 271]
    val estado: String,        // "ACTIVO", "INACTIVO", "BLOQUEADO" [cite: 272]
    val tipo: String           // "Tarjeta" o "Llavero" [cite: 274]
)

// DTO para enviar comandos a la barrera (Abrir/Cerrar)
// Requisito: Control manual desde la App [cite: 208-209]
data class BarrierRequest(
    val action: String // Enviamos "ABRIR" o "CERRAR"
)

// DTO para recibir el estado actual de la barrera desde la API
data class BarrierStateDto(
    val isOpen: Boolean // true = Abierta, false = Cerrada
)