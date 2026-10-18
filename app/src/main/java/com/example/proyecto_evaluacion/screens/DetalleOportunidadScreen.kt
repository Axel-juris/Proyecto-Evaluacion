package com.example.proyecto_evaluacion.screens

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.proyecto_evaluacion.components.CampoValidado
import com.example.proyecto_evaluacion.components.EtiquetaColor
import com.example.proyecto_evaluacion.components.SelectorDesplegable
import com.example.proyecto_evaluacion.data.Oportunidad
import com.example.proyecto_evaluacion.ui.theme.colorEstado
import com.example.proyecto_evaluacion.ui.theme.colorPrioridad
import com.example.proyecto_evaluacion.ui.theme.textoEstado
import com.example.proyecto_evaluacion.fechas.Calendario
import com.example.proyecto_evaluacion.fechas.Evidencias
import com.example.proyecto_evaluacion.fechas.Fechas
import com.example.proyecto_evaluacion.validation.SeguimientoValidator
import com.example.proyecto_evaluacion.viewmodel.DetalleOportunidadUiState
import com.example.proyecto_evaluacion.viewmodel.DetalleOportunidadViewModel

@Composable
fun DetalleOportunidadScreen(
    oportunidadId: Int,
    modifier: Modifier = Modifier,
    onEditar: (Int) -> Unit = {},
    viewModel: DetalleOportunidadViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(oportunidadId) {
        viewModel.cargar(oportunidadId)
    }

    val oportunidad = uiState.oportunidad

    if (uiState.cargando) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (oportunidad == null) {
        MensajeCentrado("No se encontró la oportunidad.", modifier)
    } else if (!uiState.autorizado) {
        MensajeCentrado(
            "No tienes acceso a esta oportunidad. Solo el supervisor o el ejecutivo asignado pueden verla.",
            modifier
        )
    } else {
        ContenidoDetalle(uiState, oportunidad, viewModel, onEditar, modifier)
    }
}

