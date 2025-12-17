package com.example.eva3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.* // Importante para remember y LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eva3.data.remote.HttpClient
import com.example.eva3.data.remote.IotRepository
import com.example.eva3.screens.iot.IotControlScreen
import com.example.eva3.screens.iot.IotViewModel
import com.example.eva3.screens.login.LoginScreen
import com.example.eva3.ui.theme.Eva3Theme
import kotlinx.coroutines.delay // Importante para el temporizador

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Eva3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // --- LÓGICA DEL SPLASH SCREEN ---
                    var showSplash by remember { mutableStateOf(true) }

                    // Este efecto corre una sola vez al iniciar
                    LaunchedEffect(Unit) {
                        delay(2500) // Espera 2.5 segundos
                        showSplash = false // Oculta el splash y muestra la App
                    }

                    if (showSplash) {
                        // --- PANTALLA DE CARGA (TU LOGO) ---
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home, // Icono genérico
                                contentDescription = "Logo",
                                modifier = Modifier.size(200.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        // --- TU APP NORMAL (LÓGICA ORIGINAL) ---
                        val viewModel: IotViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return IotViewModel(IotRepository(HttpClient.iotApi)) as T
                            }
                        })

                        if (viewModel.usuarioLogueado == null) {
                            LoginScreen(
                                onLoginClick = { email, pass ->
                                    viewModel.login(email, pass)
                                },
                                errorMessage = viewModel.mensajeUsuario
                            )
                        } else {
                            IotControlScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}