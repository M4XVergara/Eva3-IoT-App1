package com.example.eva3.screens.iot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
// Importamos cada ícono explícitamente para evitar errores
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CheckCircle // Usamos este en vez de LockOpen
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.* // --- ¡ESTOS DOS IMPORTS SON VITALES PARA EL 'BY REMEMBER'! ---
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
// -------------------------------------------------------------
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun IotControlScreen(viewModel: IotViewModel) {
    // CORRECCIÓN 1: Usamos 'mutableIntStateOf' (nombre correcto)
    var selectedTab by remember { mutableIntStateOf(0) }
    val titulos = listOf("Control", "Sensores", "Historial")

    Scaffold(
        bottomBar = {
            NavigationBar {
                titulos.forEachIndexed { index, titulo ->
                    NavigationBarItem(
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.Home, null)
                                1 -> Icon(Icons.Default.Settings, null)
                                else -> Icon(Icons.Default.List, null)
                            }
                        },
                        label = { Text(titulo) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text(
                text = "Panel: ${viewModel.usuarioLogueado?.nombre ?: "Usuario"}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> SectionControlBarrera(viewModel)
                1 -> SectionGestionSensores(viewModel)
                2 -> SectionHistorial(viewModel)
            }
        }
    }
}

@Composable
fun SectionControlBarrera(viewModel: IotViewModel) {
    val estadoTexto = if (viewModel.isBarrierOpen) "ABIERTA" else "CERRADA"
    val colorEstado = if (viewModel.isBarrierOpen) Color.Green else Color.Red

    // CORRECCIÓN 2: Cambiamos LockOpen por CheckCircle para asegurar que compile sin librerías extra
    val iconoEstado = if (viewModel.isBarrierOpen) Icons.Default.CheckCircle else Icons.Default.Lock

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = iconoEstado,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = colorEstado
        )
        Text(text = "Estado: $estadoTexto", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.toggleBarrier() },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (viewModel.isBarrierOpen) Color.Red else Color(0xFF006400)
            )
        ) {
            Text(if (viewModel.isBarrierOpen) "CERRAR BARRERA" else "ABRIR BARRERA")
        }
    }
}

@Composable
fun SectionGestionSensores(viewModel: IotViewModel) {
    var nuevoCodigo by remember { mutableStateOf("") }

    Column {
        Text("Agregar Nuevo Sensor:")
        Row {
            OutlinedTextField(
                value = nuevoCodigo,
                onValueChange = { nuevoCodigo = it },
                modifier = Modifier.weight(1f),
                label = { Text("Código RFID") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                viewModel.agregarSensor(nuevoCodigo, "Tarjeta")
                nuevoCodigo = ""
            }) {
                Icon(Icons.Default.Add, null)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Lista de Sensores:")
        LazyColumn {
            items(viewModel.sensores) { sensor ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (sensor.estado == "ACTIVO") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "ID: ${sensor.codigo_sensor}", style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Estado: ${sensor.estado}", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = sensor.estado == "ACTIVO",
                            onCheckedChange = { viewModel.cambiarEstadoSensor(sensor) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHistorial(viewModel: IotViewModel) {
    // Recargamos historial cada vez que entramos a esta pestaña
    LaunchedEffect(Unit) { viewModel.refrescarHistorial() }

    LazyColumn {
        items(viewModel.historial) { evento ->
            ListItem(
                headlineContent = { Text(evento.tipo) },
                supportingContent = { Text(evento.detalle) },
                leadingContent = { Icon(Icons.Default.Info, null) },
                trailingContent = { Text(evento.fecha.take(10), style = MaterialTheme.typography.labelSmall) }
            )
            HorizontalDivider()
        }
    }
}