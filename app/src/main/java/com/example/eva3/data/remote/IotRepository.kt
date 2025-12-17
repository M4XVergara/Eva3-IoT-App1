package com.example.eva3.data.remote

import com.example.eva3.data.remote.dto.*

// LA CLAVE: Aquí debe decir "class IotRepository"
class IotRepository(private val api: IotApi) {

    // --- 1. AUTENTICACIÓN (LOGIN) MEJORADO ---
    suspend fun login(email: String, pass: String): Result<UsuarioDto> {
        return try {
            val request = LoginRequestDto(email, pass)
            val response = api.login(request)

            if (response.status == "ok" && response.usuario != null) {
                Result.success(response.usuario)
            } else {
                // CAMBIO: Ahora usamos el mensaje que viene del servidor (o uno por defecto)
                val errorMsg = response.message ?: "Error de acceso desconocido"
                Result.failure(Exception(errorMsg))
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

    // ACTUALIZADO: Recibe deptoId y usuarioId (puede ser null)
    suspend fun addSensor(codigo: String, tipo: String, deptoId: Int, usuarioId: Int?): Result<SensorDto> {
        return try {
            val sensor = SensorDto(
                id = 0,
                codigo_sensor = codigo,
                tipo = tipo,
                estado = "ACTIVO",
                departamento_id = deptoId,
                usuario_id = usuarioId // <--- Enviamos el dueño al servidor
            )
            val response = api.addSensor(sensor)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- BORRAR SENSOR ---
    suspend fun deleteSensor(id: Int): Result<Boolean> {
        return try {
            api.deleteSensor(id)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- EDITAR SENSOR (Renombrado para ser general) ---
    suspend fun updateSensor(sensor: SensorDto): Result<SensorDto> {
        return try {
            // Enviamos el objeto completo con los cambios (dueño, codigo, estado)
            val response = api.updateSensorState(sensor.id, sensor)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- 3. CONTROL DE BARRERA ---
    suspend fun controlBarrier(abrir: Boolean): Result<Boolean> {
        return try {
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

    // --- 5. GESTIÓN USUARIOS (VECINOS) ---
    suspend fun getUsuarios(deptoId: Int): Result<List<UsuarioDto>> {
        return try {
            val res = api.getUsuarios(deptoId)
            Result.success(res)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun addUsuario(nombre: String, email: String, pass: String, deptoId: Int): Result<Boolean> {
        return try {
            val req = UsuarioRequest(nombre, email, pass, deptoId)
            api.addUsuario(req)
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun toggleBloqueoUsuario(usuario: UsuarioDto): Result<Boolean> {
        return try {
            val nuevoRol = if (usuario.rol == "BLOQUEADO") "OPERADOR" else "BLOQUEADO"
            api.updateUsuarioEstado(usuario.id, EstadoUsuarioRequest(nuevoRol))
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun usarLlaveroDigital(codigo: String): Result<Boolean> {
        return try {
            val response = api.validarRfid(ValidacionRequest(codigo))
            // Si acceso es true, funcionó
            Result.success(response.acceso)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}