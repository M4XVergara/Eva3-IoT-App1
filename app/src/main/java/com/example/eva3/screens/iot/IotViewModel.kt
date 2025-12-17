package com.example.eva3.screens.iot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eva3.data.remote.IotRepository
import com.example.eva3.data.remote.dto.EventoDto
import com.example.eva3.data.remote.dto.SensorDto
import com.example.eva3.data.remote.dto.UsuarioDto
import kotlinx.coroutines.launch

class IotViewModel(private val repository: IotRepository) : ViewModel() {

    // --- ESTADOS DE LA PANTALLA ---
    var isBarrierOpen by mutableStateOf(false)
        private set

    var sensores by mutableStateOf<List<SensorDto>>(emptyList())
        private set

    var historial by mutableStateOf<List<EventoDto>>(emptyList())
        private set

    var usuarioLogueado by mutableStateOf<UsuarioDto?>(null)
        private set

    var mensajeUsuario by mutableStateOf<String?>(null)
        private set

    // Lista de Vecinos (Para el dropdown del Admin)
    var vecinos by mutableStateOf<List<UsuarioDto>>(emptyList())
        private set

    // --- 1. LOGIN ---
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val resultado = repository.login(email, pass)
            if (resultado.isSuccess) {
                usuarioLogueado = resultado.getOrNull()
                cargarDatosIniciales()
            } else {
                mensajeUsuario = resultado.exceptionOrNull()?.message ?: "Error de conexión"
            }
        }
    }

    // --- 2. CONTROL BARRERA ---
    fun toggleBarrier() {
        viewModelScope.launch {
            val nuevaOrden = !isBarrierOpen
            val resultado = repository.controlBarrier(nuevaOrden)

            if (resultado.isSuccess) {
                isBarrierOpen = resultado.getOrDefault(false)
                refrescarHistorial()
            } else {
                mensajeUsuario = "Error de conexión con la Barrera"
            }
        }
    }

    // --- 3. GESTIÓN DE SENSORES ---
    fun cargarSensores() {
        viewModelScope.launch {
            val res = repository.getSensores()
            if (res.isSuccess) sensores = res.getOrDefault(emptyList())
        }
    }

    fun agregarSensor(codigo: String, tipo: String, usuarioId: Int?) {
        viewModelScope.launch {
            val miDeptoId = usuarioLogueado?.departamento_id
            if (miDeptoId != null) {
                val res = repository.addSensor(codigo, tipo, miDeptoId, usuarioId)
                if (res.isSuccess) {
                    mensajeUsuario = "Sensor agregado correctamente"
                    cargarSensores()
                } else {
                    mensajeUsuario = "Error al agregar sensor"
                }
            } else {
                mensajeUsuario = "Error: No tienes departamento asignado"
            }
        }
    }

    // FUNCIÓN UNIFICADA: Sirve para Editar Texto, Asignar Dueño Y Cambiar Estado (Switch)
    fun editarSensor(sensorEditado: SensorDto) {
        viewModelScope.launch {
            // Llamamos a 'updateSensor' en el repositorio (antes se llamaba toggleSensorState)
            val res = repository.updateSensor(sensorEditado)
            if (res.isSuccess) {
                // No mostramos mensaje invasivo para el switch, solo recargamos
                cargarSensores()
            } else {
                mensajeUsuario = "Error al actualizar sensor"
            }
        }
    }

    fun borrarSensor(sensor: SensorDto) {
        viewModelScope.launch {
            val res = repository.deleteSensor(sensor.id)
            if (res.isSuccess) {
                mensajeUsuario = "Sensor eliminado"
                cargarSensores()
            } else {
                mensajeUsuario = "Error al eliminar"
            }
        }
    }

    // --- 4. HISTORIAL ---
    fun refrescarHistorial() {
        viewModelScope.launch {
            val res = repository.getHistorial()
            if (res.isSuccess) {
                historial = res.getOrDefault(emptyList())
            }
        }
    }

    // --- 5. GESTIÓN VECINOS ---
    fun cargarVecinos() {
        viewModelScope.launch {
            val deptoId = usuarioLogueado?.departamento_id
            if (deptoId != null) {
                val res = repository.getUsuarios(deptoId)
                if (res.isSuccess) {
                    vecinos = res.getOrDefault(emptyList()).filter { it.id != usuarioLogueado?.id }
                }
            }
        }
    }

    fun crearVecino(nombre: String, email: String, pass: String) {
        viewModelScope.launch {
            val deptoId = usuarioLogueado?.departamento_id
            if (deptoId != null) {
                val res = repository.addUsuario(nombre, email, pass, deptoId)
                if (res.isSuccess) {
                    mensajeUsuario = "Vecino agregado exitosamente"
                    cargarVecinos()
                } else {
                    mensajeUsuario = "Error al crear vecino"
                }
            }
        }
    }

    fun bloquearVecino(vecino: UsuarioDto) {
        viewModelScope.launch {
            repository.toggleBloqueoUsuario(vecino)
            cargarVecinos()
        }
    }

    // --- UTILIDADES ---
    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            val barreraRes = repository.getBarrierState()
            if (barreraRes.isSuccess) isBarrierOpen = barreraRes.getOrDefault(false)
            cargarSensores()
            refrescarHistorial()
        }
    }

    fun limpiarMensaje() {
        mensajeUsuario = null
    }

    fun cerrarSesion() {
        usuarioLogueado = null
        sensores = emptyList()
        historial = emptyList()
        vecinos = emptyList()
        isBarrierOpen = false
        mensajeUsuario = null
    }

    // --- LLAVERO DIGITAL ---
    fun usarLlavero(sensor: SensorDto) {
        viewModelScope.launch {
            val res = repository.usarLlaveroDigital(sensor.codigo_sensor)
            if (res.isSuccess && res.getOrDefault(false)) {
                mensajeUsuario = "🔓 ¡Acceso concedido con Llavero Digital!"
                isBarrierOpen = true // Visualmente abrimos la barrera en la UI
                refrescarHistorial() // Para que aparezca el evento
            } else {
                mensajeUsuario = "⛔ Acceso denegado o sensor inactivo"
            }
        }
    }
    // --- NUEVO: Función para consultar estado silenciosamente ---
    fun consultarEstadoBarrera() {
        viewModelScope.launch {
            val resultado = repository.getBarrierState()
            if (resultado.isSuccess) {
                // Actualizamos la variable que controla el color de la UI
                isBarrierOpen = resultado.getOrDefault(false)
            }
        }
    }
}