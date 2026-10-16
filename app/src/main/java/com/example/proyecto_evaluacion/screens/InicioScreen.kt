package com.example.proyecto_evaluacion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyecto_evaluacion.components.PantallaConBarra
import com.example.proyecto_evaluacion.data.SessionManager

@Preview(showBackground = true)
@Composable
fun InicioPreview() {
    InicioScreen(
        onIrALista = {},
        onIrAOportunidad = {},
        onIrAOrganizacion = {},
        onIrAContacto = {},
        onCerrarSesion = {}
    )
}

@Composable
fun InicioScreen(
    onIrALista: () -> Unit,
    onIrAOportunidad: () -> Unit,
    onIrAOrganizacion: () -> Unit,
    onIrAContacto: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val usuario by SessionManager.usuarioActual.collectAsStateWithLifecycle()
    val esSupervisor = usuario?.rol == "SUPERVISOR"

    PantallaConBarra(titulo = "Radar de Eventos") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (esSupervisor) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hola, ${usuario?.nombre ?: "invitado"}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = if (esSupervisor) "Supervisor Comercial" else "Ejecutivo Comercial",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Text(
                text = "¿Qué quieres hacer?",
                style = MaterialTheme.typography.titleMedium
            )

            Button(onClick = onIrALista, modifier = Modifier.fillMaxWidth()) {
                Text(if (esSupervisor) "Ver todas las oportunidades" else "Mis oportunidades")
            }
            Button(onClick = onIrAOportunidad, modifier = Modifier.fillMaxWidth()) {
                Text("Nueva oportunidad")
            }
            Button(onClick = onIrAOrganizacion, modifier = Modifier.fillMaxWidth()) {
                Text("Nueva organización")
            }
            Button(onClick = onIrAContacto, modifier = Modifier.fillMaxWidth()) {
                Text("Nuevo contacto")
            }
            OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
        }
    }
}