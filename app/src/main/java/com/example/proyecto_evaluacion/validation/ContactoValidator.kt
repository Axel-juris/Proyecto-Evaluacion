package com.example.proyecto_evaluacion.validation

object ContactoValidator {

    val areasValidas = listOf(
        "Eventos",
        "Activaciones",
        "Marketing",
        "Comunicaciones",
        "Recursos Humanos",
        "Bienestar",
        "Beneficios",
        "Administración"
    )

    val mediosValidos = listOf("Correo", "Teléfono")

    fun validarNombre(nombre: String): String? = when {
        nombre.isBlank() -> "El nombre es obligatorio"
        nombre.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        !nombre.all { it.isLetter() || it == ' ' } -> "Solo se permiten letras y espacios"
        nombre.length > 60 -> "Máximo 60 caracteres"
        else -> null
    }

    fun validarOrganizacion(organizacion: String): String? = when {
        organizacion.isBlank() -> "La organización es obligatoria"
        organizacion.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        organizacion.length > 80 -> "Máximo 80 caracteres"
        else -> null
    }

    fun validarCargo(cargo: String): String? = when {
        cargo.isBlank() -> "El cargo es obligatorio"
        cargo.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        cargo.length > 60 -> "Máximo 60 caracteres"
        else -> null
    }

    fun validarArea(area: String): String? = when {
        area.isBlank() -> "El área es obligatoria"
        areasValidas.none { it.equals(area.trim(), ignoreCase = true) } ->
            "Área no válida, revisa la lista de arriba"
        else -> null
    }

    fun validarCorreo(correo: String): String? = when {
        correo.isBlank() -> "El correo es obligatorio"
        correo.contains(" ") -> "No debe contener espacios"
        !Regex("^[^@]+@[^@]+\\.[^@]+$").matches(correo.trim()) ->
            "Formato inválido, por ejemplo contacto@ejemplo.cl"
        else -> null
    }

    fun validarTelefono(telefono: String): String? = when {
        telefono.isBlank() -> "El teléfono es obligatorio"
        !Regex("^\\+?\\d{9,11}$").matches(telefono.trim()) ->
            "Usa de 9 a 11 dígitos, con + opcional al inicio"
        else -> null
    }

    fun validarMedio(medio: String): String? = when {
        medio.isBlank() -> "El medio preferente es obligatorio"
        mediosValidos.none { it.equals(medio.trim(), ignoreCase = true) } ->
            "Escribe Correo o Teléfono"
        else -> null
    }

    // Campo opcional
    fun validarObservaciones(observaciones: String): String? = when {
        observaciones.length > 200 -> "Máximo 200 caracteres"
        else -> null
    }
}