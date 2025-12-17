package com.example.eva3.screens.iot

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
// Importamos individualmente para evitar conflictos
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun IotControlScreen(viewModel: IotViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val esAdmin = viewModel.usuarioLogueado?.rol == "ADMIN"

    // CONFIGURACIÓN DE PESTAÑAS (Ahora son 4 para Admin)
    val titulos = if (esAdmin) {
        listOf("Control", "Sensores", "Usuarios", "Historial")
    } else {
        listOf("Control", "Historial")
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                titulos.forEachIndexed { index, titulo ->
                    NavigationBarItem(
                        icon = {
                            if (esAdmin) {
                                when (index) {
                                    0 -> Icon(Icons.Default.Home, null)
                                    1 -> Icon(Icons.Default.Settings, null)
                                    2 -> Icon(Icons.Default.Person, null) // Icono Usuarios
                                    else -> Icon(Icons.Default.List, null)
                                }
                            } else {
                                when (index) {
                                    0 -> Icon(Icons.Default.Home, null)
                                    else -> Icon(Icons.Default.List, null)
                                }
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

            // --- ENCABEZADO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Hola, ${viewModel.usuarioLogueado?.nombre ?: "Usuario"}", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small) {
                            Text(text = viewModel.usuarioLogueado?.rol ?: "Rol?", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        val depto = viewModel.usuarioLogueado?.numero_depto
                        if (depto != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "📍 Depto $depto", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                }
                IconButton(onClick = { viewModel.cerrarSesion() }) {
                    Icon(Icons.Default.ExitToApp, "Salir", tint = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            // --- CONTENIDO DE PESTAÑAS ---
            Box(modifier = Modifier.weight(1f)) {
                if (esAdmin) {
                    when (selectedTab) {
                        0 -> SectionControlBarrera(viewModel)
                        1 -> SectionGestionSensores(viewModel)
                        2 -> SectionGestionUsuarios(viewModel) // <--- PANTALLA DE VECINOS
                        3 -> SectionHistorial(viewModel)
                    }
                } else {
                    when (selectedTab) {
                        0 -> SectionControlBarrera(viewModel)
                        1 -> SectionHistorial(viewModel)
                    }
                }
            }
        }
    }
}

// --- SECCIÓN 1: BARRERA ---
@Composable
fun SectionControlBarrera(viewModel: IotViewModel) {
    val textoEstado = if (viewModel.isBarrierOpen) "ABIERTA" else "CERRADA"
    val colorEstado = if (viewModel.isBarrierOpen) Color.Green else Color.Red
    val icono = if (viewModel.isBarrierOpen) Icons.Default.CheckCircle else Icons.Default.Lock

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(100.dp), tint = colorEstado)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "La barrera está:", style = MaterialTheme.typography.bodyLarge)
        Text(text = textoEstado, style = MaterialTheme.typography.headlineLarge, color = colorEstado)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { viewModel.toggleBarrier() },
            colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.isBarrierOpen) Color.Red else Color(0xFF006400)),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(if (viewModel.isBarrierOpen) "CERRAR BARRERA" else "ABRIR BARRERA")
        }
    }
}

// --- SECCIÓN 2: SENSORES ---
@Composable
fun SectionGestionSensores(viewModel: IotViewModel) {
    var nuevoCodigo by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Registrar Nuevo Sensor", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = nuevoCodigo, onValueChange = { nuevoCodigo = it }, modifier = Modifier.weight(1f), label = { Text("MAC / UID") }, singleLine = true)
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { if (nuevoCodigo.isNotEmpty()) { viewModel.agregarSensor(nuevoCodigo, "Tarjeta"); nuevoCodigo = "" } }, modifier = Modifier.height(56.dp)) {
                Icon(Icons.Default.Add, null)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Sensores Registrados:", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(viewModel.sensores) { sensor ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = if (sensor.estado == "ACTIVO") Color(0xFFE8F5E9) else Color(0xFFFFEBEE))) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Sensor: ${sensor.codigo_sensor}", style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Tipo: ${sensor.tipo}", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = sensor.estado == "ACTIVO", onCheckedChange = { viewModel.cambiarEstadoSensor(sensor) })
                    }
                }
            }
        }
    }
}

// --- SECCIÓN 3: GESTIÓN DE USUARIOS (NUEVA) ---
@Composable
fun SectionGestionUsuarios(viewModel: IotViewModel) {
    // Cargar vecinos al entrar
    LaunchedEffect(Unit) { viewModel.cargarVecinos() }

    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoEmail by remember { mutableStateOf("") }
    var nuevoPass by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Agregar Vecino (Operador)", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nuevoEmail, onValueChange = { nuevoEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nuevoPass, onValueChange = { nuevoPass = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (nuevoEmail.isNotEmpty() && nuevoPass.isNotEmpty()) {
                    viewModel.crearVecino(nuevoNombre, nuevoEmail, nuevoPass)
                    nuevoNombre = ""; nuevoEmail = ""; nuevoPass = ""
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("REGISTRAR VECINO")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Lista de Vecinos:", style = MaterialTheme.typography.titleMedium)

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(viewModel.vecinos) { vecino ->
                val esBloqueado = vecino.rol == "BLOQUEADO"
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = if (esBloqueado) Color(0xFFFFEBEE) else Color(0xFFE3F2FD))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = vecino.nombre ?: "Sin nombre", style = MaterialTheme.typography.titleSmall)
                            Text(text = vecino.email ?: "", style = MaterialTheme.typography.bodySmall)
                            Text(text = if(esBloqueado) "BLOQUEADO" else "HABILITADO", color = if(esBloqueado) Color.Red else Color.Green, style = MaterialTheme.typography.labelSmall)
                        }
                        // Botón para bloquear/desbloquear (Usamos CheckCircle para desbloqueado para evitar errores de iconos faltantes)
                        IconButton(onClick = { viewModel.bloquearVecino(vecino) }) {
                            Icon(
                                imageVector = if (esBloqueado) Icons.Default.Lock else Icons.Default.CheckCircle,
                                contentDescription = "Bloquear",
                                tint = if (esBloqueado) Color.Red else Color.Green
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SECCIÓN 4: HISTORIAL ---
@Composable
fun SectionHistorial(viewModel: IotViewModel) {
    LaunchedEffect(Unit) { viewModel.refrescarHistorial() }
    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = { viewModel.refrescarHistorial() }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Icon(Icons.Default.Refresh, null); Spacer(modifier = Modifier.width(8.dp)); Text("ACTUALIZAR EVENTOS")
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(viewModel.historial) { evento ->
                ListItem(
                    headlineContent = { Text(evento.tipo) },
                    supportingContent = { Text("${evento.detalle}\n${evento.fecha}") },
                    leadingContent = { Icon(if (evento.tipo == "ACCESO") Icons.Default.CheckCircle else Icons.Default.Info, null, tint = if (evento.tipo == "ACCESO") Color.Green else Color.Gray) }
                )
                HorizontalDivider()
            }
        }
    }
}