package com.example.proyecto_evaluacion.validation

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EventoValidator {

    val tiposValidos = listOf(
        "Evento corporativo",
        "Feria empresarial",
        "Actividad municipal",
        "Jornada de beneficios",
        "Feria institucional",
        "Actividad de trabajadores",
        "Activación comercial",
        "Otra instancia relevante"
    )

    private fun parsearFecha(fecha: String): Date? {
        if (!Regex("^\\d{2}/\\d{2}/\\d{4}$").matches(fecha)) return null
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formato.isLenient = false
        return try {
            formato.parse(fecha)
        } catch (e: Exception) {
            null
        }
    }

    fun validarNombre(nombre: String): String? = when {
        nombre.isBlank() -> "El nombre es obligatorio"
        nombre.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        nombre.length > 80 -> "Máximo 80 caracteres"
        else -> null
    }

    fun validarTipo(tipo: String): String? = when {
        tipo.isBlank() -> "El tipo de evento es obligatorio"
        tiposValidos.none { it.equals(tipo.trim(), ignoreCase = true) } ->
            "Tipo no válido, revisa la lista de arriba"
        else -> null
    }

    fun validarFechaInicio(fecha: String): String? = when {
        fecha.isBlank() -> "La fecha de inicio es obligatoria"
        parsearFecha(fecha.trim()) == null -> "Usa el formato dd/mm/aaaa con una fecha real"
        else -> null
    }

    fun validarFechaTermino(inicio: String, termino: String): String? {
        val fechaInicio = parsearFecha(inicio.trim())
        val fechaTermino = parsearFecha(termino.trim())
        return when {
            termino.isBlank() -> "La fecha de término es obligatoria"
            fechaTermino == null -> "Usa el formato dd/mm/aaaa con una fecha real"
            fechaInicio != null && fechaTermino.before(fechaInicio) ->
                "No puede ser anterior a la fecha de inicio"
            else -> null
        }
    }

    fun validarHorario(horario: String): String? = when {
        horario.isBlank() -> "El horario es obligatorio"
        !Regex("^([01]\\d|2[0-3]):[0-5]\\d$").matches(horario.trim()) ->
            "Usa el formato HH:mm (ejemplo: 09:30)"
        else -> null
    }

    fun validarComuna(comuna: String): String? = when {
        comuna.isBlank() -> "La comuna es obligatoria"
        !comuna.all { it.isLetter() || it == ' ' } -> "Solo se permiten letras y espacios"
        comuna.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        else -> null
    }

    fun validarRegion(region: String): String? = when {
        region.isBlank() -> "La región es obligatoria"
        region.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        else -> null
    }

    fun validarOrganizacion(organizacion: String): String? = when {
        organizacion.isBlank() -> "La organización es obligatoria"
        organizacion.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        organizacion.length > 80 -> "Máximo 80 caracteres"
        else -> null
    }

    fun validarAsistentes(asistentes: String): String? {
        val cantidad = asistentes.trim().toIntOrNull()
        return when {
            asistentes.isBlank() -> "El público esperado es obligatorio"
            cantidad == null -> "Ingresa solo números"
            cantidad <= 0 -> "Debe ser mayor a 0"
            cantidad > 100000 -> "El valor es demasiado alto"
            else -> null
        }
    }

    fun validarCosto(costo: String): String? {
        val monto = costo.trim().toIntOrNull()
        return when {
            costo.isBlank() -> "El costo referencial es obligatorio (puede ser 0)"
            monto == null -> "Ingresa solo números, sin puntos ni símbolos"
            monto < 0 -> "No puede ser negativo"
            monto > 100000000 -> "El valor es demasiado alto"
            else -> null
        }
    }

    // Campo opcional: solo se valida el largo
    fun validarCondiciones(condiciones: String): String? = when {
        condiciones.length > 200 -> "Máximo 200 caracteres"
        else -> null
    }
}