package com.example.eva3.screens.iot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eva3.data.remote.HttpClient
import com.example.eva3.data.remote.IotRepository
import com.example.eva3.data.remote.dto.SensorDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IotViewModel : ViewModel() {

    // Conectamos con el repositorio que creaste antes
    private val repository = IotRepository(HttpClient.iotApi)

    // Estado privado (mutable) y público (inmutable)
    private val _uiState = MutableStateFlow(IotUiState(isLoading = true))
    val uiState: StateFlow<IotUiState> = _uiState

    init {
        // Al iniciar, comenzamos el ciclo de actualización automática
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                loadData()
                delay(2000) // Recarga datos cada 2 segundos (igual que Guía C)
            }
        }
    }

    // Función principal para cargar datos de la API
    private suspend fun loadData() {
        try {
            // Pedimos estado de barrera y lista de sensores al mismo tiempo
            val barrierState = repository.getBarrierState()
            val sensores = repository.getSensores()

            // Actualizamos la pantalla con datos nuevos y quitamos el loading
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = null,
                isBarrierOpen = barrierState.isOpen,
                sensorList = sensores
            )
        } catch (e: Exception) {
            // Si falla, mostramos el error pero mantenemos los datos viejos si existen
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Error de conexión"
            )
        }
    }

    // Acción 1: Usuario presiona botón "Abrir/Cerrar Barrera"
    fun toggleBarrier(shouldOpen: Boolean) {
        viewModelScope.launch {
            try {
                // Enviamos la orden al backend
                repository.controlBarrier(shouldOpen)
                // Actualizamos la UI inmediatamente para que se sienta rápido
                _uiState.value = _uiState.value.copy(isBarrierOpen = shouldOpen)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error moviendo barrera")
            }
        }
    }

    // Acción 2: Usuario (Admin) activa/desactiva un sensor
    fun toggleSensorState(sensor: SensorDto, isActive: Boolean) {
        viewModelScope.launch {
            try {
                // Definimos el nuevo estado en texto, según pide tu backend
                val newStateString = if (isActive) "ACTIVO" else "INACTIVO"

                // Llamamos a la API
                repository.updateSensorState(sensor.id, newStateString)

                // Nota: No actualizamos _uiState aquí manualmente porque
                // el "startAutoRefresh" traerá el dato actualizado en menos de 2 seg.
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error actualizando sensor")
            }
        }
    }
}