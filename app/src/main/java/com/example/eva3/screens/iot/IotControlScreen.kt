package com.example.eva3.screens.iot

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Importa todos los iconos necesarios
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.eva3.data.remote.dto.UsuarioDto
import com.example.eva3.data.remote.dto.SensorDto

@Composable
fun IotControlScreen(viewModel: IotViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val esAdmin = viewModel.usuarioLogueado?.rol == "ADMIN"

    // CAMBIO 1: Ahora los NO ADMIN también ven "Sensores"
    val titulos = if (esAdmin) {
        listOf("Control", "Sensores", "Usuarios", "Historial")
    } else {
        listOf("Control", "Sensores", "Historial") // <--- Agregamos Sensores aquí
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
                                    2 -> Icon(Icons.Default.Person, null)
                                    else -> Icon(Icons.Default.List, null)
                                }
                            } else {
                                // Iconos para el Vecino Normal
                                when (index) {
                                    0 -> Icon(Icons.Default.Home, null)
                                    1 -> Icon(Icons.Default.Settings, null) // Icono para Sensores
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

            // --- ERRORES ---
            val mensaje = viewModel.mensajeUsuario
            if (mensaje != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color.Blue), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = mensaje, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.limpiarMensaje() }) { Text("X", color = Color.White) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- PESTAÑAS ---
            Box(modifier = Modifier.weight(1f)) {
                if (esAdmin) {
                    when (selectedTab) {
                        0 -> SectionControlBarrera(viewModel)
                        1 -> SectionGestionSensores(viewModel)
                        2 -> SectionGestionUsuarios(viewModel)
                        3 -> SectionHistorial(viewModel)
                    }
                } else {
                    // CAMBIO 2: Lógica de navegación para Vecino
                    when (selectedTab) {
                        0 -> SectionControlBarrera(viewModel)
                        1 -> SectionGestionSensores(viewModel) // Ahora puede entrar aquí
                        2 -> SectionHistorial(viewModel)
                    }
                }
            }
        }
    }
}

// ... (SectionControlBarrera sigue igual, la omito para ahorrar espacio, pégala si la borraste) ...
@Composable
fun SectionControlBarrera(viewModel: IotViewModel) {
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.consultarEstadoBarrera() // Pregunta al servidor
            kotlinx.coroutines.delay(2000)     // Espera 2 segundos
        }
    }
    val textoEstado = if (viewModel.isBarrierOpen) "ABIERTA" else "CERRADA"
    val colorEstado = if (viewModel.isBarrierOpen) Color.Green else Color.Red
    val icono = if (viewModel.isBarrierOpen) Icons.Default.CheckCircle else Icons.Default.Lock
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(100.dp), tint = colorEstado)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "La barrera está:", style = MaterialTheme.typography.bodyLarge)
        Text(text = textoEstado, style = MaterialTheme.typography.headlineLarge, color = colorEstado)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { viewModel.toggleBarrier() }, colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.isBarrierOpen) Color.Red else Color(0xFF006400)), modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text(if (viewModel.isBarrierOpen) "CERRAR BARRERA" else "ABRIR BARRERA")
        }
    }
}

