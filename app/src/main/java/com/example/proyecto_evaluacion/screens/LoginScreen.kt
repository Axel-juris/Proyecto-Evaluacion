
package com.example.proyecto_evaluacion.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_evaluacion.components.CampoValidado
import com.example.proyecto_evaluacion.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginExitoso: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {

        Text("Radar de Eventos", style = MaterialTheme.typography.headlineMedium)
        Text("Inicia sesión para continuar")

        CampoValidado(
            label = "Correo",
            value = uiState.correo,
            onValueChange = { viewModel.cambiarCorreo(it) },
            error = uiState.errorCorreo,
            keyboardType = KeyboardType.Email
        )

        CampoValidado(
            label = "Clave",
            value = uiState.clave,
            onValueChange = { viewModel.cambiarClave(it) },
            error = uiState.errorClave,
            visualTransformation = PasswordVisualTransformation()
        )

        AnimatedVisibility(
            visible = uiState.errorLogin != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error de acceso",
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = uiState.errorLogin ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Button(
            onClick = { viewModel.ingresar(onLoginExitoso) },
            enabled = !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.cargando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Ingresar")
            }
        }

        Text(
            text = "Cuentas de prueba (clave: clave123): supervisor@duoc.cl, ana@duoc.cl, bruno@duoc.cl, carla@duoc.cl",
            style = MaterialTheme.typography.bodySmall        )
    }
}