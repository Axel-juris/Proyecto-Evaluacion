package com.example.proyecto_evaluacion.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyecto_evaluacion.data.CrmRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConBarra(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val repositorio = CrmRepository.instancia
    val errorConexion by repositorio.errorConexion.collectAsStateWithLifecycle()

    // Al volver a una pantalla se vuelven a pedir los datos al servidor
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        repositorio.refrescar()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(titulo) },
                    navigationIcon = {
                        if (onVolver != null) {
                            IconButton(onClick = { onVolver() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver"
                                )
                            }
                        }
                    }
                )
                // Aviso cuando no se pueden cargar los datos (servidor apagado, sin red, etc.)
                AnimatedVisibility(
                    visible = errorConexion != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = errorConexion ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { repositorio.refrescar() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        },
        content = content
    )
}