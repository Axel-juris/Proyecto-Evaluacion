package com.example.proyecto_evaluacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.proyecto_evaluacion.screens.LoginScreen
import com.example.proyecto_evaluacion.screens.OportunidadFormScreen
import com.example.proyecto_evaluacion.ui.theme.ProyectoEvaluacionTheme
import androidx.compose.runtime.*
import com.example.proyecto_evaluacion.screens.LoginScreen
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoEvaluacionTheme {
                var sesionIniciada by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (sesionIniciada) {
                        OportunidadFormScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        LoginScreen(
                            modifier = Modifier.padding(innerPadding),
                            onLoginExitoso = { sesionIniciada = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ProyectoEvaluacionTheme {
        Greeting("Android")
    }
}