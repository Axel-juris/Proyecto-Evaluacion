package com.example.proyecto_evaluacion.validation

object OportunidadValidator {

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

    fun validarComuna(comuna: String): String? = when {
        comuna.isBlank() -> "La comuna es obligatoria"
        !comuna.all { it.isLetter() || it == ' ' } -> "Solo se permiten letras y espacios"
        comuna.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        else -> null
    }
}