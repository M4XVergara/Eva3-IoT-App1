package com.example.eva3.data.remote

import com.example.eva3.data.remote.dto.BarrierRequest
import com.example.eva3.data.remote.dto.BarrierStateDto
import com.example.eva3.data.remote.dto.SensorDto

class IotRepository(private val api: IotApi) {

    // Función para traer todos los sensores
    suspend fun getSensores(): List<SensorDto> {
        return api.getSensores()
    }

    // Función para cambiar estado de un sensor (Activo/Inactivo)
    suspend fun updateSensorState(id: Int, estado: String): SensorDto {
        // Reutilizamos el objeto SensorDto solo enviando lo necesario, o el objeto completo según tu backend
        // Aquí asumo que envías el objeto modificado.
        // Para simplificar, creamos un objeto dummy con el nuevo estado si el backend solo mira eso,
        // o idealmente deberías recibir el objeto completo como parámetro.
        // Ajuste para la sumativa:
        val sensorUpdate = SensorDto(id, "", estado, "") // Backend debería ignorar campos vacíos si solo actualiza estado
        return api.updateSensorState(id, sensorUpdate)
    }

    // --- FUNCIONES BARRERA ---

    // Consultar estado barrera
    suspend fun getBarrierState(): BarrierStateDto {
        return api.getBarrierState()
    }

    // Mover barrera (Abrir/Cerrar)
    suspend fun controlBarrier(abrir: Boolean): BarrierStateDto {
        val action = if (abrir) "ABRIR" else "CERRAR"
        val request = BarrierRequest(action)
        return api.controlBarrier(request)
    }
}