package com.example.proyecto_evaluacion.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
    }

    fun esSupervisor(): Boolean {
        return _usuarioActual.value?.rol == "SUPERVISOR"
    }

    fun esEjecutivo(): Boolean {
        return _usuarioActual.value?.rol == "EJECUTIVO"
    }
}