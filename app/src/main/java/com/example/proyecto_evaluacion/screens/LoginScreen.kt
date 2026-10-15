package com.example.proyecto_evaluacion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_evaluacion.validation.LoginValidator

@Preview(showBackground = true)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginExitoso: () -> Unit = {}
) {

    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }

    var errorUsuario by remember { mutableStateOf<String?>(null) }
    var errorClave by remember { mutableStateOf<String?>(null) }
    var errorLogin by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {

        Text("Radar de Eventos", fontSize = 27.sp)
        Text("Inicia sesión para continuar")

        OutlinedTextField(
            value = usuario,
            onValueChange = {
                usuario = it
                errorUsuario = LoginValidator.validarUsuario(it)
                errorLogin = null
            },
            label = { Text("Usuario") },
            isError = errorUsuario != null,
            supportingText = { errorUsuario?.let { Text(it) } },
            trailingIcon = {
                if (errorUsuario != null) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else if (usuario.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Campo válido"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = clave,
            onValueChange = {
                clave = it
                errorClave = LoginValidator.validarClave(it)
                errorLogin = null
            },
            label = { Text("Clave") },
            visualTransformation = PasswordVisualTransformation(),
            isError = errorClave != null,
            supportingText = { errorClave?.let { Text(it) } },
            trailingIcon = {
                if (errorClave != null) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else if (clave.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Campo válido"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        errorLogin?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error de acceso",
                    tint = MaterialTheme.colorScheme.error
                )
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }

        Button(
            onClick = {
                errorUsuario = LoginValidator.validarUsuario(usuario)
                errorClave = LoginValidator.validarClave(clave)

                if (errorUsuario == null && errorClave == null) {
                    if (LoginValidator.credencialesValidas(usuario, clave)) {
                        errorLogin = null
                        onLoginExitoso()
                    } else {
                        errorLogin = "Usuario o clave incorrectos"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }
    }
}