@Composable
private fun MensajeCentrado(texto: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = texto, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ContenidoDetalle(
    uiState: DetalleOportunidadUiState,
    oportunidad: Oportunidad,
    viewModel: DetalleOportunidadViewModel,
    onEditar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val form = uiState.form
    var mostrarFormulario by remember { mutableStateOf(false) }

    // Se guarda con rememberSaveable por si el sistema recrea la pantalla mientras está la cámara abierta
    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Todos los resultados (éxito o error) se muestran como aviso flotante, visible en cualquier parte de la pantalla
    LaunchedEffect(form.mensajeExito, form.mensajeError) {
        val texto = form.mensajeExito ?: form.mensajeError
        if (texto != null) {
            snackbarHostState.showSnackbar(texto)
            viewModel.limpiarMensajes()
        }
    }

    // Al guardar una gestión, el formulario se pliega solo
    LaunchedEffect(form.seguimientoGuardado) {
        if (form.seguimientoGuardado) mostrarFormulario = false
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val destino = uriPendiente
        uriPendiente = null
        if (exito && destino != null) {
            viewModel.guardarFoto(destino)
        } else {
            // Captura cancelada: se borra el archivo vacío que se había creado
            Evidencias.eliminar(context, destino)
            viewModel.informar(null, "No se tomó la foto (captura cancelada)")
        }
    }

    // El color del encabezado cambia con animación cuando cambia el estado
    val colorEncabezado by animateColorAsState(
        targetValue = colorEstado(oportunidad.estado).copy(alpha = 0.15f),
        label = "colorEncabezado"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                colors = CardDefaults.cardColors(containerColor = colorEncabezado)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = oportunidad.titulo,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EtiquetaColor(
                            texto = textoEstado(oportunidad.estado),
                            color = colorEstado(oportunidad.estado)
                        )
                        EtiquetaColor(
                            texto = "Prioridad ${oportunidad.prioridad.lowercase()}",
                            color = colorPrioridad(oportunidad.prioridad)
                        )
                    }
                    Text(
                        text = oportunidad.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Dato("Organización", uiState.organizacionNombre)
                    Dato("Contacto", uiState.contactoNombre)
                    Dato("Tipo de evento", oportunidad.tipoEvento)
                    Dato(
                        "Fechas",
                        "${oportunidad.fechaEvento} al ${oportunidad.fechaTermino} · ${oportunidad.horario}"
                    )
                    Dato("Lugar", oportunidad.lugarEvento)
                    Dato("Público esperado", "${oportunidad.publicoEsperado} asistentes")
                    Dato("Costo referencial (ficticio)", "\$${oportunidad.monto.toLong()}")
                    Dato("Responsable", uiState.responsable)
                    Dato("Detectada el", oportunidad.fechaCreacion)
                }
            }

            OutlinedButton(
                onClick = {
                    val inicio = Fechas.milisLocal(oportunidad.fechaEvento, oportunidad.horario)
                    // Mismo día: dura 1 hora. Varios días: termina el último día a las 23:59
                    val fin = if (oportunidad.fechaTermino == oportunidad.fechaEvento) {
                        inicio?.plus(3_600_000L)
                    } else {
                        Fechas.milisLocal(oportunidad.fechaTermino, "23:59")
                    }

                    if (inicio == null || fin == null) {
                        viewModel.informar(
                            null,
                            "Las fechas de la oportunidad no son válidas para el calendario"
                        )
                    } else {
                        val error = Calendario.agendarEvento(
                            context = context,
                            titulo = oportunidad.titulo,
                            descripcion = "${oportunidad.tipoEvento} · ${uiState.organizacionNombre}",
                            lugar = oportunidad.lugarEvento,
                            inicioMillis = inicio,
                            finMillis = fin,
                            todoElDia = false
                        )
                        if (error != null) viewModel.informar(null, error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar evento al calendario")
            }

            HorizontalDivider()

            Text(text = "Evidencia fotográfica", style = MaterialTheme.typography.titleMedium)

            if (oportunidad.fotoUri != null) {
                AsyncImage(
                    model = oportunidad.fotoUri,
                    contentDescription = "Evidencia fotográfica de la oportunidad",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            } else {
                Text(
                    text = "Aún no hay evidencia fotográfica para esta oportunidad.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    try {
                        val destino = Evidencias.crearDestino(context, oportunidad.id)
                        uriPendiente = destino.toString()
                        lanzadorCamara.launch(destino)
                    } catch (e: ActivityNotFoundException) {
                        Evidencias.eliminar(context, uriPendiente)
                        uriPendiente = null
                        viewModel.informar(
                            null,
                            "Este dispositivo no tiene una app de cámara disponible"
                        )
                    } catch (e: Exception) {
                        viewModel.informar(null, "No se pudo preparar la cámara, intenta de nuevo")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (oportunidad.fotoUri == null) "Tomar foto" else "Reemplazar foto")
            }

            HorizontalDivider()

            if (uiState.puedeEditar) {
                OutlinedButton(
                    onClick = { onEditar(oportunidad.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar oportunidad")
                }
            }

            Text(text = "Estado de la gestión", style = MaterialTheme.typography.titleMedium)

            if (uiState.transicionesPermitidas.isEmpty()) {
                Text(
                    text = if (uiState.esSupervisor) {
                        "Esta oportunidad no tiene cambios de estado disponibles."
                    } else {
                        "Solo el supervisor puede confirmar o descartar oportunidades."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                uiState.transicionesPermitidas.forEach { nuevoEstado ->
                    Button(
                        onClick = { viewModel.cambiarEstado(nuevoEstado) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(etiquetaTransicion(oportunidad.estado, nuevoEstado))
                    }
                }
            }

            if (uiState.esSupervisor) {
                Text(text = "Asignar responsable", style = MaterialTheme.typography.titleMedium)

                if (uiState.ejecutivos.isEmpty()) {
                    Text(
                        text = "No hay ejecutivos disponibles.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    SelectorDesplegable(
                        label = "Ejecutivo responsable",
                        seleccion = uiState.responsable,
                        opciones = uiState.ejecutivos.map { it.nombre },
                        onSeleccionar = { indice ->
                            viewModel.asignar(uiState.ejecutivos[indice].id)
                        },
                        error = null
                    )
                }
            }

            HorizontalDivider()

            Text(
                text = "Historial de gestiones (${uiState.seguimientos.size})",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick = { mostrarFormulario = !mostrarFormulario },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (mostrarFormulario) "Cancelar" else "Registrar nueva gestión")
            }

            AnimatedVisibility(
                visible = mostrarFormulario,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SelectorDesplegable(
                        label = "Tipo de gestión",
                        seleccion = SeguimientoValidator.etiquetaTipo(form.tipoGestion),
                        opciones = SeguimientoValidator.tiposGestion.map { it.second },
                        onSeleccionar = { indice ->
                            viewModel.cambiarTipoGestion(SeguimientoValidator.tiposGestion[indice].first)
                        },
                        error = form.errorTipo
                    )

                    CampoValidado(
                        label = "Observación",
                        value = form.observacion,
                        onValueChange = { viewModel.cambiarObservacion(it) },
                        error = form.errorObservacion
                    )

                    CampoValidado(
                        label = "Próxima gestión (dd/mm/aaaa, opcional)",
                        value = form.proximaFecha,
                        onValueChange = { viewModel.cambiarProximaFecha(it) },
                        error = form.errorProximaFecha
                    )

                    Button(
                        onClick = { viewModel.guardarSeguimiento() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar gestión")
                    }
                }
            }

            if (uiState.seguimientos.isEmpty()) {
                Text(
                    text = "Aún no hay gestiones registradas para esta oportunidad.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                uiState.seguimientos.forEach { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${SeguimientoValidator.etiquetaTipo(item.seguimiento.tipoGestion)} · ${item.seguimiento.fecha}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Por ${item.autor}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = item.seguimiento.observacion,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            item.seguimiento.proximaGestionFecha?.let { fecha ->
                                Text(
                                    text = "Próxima gestión: $fecha",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                TextButton(
                                    onClick = {
                                        // La gestión solo tiene fecha, así que se agenda como evento de todo el día
                                        val inicio = Fechas.milisInicioDiaUtc(fecha)
                                        if (inicio == null) {
                                            viewModel.informar(
                                                null,
                                                "La fecha de la gestión no es válida para el calendario"
                                            )
                                        } else {
                                            val error = Calendario.agendarEvento(
                                                context = context,
                                                titulo = "Seguimiento: ${oportunidad.titulo}",
                                                descripcion = "${SeguimientoValidator.etiquetaTipo(item.seguimiento.tipoGestion)}: ${item.seguimiento.observacion}",
                                                lugar = oportunidad.lugarEvento,
                                                inicioMillis = inicio,
                                                finMillis = inicio + 86_400_000L,
                                                todoElDia = true
                                            )
                                            if (error != null) viewModel.informar(null, error)
                                        }
                                    }
                                ) {
                                    Text("Agendar en calendario")
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Column {
        Text(text = etiqueta, style = MaterialTheme.typography.labelMedium)
        Text(text = valor, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun etiquetaTransicion(estadoActual: String, nuevoEstado: String): String =
    when (nuevoEstado) {
        "EN_GESTION" -> if (estadoActual == "NUEVA") "Iniciar gestión" else "Reabrir gestión"
        "CONFIRMADA" -> "Confirmar participación"
        "DESCARTADA" -> "Descartar oportunidad"
        else -> "Pasar a ${textoEstado(nuevoEstado)}"
    }