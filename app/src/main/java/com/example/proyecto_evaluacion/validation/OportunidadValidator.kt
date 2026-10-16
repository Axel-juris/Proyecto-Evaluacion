package com.example.proyecto_evaluacion.validation

object OportunidadValidator {

    val prioridades = listOf("ALTA", "MEDIA", "BAJA")

    fun validarNombre(nombre: String): String? = when {
        nombre.isBlank() -> "El nombre es obligatorio"
        nombre.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        nombre.length > 60 -> "Máximo 60 caracteres"
        else -> null
    }

    fun validarDescripcion(descripcion: String): String? = when {
        descripcion.isBlank() -> "La descripción es obligatoria"
        descripcion.trim().length < 10 -> "Debe tener al menos 10 caracteres"
        descripcion.length > 200 -> "Máximo 200 caracteres"
        else -> null
    }

    fun validarTipo(tipo: String): String? =
        if (tipo.isBlank()) "Selecciona el tipo de evento" else null

    fun validarPrioridad(prioridad: String): String? =
        if (prioridad.isBlank()) "Selecciona la prioridad" else null

    fun validarOrganizacion(organizacion: String): String? =
        if (organizacion.isBlank()) "Selecciona una organización" else null

    fun validarLugar(lugar: String): String? = when {
        lugar.isBlank() -> "El lugar es obligatorio"
        lugar.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        lugar.length > 80 -> "Máximo 80 caracteres"
        else -> null
    }

    // Fechas, horario, público y costo reutilizan las reglas de EventoValidator
    fun validarFechaInicio(fecha: String): String? = EventoValidator.validarFechaInicio(fecha)

    fun validarFechaTermino(inicio: String, termino: String): String? =
        EventoValidator.validarFechaTermino(inicio, termino)

    fun validarHorario(horario: String): String? = EventoValidator.validarHorario(horario)

    fun validarPublico(publico: String): String? = EventoValidator.validarAsistentes(publico)

    fun validarCosto(costo: String): String? = EventoValidator.validarCosto(costo)
}