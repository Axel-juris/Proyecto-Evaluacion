package com.example.proyecto_evaluacion.validation

object LoginValidator {

    fun validarCorreo(correo: String): String? = when {
        correo.isBlank() -> "El correo es obligatorio"
        correo.contains(" ") -> "No debe contener espacios"
        !Regex("^[^@]+@[^@]+\\.[^@]+$").matches(correo.trim()) ->
            "Formato inválido, por ejemplo ana@duoc.cl"
        else -> null
    }

    fun validarClave(clave: String): String? = when {
        clave.isBlank() -> "La clave es obligatoria"
        clave.length < 6 -> "Debe tener al menos 6 caracteres"
        else -> null
    }
}