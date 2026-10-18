package com.example.proyecto_evaluacion.viewmodel

import com.example.proyecto_evaluacion.data.Oportunidad
import com.example.proyecto_evaluacion.data.Seguimiento
import com.example.proyecto_evaluacion.data.Usuario

data class SeguimientoItem(
    val seguimiento: Seguimiento,
    val autor: String
)

// Campos del formulario de gestión y mensajes de resultado
data class EstadoFormulario(
    val tipoGestion: String = "",
    val observacion: String = "",
    val proximaFecha: String = "",

    val errorTipo: String? = null,
    val errorObservacion: String? = null,
    val errorProximaFecha: String? = null,

    val mensajeExito: String? = null,
    val mensajeError: String? = null,
    val seguimientoGuardado: Boolean = false
)

data class DetalleOportunidadUiState(
    val cargando: Boolean = true,
    val oportunidad: Oportunidad? = null,
    val organizacionNombre: String = "",
    val contactoNombre: String = "Sin contacto",
    val responsable: String = "Sin asignar",
    val ejecutivos: List<Usuario> = emptyList(),
    val seguimientos: List<SeguimientoItem> = emptyList(),
    val esSupervisor: Boolean = false,
    val autorizado: Boolean = true,
    val puedeEditar: Boolean = false,
    val transicionesPermitidas: List<String> = emptyList(),
    val form: EstadoFormulario = EstadoFormulario()
)