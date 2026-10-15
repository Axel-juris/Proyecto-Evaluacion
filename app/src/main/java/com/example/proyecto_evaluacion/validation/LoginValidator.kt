package com.example.proyecto_evaluacion.validation

object LoginValidator {

    fun validarUsuario(usuario: String): String? = when {
        usuario.isBlank() -> "El usuario es obligatorio"
        usuario.contains(" ") -> "No debe contener espacios"
        usuario.length < 4 -> "Debe tener al menos 4 caracteres"
        else -> null
    }

    fun validarClave(clave: String): String? = when {
        clave.isBlank() -> "La clave es obligatoria"
        clave.length < 6 -> "Debe tener al menos 6 caracteres"
        else -> null
    }

    // Cuenta ficticia para el MVP. Más adelante se reemplaza por Room.
    fun credencialesValidas(usuario: String, clave: String): Boolean =
        usuario == "ejecutivo01" && clave == "clave123"
}