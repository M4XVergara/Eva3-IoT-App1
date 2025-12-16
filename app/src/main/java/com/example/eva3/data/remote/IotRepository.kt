package com.example.eva3.data.remote

import com.example.eva3.data.remote.dto.*

class IotRepository(private val api: IotApi) {

    // --- 1. AUTENTICACIÓN (LOGIN) ---
    // Esta función conecta la pantalla de Login con la API
    suspend fun login(email: String, pass: String): Result<UsuarioDto> {
        return try {
            val request = LoginRequestDto(email, pass)
            val response = api.login(request)

            if (response.status == "ok" && response.usuario != null) {
                Result.success(response.usuario)
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- 2. GESTIÓN DE SENSORES ---
    suspend fun getSensores(): Result<List<SensorDto>> {
        return try {
            val response = api.getSensores()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addSensor(codigo: String, tipo: String): Result<SensorDto> {
        return try {
            // El ID se autogenera en BD, enviamos 0. Estado inicial ACTIVO.
            val sensor = SensorDto(id = 0, codigo_sensor = codigo, tipo = tipo, estado = "ACTIVO")
            val response = api.addSensor(sensor)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleSensorState(sensor: SensorDto): Result<SensorDto> {
        return try {
            // Invertimos el estado: Si es ACTIVO pasa a INACTIVO, y viceversa
            val nuevoEstado = if (sensor.estado == "ACTIVO") "INACTIVO" else "ACTIVO"
            val sensorActualizado = sensor.copy(estado = nuevoEstado)

            // Llamamos a la API con el ID y el nuevo dato
            val response = api.updateSensorState(sensor.id, sensorActualizado)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- 3. CONTROL DE BARRERA ---
    suspend fun controlBarrier(abrir: Boolean): Result<Boolean> {
        return try {
            // AQUÍ ESTÁ LA CORRECCIÓN CLAVE:
            // Usamos "accion" ("ABRIR"/"CERRAR") para que Node.js entienda
            val comando = if (abrir) "ABRIR" else "CERRAR"
            val request = BarrierRequest(accion = comando)

            val response = api.controlBarrier(request)
            Result.success(response.isOpen)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBarrierState(): Result<Boolean> {
        return try {
            val response = api.getBarrierState()
            Result.success(response.isOpen)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- 4. HISTORIAL ---
    suspend fun getHistorial(): Result<List<EventoDto>> {
        return try {
            val response = api.getHistorial()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}