// --- SECCIÓN 2: SENSORES (INTELIGENTE) ---
@Composable
fun SectionGestionSensores(viewModel: IotViewModel) {
    LaunchedEffect(Unit) { viewModel.cargarVecinos() }

    val esAdmin = viewModel.usuarioLogueado?.rol == "ADMIN" // Detectamos rol
    val miId = viewModel.usuarioLogueado?.id

    // Estados para Admin
    var nuevoCodigo by remember { mutableStateOf("") }
    var expandedAgregar by remember { mutableStateOf(false) }
    var usuarioSeleccionadoAgregar by remember { mutableStateOf<UsuarioDto?>(null) }
    var sensorAEditar by remember { mutableStateOf<SensorDto?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Llaveros Digitales", style = MaterialTheme.typography.titleMedium)

        // CAMBIO 3: SOLO el Admin ve el formulario de agregar
        if (esAdmin) {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Registrar Nuevo Sensor (Solo Admin)", style = MaterialTheme.typography.labelSmall)
                    OutlinedTextField(
                        value = nuevoCodigo, onValueChange = { nuevoCodigo = it },
                        label = { Text("Nuevo Sensor (MAC)") }, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { expandedAgregar = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(usuarioSeleccionadoAgregar?.nombre ?: "Asignar a: Nadie (Común)")
                        }
                        DropdownMenu(expanded = expandedAgregar, onDismissRequest = { expandedAgregar = false }) {
                            DropdownMenuItem(text = { Text("Ninguno(Sin Asignar)") }, onClick = { usuarioSeleccionadoAgregar = null; expandedAgregar = false })
                            viewModel.vecinos.forEach { vecino: UsuarioDto ->
                                DropdownMenuItem(text = { Text(vecino.nombre ?: "?") }, onClick = { usuarioSeleccionadoAgregar = vecino; expandedAgregar = false })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        if (nuevoCodigo.isNotEmpty()) {
                            viewModel.agregarSensor(nuevoCodigo, "Tarjeta", usuarioSeleccionadoAgregar?.id)
                            nuevoCodigo = ""; usuarioSeleccionadoAgregar = null
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("AGREGAR SENSOR") }
                }
            }
        } else {
            // Mensaje para el vecino
            Text("Toca el botón ▶️ para abrir la barrera con tu sensor.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // CAMBIO 4: Filtramos la lista.
        // Admin ve todo. Vecino ve SOLO sus sensores o los que no tienen dueño (null).
        val listaVisible = if (esAdmin) {
            viewModel.sensores
        } else {
            viewModel.sensores.filter { it.usuario_id == miId }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(listaVisible) { sensor ->
                SensorCardItem(sensor, viewModel) { sensorParaEditar ->
                    sensorAEditar = sensorParaEditar
                }
            }
        }
    }

    if (sensorAEditar != null && esAdmin) {
        DialogEditarSensor(
            sensor = sensorAEditar!!,
            vecinos = viewModel.vecinos,
            onDismiss = { sensorAEditar = null },
            onConfirm = { sensorEditado ->
                viewModel.editarSensor(sensorEditado)
                sensorAEditar = null
            }
        )
    }
}

// Tarjeta Inteligente: Muestra botones según el rol
@Composable
fun SensorCardItem(sensor: SensorDto, viewModel: IotViewModel, onEditClick: (SensorDto) -> Unit) {
    val esAdmin = viewModel.usuarioLogueado?.rol == "ADMIN"

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (sensor.estado == "ACTIVO") Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = sensor.codigo_sensor, style = MaterialTheme.typography.titleMedium)
                Text(text = if (sensor.nombre_dueno != null) "👤 ${sensor.nombre_dueno}" else "Sin Asignar", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            // BOTÓN DE "USAR LLAVERO" (Para todos: Admin y Vecinos)
            IconButton(onClick = { viewModel.usarLlavero(sensor) }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Usar Llavero", tint = Color(0xFF4CAF50))
            }

            // CAMBIO 5: Estos botones SOLO los ve el Admin
            if (esAdmin) {
                IconButton(onClick = { onEditClick(sensor) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Blue)
                }
                IconButton(onClick = { viewModel.borrarSensor(sensor) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Red)
                }
                Switch(
                    checked = sensor.estado == "ACTIVO",
                    onCheckedChange = {
                        val nuevoEstado = if (sensor.estado == "ACTIVO") "INACTIVO" else "ACTIVO"
                        viewModel.editarSensor(sensor.copy(estado = nuevoEstado))
                    }
                )
            }
        }
    }
}

// ... (DialogEditarSensor, SectionGestionUsuarios y SectionHistorial se mantienen igual abajo) ...
@Composable
fun DialogEditarSensor(sensor: SensorDto, vecinos: List<UsuarioDto>, onDismiss: () -> Unit, onConfirm: (SensorDto) -> Unit) {
    var codigoEditado by remember { mutableStateOf(sensor.codigo_sensor) }
    var expanded by remember { mutableStateOf(false) }
    var usuarioSeleccionado by remember { mutableStateOf(vecinos.find { it.id == sensor.usuario_id }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Sensor") },
        text = {
            Column {
                OutlinedTextField(value = codigoEditado, onValueChange = { codigoEditado = it }, label = { Text("Código MAC") })
                Spacer(modifier = Modifier.height(16.dp))
                Box {
                    OutlinedButton(onClick = { expanded = true }) { Text(usuarioSeleccionado?.nombre ?: "Asignar a: Nadie") }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Ninguno") }, onClick = { usuarioSeleccionado = null; expanded = false })
                        vecinos.forEach { vecino: UsuarioDto ->
                            DropdownMenuItem(text = { Text(vecino.nombre ?: "?") }, onClick = { usuarioSeleccionado = vecino; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(sensor.copy(codigo_sensor = codigoEditado, usuario_id = usuarioSeleccionado?.id)) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun SectionGestionUsuarios(viewModel: IotViewModel) {
    LaunchedEffect(Unit) { viewModel.cargarVecinos() }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoEmail by remember { mutableStateOf("") }
    var nuevoPass by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Agregar Vecino", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nuevoEmail, onValueChange = { nuevoEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nuevoPass, onValueChange = { nuevoPass = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Button(onClick = { if (nuevoEmail.isNotEmpty() && nuevoPass.isNotEmpty()) { viewModel.crearVecino(nuevoNombre, nuevoEmail, nuevoPass); nuevoNombre = ""; nuevoEmail = ""; nuevoPass = "" } }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("REGISTRAR VECINO") }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Lista de Vecinos:", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(viewModel.vecinos) { vecino ->
                val esBloqueado = vecino.rol == "BLOQUEADO"
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = if (esBloqueado) Color(0xFFFFEBEE) else Color(0xFFE3F2FD))) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = vecino.nombre ?: "Sin nombre", style = MaterialTheme.typography.titleSmall)
                            Text(text = if(esBloqueado) "BLOQUEADO" else "HABILITADO", color = if(esBloqueado) Color.Red else Color.Green, style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { viewModel.bloquearVecino(vecino) }) { Icon(imageVector = if (esBloqueado) Icons.Default.Lock else Icons.Default.CheckCircle, contentDescription = "Bloquear", tint = if (esBloqueado) Color.Red else Color.Green) }
                    }
                }
            }
        }
    }
}

// --- SECCIÓN 4: HISTORIAL (MEJORADO) ---
@Composable
fun SectionHistorial(viewModel: IotViewModel) {
    LaunchedEffect(Unit) { viewModel.refrescarHistorial() }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { viewModel.refrescarHistorial() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ACTUALIZAR EVENTOS")
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(viewModel.historial) { evento ->
                // TRUCO: Limpiamos la fecha fea quitando la T y los milisegundos
                val fechaBonita = evento.fecha
                    .replace("T", "  ")  // Cambia la T por espacios
                    .substringBefore(".") // Quita los milisegundos (.000Z)

                ListItem(
                    headlineContent = {
                        Text(if (evento.tipo == "MANUAL_APP") "📱 Apertura Remota" else "🔑 Acceso Sensor")
                    },
                    supportingContent = {
                        Text("${evento.detalle}\n📅 $fechaBonita")
                    },
                    leadingContent = {
                        Icon(
                            if (evento.tipo == "ACCESO") Icons.Default.CheckCircle else Icons.Default.Info,
                            null,
                            tint = if (evento.tipo == "ACCESO") Color.Green else Color.Blue
                        )
                    }
                )
                HorizontalDivider()
            }
        }
    }
}