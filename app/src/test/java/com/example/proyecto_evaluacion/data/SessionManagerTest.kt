package com.example.proyecto_evaluacion.data

import com.example.proyecto_evaluacion.EJECUTIVO
import com.example.proyecto_evaluacion.SUPERVISOR
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionManagerTest {

    @Before
    fun setUp() = SessionManager.cerrarSesion()

    @After
    fun tearDown() = SessionManager.cerrarSesion()

    @Test
    fun sin_sesion_no_hay_rol() {
        assertNull(SessionManager.usuarioActual.value)
        assertFalse(SessionManager.esSupervisor())
        assertFalse(SessionManager.esEjecutivo())
    }

    @Test
    fun iniciar_sesion_como_supervisor() {
        SessionManager.iniciarSesion(SUPERVISOR)
        assertEquals(SUPERVISOR, SessionManager.usuarioActual.value)
        assertTrue(SessionManager.esSupervisor())
        assertFalse(SessionManager.esEjecutivo())
    }

    @Test
    fun iniciar_sesion_como_ejecutivo() {
        SessionManager.iniciarSesion(EJECUTIVO)
        assertTrue(SessionManager.esEjecutivo())
        assertFalse(SessionManager.esSupervisor())
    }

    @Test
    fun cerrar_sesion_limpia_el_usuario() {
        SessionManager.iniciarSesion(SUPERVISOR)
        SessionManager.cerrarSesion()
        assertNull(SessionManager.usuarioActual.value)
    }
}