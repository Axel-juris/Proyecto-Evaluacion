package com.example.proyecto_evaluacion.validation
object OrganizacionValidator {

    val tiposValidos = listOf(
        "Empresa",
        "Municipalidad",
        "Institución pública",
        "Institución privada",
        "Organizador de eventos",
        "Centro comercial",
        "Asociación",
        "Otra entidad"
    )

    fun validarNombre(nombre: String): String? = when {
        nombre.isBlank() -> "El nombre es obligatorio"
        nombre.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        nombre.length > 80 -> "Máximo 80 caracteres"
        else -> null
    }

    fun validarTipo(tipo: String): String? = when {
        tipo.isBlank() -> "El tipo de organización es obligatorio"
        tiposValidos.none { it.equals(tipo.trim(), ignoreCase = true) } ->
            "Tipo no válido, revisa la lista de arriba"
        else -> null
    }

    fun validarRubro(rubro: String): String? = when {
        rubro.isBlank() -> "El rubro es obligatorio"
        rubro.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        rubro.length > 60 -> "Máximo 60 caracteres"
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

    // Campo opcional
    fun validarSitioWeb(sitioWeb: String): String? = when {
        sitioWeb.isBlank() -> null
        sitioWeb.contains(" ") -> "No debe contener espacios"
        !sitioWeb.contains(".") -> "Ingresa un sitio válido, por ejemplo www.ejemplo.cl"
        else -> null
    }

    // Campo opcional
    fun validarObservaciones(observaciones: String): String? = when {
        observaciones.length > 200 -> "Máximo 200 caracteres"
        else -> null
    }
}