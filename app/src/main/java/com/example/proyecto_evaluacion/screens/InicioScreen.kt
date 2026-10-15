package com.example.proyecto_evaluacion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyecto_evaluacion.components.PantallaConBarra

@Preview(showBackground = true)
@Composable
fun InicioPreview() {
    InicioScreen(
        onIrAOportunidad = {},
        onIrAEvento = {},
        onIrAOrganizacion = {},
        onCerrarSesion = {}
    )
}

@Composable
fun InicioScreen(
    onIrAOportunidad: () -> Unit,
    onIrAEvento: () -> Unit,
    onIrAOrganizacion: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    PantallaConBarra(titulo = "Radar de Eventos") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("¿Qué quieres hacer?", style = MaterialTheme.typography.headlineMedium)

            Button(onClick = onIrAOportunidad, modifier = Modifier.fillMaxWidth()) {
                Text("Nueva oportunidad")
            }
            Button(onClick = onIrAEvento, modifier = Modifier.fillMaxWidth()) {
                Text("Nuevo evento")
            }
            Button(onClick = onIrAOrganizacion, modifier = Modifier.fillMaxWidth()) {
                Text("Nueva organización")
            }
            OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
        }
    }
}
