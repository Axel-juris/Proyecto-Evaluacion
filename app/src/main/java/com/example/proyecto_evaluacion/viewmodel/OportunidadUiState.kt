package com.example.proyecto_evaluacion.viewmodel


data class OportunidadUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val tipoEvento: String = "",
    val prioridad: String = "",
    val organizacionId: Int? = null,
    val organizacionNombre: String = "",
    val contactoId: Int? = null,
    val contactoNombre: String = "",
    val fechaInicio: String = "",
    val fechaTermino: String = "",
    val horario: String = "",
    val lugar: String = "",
    val publico: String = "",
    val costo: String = "",

    val errorTitulo: String? = null,
    val errorDescripcion: String? = null,
    val errorTipo: String? = null,
    val errorPrioridad: String? = null,
    val errorOrganizacion: String? = null,
    val errorFechaInicio: String? = null,
    val errorFechaTermino: String? = null,
    val errorHorario: String? = null,
    val errorLugar: String? = null,
    val errorPublico: String? = null,
    val errorCosto: String? = null,

    val errorOperacion: String? = null,
    val guardadoExitoso: Boolean = false
)