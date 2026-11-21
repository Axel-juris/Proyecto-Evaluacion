package com.example.proyecto_evaluacion.data

import com.example.proyecto_evaluacion.httpError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class ApiErrorMapperTest {

    @Test
    fun errores_http_se_traducen_a_mensajes_en_espanol() {
        val esperados = mapOf(
            400 to "Los datos enviados no son válidos.",
            401 to "Correo o clave incorrectos.",
            403 to "No tienes permisos para esta operación.",
            404 to "El registro solicitado no existe.",
            409 to "La operación genera un conflicto con datos existentes.",
            422 to "El servidor rechazó los datos.",
            500 to "Error interno del servidor.",
            503 to "Error interno del servidor.",
            418 to "Error HTTP 418."
        )
        esperados.forEach { (codigo, mensaje) ->
            val error = traducirError(httpError(codigo))
            assertEquals(codigo, error.codigoHttp)
            assertEquals(mensaje, error.message)
        }
    }

    @Test
    fun timeout_se_distingue_de_otros_errores_de_red() {
        val error = traducirError(SocketTimeoutException("lento"))
        assertNull(error.codigoHttp)
        assertEquals("El servidor tardó demasiado en responder.", error.message)
    }

    @Test
    fun error_de_red_generico() {
        val error = traducirError(IOException("sin red"))
        assertNull(error.codigoHttp)
        assertEquals(
            "No hay conexión con el servidor. Revisa que los microservicios estén encendidos.",
            error.message
        )
    }

    @Test
    fun error_inesperado() {
        val error = traducirError(IllegalStateException("boom"))
        assertNull(error.codigoHttp)
        assertEquals("Ocurrió un error inesperado.", error.message)
    }

    @Test
    fun un_ErrorAplicacion_no_se_vuelve_a_traducir() {
        val original = ErrorAplicacion(404, "ya traducido")
        assertSame(original, traducirError(original))
    }
}