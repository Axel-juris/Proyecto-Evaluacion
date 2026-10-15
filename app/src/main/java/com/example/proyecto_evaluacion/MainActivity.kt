package com.example.proyecto_evaluacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.proyecto_evaluacion.ui.theme.ProyectoEvaluacionTheme
import com.example.proyecto_evaluacion.navegacion.Navegacion
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoEvaluacionTheme {
                Navegacion()
            }
        }
    }
}