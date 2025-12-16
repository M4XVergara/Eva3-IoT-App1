package com.example.eva3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eva3.data.remote.HttpClient
import com.example.eva3.data.remote.IotRepository
import com.example.eva3.screens.iot.IotControlScreen
import com.example.eva3.screens.iot.IotViewModel
import com.example.eva3.screens.login.LoginScreen // Crearemos esto en el Paso 2
import com.example.eva3.ui.theme.Eva3Theme

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
                    // 1. INYECCIÓN DE DEPENDENCIAS MANUAL
                    // Aquí creamos el ViewModel y le pasamos el Repositorio y la API
                    val viewModel: IotViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return IotViewModel(IotRepository(HttpClient.iotApi)) as T
                        }
                    })

                    // 2. NAVEGACIÓN SIMPLE
                    // Si el usuario es null, mostramos Login. Si no, mostramos el Control.
                    if (viewModel.usuarioLogueado == null) {
                        LoginScreen(
                            onLoginClick = { email, pass ->
                                viewModel.login(email, pass)
                            },
                            errorMessage = viewModel.mensajeUsuario
                        )
                    } else {
                        // Pasamos el viewModel a la pantalla principal para que controle todo
                        IotControlScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}