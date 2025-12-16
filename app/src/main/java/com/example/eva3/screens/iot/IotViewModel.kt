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

    // --- ESTADOS DE LA PANTALLA (Lo que ve el usuario) ---

    // Estado de la Barrera (true = Abierta)
    var isBarrierOpen by mutableStateOf(false)
        private set

    // Lista de Sensores (Para la pantalla de Gestión)
    var sensores by mutableStateOf<List<SensorDto>>(emptyList())
        private set

    // Historial de Eventos (Para la pantalla Home)
    var historial by mutableStateOf<List<EventoDto>>(emptyList())
        private set

    // Usuario Logueado (Si es null, mostramos LoginScreen)
    var usuarioLogueado by mutableStateOf<UsuarioDto?>(null)
        private set

    // Mensajes de error o éxito (Para mostrar Toasts o Snackbars)
    var mensajeUsuario by mutableStateOf<String?>(null)
        private set

    // --- FUNCIONES QUE LLAMA LA PANTALLA ---

    // 1. LOGIN
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val resultado = repository.login(email, pass)
            if (resultado.isSuccess) {
                usuarioLogueado = resultado.getOrNull()
                // Al entrar, cargamos los datos iniciales
                cargarDatosIniciales()
            } else {
                mensajeUsuario = "Error: Credenciales incorrectas"
            }
        }
    }

    // 2. CONTROL BARRERA
    fun toggleBarrier() {
        viewModelScope.launch {
            // Si está abierta, mandamos cerrar (false), y viceversa
            val nuevaOrden = !isBarrierOpen
            val resultado = repository.controlBarrier(nuevaOrden)

            if (resultado.isSuccess) {
                isBarrierOpen = resultado.getOrDefault(false)
                refrescarHistorial() // Actualizamos historial para ver el evento
            } else {
                mensajeUsuario = "Error de conexión con la Barrera"
            }
        }
    }

    // 3. GESTIÓN DE SENSORES
    fun cargarSensores() {
        viewModelScope.launch {
            val res = repository.getSensores()
            if (res.isSuccess) sensores = res.getOrDefault(emptyList())
        }
    }

    fun agregarSensor(codigo: String, tipo: String) {
        viewModelScope.launch {
            val res = repository.addSensor(codigo, tipo)
            if (res.isSuccess) {
                mensajeUsuario = "Sensor agregado correctamente"
                cargarSensores() // Recargar lista
            } else {
                mensajeUsuario = "Error al agregar sensor"
            }
        }
    }

    fun cambiarEstadoSensor(sensor: SensorDto) {
        viewModelScope.launch {
            val res = repository.toggleSensorState(sensor)
            if (res.isSuccess) cargarSensores() // Recargar lista para ver el cambio
        }
    }

    // 4. HISTORIAL
    fun refrescarHistorial() {
        viewModelScope.launch {
            val res = repository.getHistorial()
            if (res.isSuccess) {
                historial = res.getOrDefault(emptyList())
            }
        }
    }

    // Función auxiliar para cargar todo al inicio
    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            // Obtenemos estado actual de la barrera
            val barreraRes = repository.getBarrierState()
            if (barreraRes.isSuccess) isBarrierOpen = barreraRes.getOrDefault(false)

            // Cargamos listas
            cargarSensores()
            refrescarHistorial()
        }
    }

    // Limpiar mensaje después de mostrarlo
    fun limpiarMensaje() {
        mensajeUsuario = null
    }
}