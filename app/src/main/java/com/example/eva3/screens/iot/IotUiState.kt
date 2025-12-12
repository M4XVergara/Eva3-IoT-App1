package com.example.eva3.screens.iot

import com.example.eva3.data.remote.dto.SensorDto

// Define todos los datos que la pantalla necesita "pintar"
data class IotUiState(
    val isLoading: Boolean = false,         // ¿Está cargando datos?
    val error: String? = null,              // ¿Hubo error de conexión?
    val isBarrierOpen: Boolean = false,     // Estado de la barrera (Abierta/Cerrada)
    val sensorList: List<SensorDto> = emptyList() // Lista de sensores (Tarjetas/Llaveros)
)