package com.example.proyecto_evaluacion.data

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/** Convierte cualquier excepción de red en un ErrorAplicacion con mensaje en español. */
fun traducirError(error: Exception): ErrorAplicacion {

    if (error is ErrorAplicacion) return error

    return when (error) {
        is HttpException -> {
            val codigo = error.code()
            val mensaje = when (codigo) {
                400 -> "Los datos enviados no son válidos."
                401 -> "Correo o clave incorrectos."
                403 -> "No tienes permisos para esta operación."
                404 -> "El registro solicitado no existe."
                409 -> "La operación genera un conflicto con datos existentes."
                422 -> "El servidor rechazó los datos."
                in 500..599 -> "Error interno del servidor."
                else -> "Error HTTP $codigo."
            }
            ErrorAplicacion(codigo, mensaje)
        }

        is SocketTimeoutException ->
            ErrorAplicacion(null, "El servidor tardó demasiado en responder.")

        is IOException ->
            ErrorAplicacion(null, "No hay conexión con el servidor. Revisa que los microservicios estén encendidos.")

        else ->
            ErrorAplicacion(null, "Ocurrió un error inesperado.")
    }
}