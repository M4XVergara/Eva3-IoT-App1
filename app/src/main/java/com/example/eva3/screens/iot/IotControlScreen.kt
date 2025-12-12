package com.example.eva3.screens.iot

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Si esto sale en rojo, avísame (falta una librería pequeña)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IotControlScreen(
    viewModel: IotViewModel = viewModel() // Conectamos la pantalla con el ViewModel
) {
    // 1. Observamos el estado. Cada vez que el ViewModel cambie algo, 'uiState' se actualizará aquí.
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Control de Acceso IoT") })
        }
    ) { paddingValues ->

        // Contenedor principal
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // 2. Si hay error, lo mostramos
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 3. Indicador de carga
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- TARJETA DE CONTROL DE BARRERA ---
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Control de Barrera", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Indicador visual del estado
                    Text(
                        text = if (uiState.isBarrierOpen) "ABIERTA" else "CERRADA",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isBarrierOpen) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { viewModel.toggleBarrier(true) }, // Abrir
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("ABRIR")
                        }

                        Button(
                            onClick = { viewModel.toggleBarrier(false) }, // Cerrar
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                        ) {
                            Text("CERRAR")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- LISTA DE SENSORES ---
            Text("Sensores Registrados", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(uiState.sensorList) { sensor ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "ID: ${sensor.codigo_sensor}", fontWeight = FontWeight.Bold)
                                Text(text = "Tipo: ${sensor.tipo}", style = MaterialTheme.typography.bodySmall)
                            }

                            // Estado del sensor
                            AssistChip(
                                onClick = {
                                    // Al hacer clic, cambiamos el estado (Lógica simple: si es ACTIVO pasa a INACTIVO)
                                    val nuevoEstado = sensor.estado != "ACTIVO"
                                    viewModel.toggleSensorState(sensor, nuevoEstado)
                                },
                                label = { Text(sensor.estado) },
                                leadingIcon = {
                                    if (sensor.estado == "ACTIVO")
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Green)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}