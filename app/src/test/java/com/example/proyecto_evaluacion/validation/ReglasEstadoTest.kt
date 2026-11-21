package com.example.proyecto_evaluacion.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasEstadoTest {

    @Test
    fun transiciones_desde_NUEVA() {
        assertEquals(listOf("EN_GESTION", "DESCARTADA"), ReglasEstado.transicionesPermitidas("NUEVA", "SUPERVISOR"))
        assertEquals(listOf("EN_GESTION"), ReglasEstado.transicionesPermitidas("NUEVA", "EJECUTIVO"))
    }

    @Test
    fun transiciones_desde_EN_GESTION() {
        assertEquals(listOf("CONFIRMADA", "DESCARTADA"), ReglasEstado.transicionesPermitidas("EN_GESTION", "SUPERVISOR"))
        assertTrue(ReglasEstado.transicionesPermitidas("EN_GESTION", "EJECUTIVO").isEmpty())
    }

    @Test
    fun transiciones_desde_estados_finales() {
        listOf("CONFIRMADA", "DESCARTADA").forEach { estado ->
            assertEquals(listOf("EN_GESTION"), ReglasEstado.transicionesPermitidas(estado, "SUPERVISOR"))
            assertTrue(ReglasEstado.transicionesPermitidas(estado, "EJECUTIVO").isEmpty())
        }
    }

    @Test
    fun estado_desconocido_no_tiene_transiciones() {
        assertTrue(ReglasEstado.transicionesPermitidas("OTRO", "SUPERVISOR").isEmpty())
    }

    @Test
    fun puede_editar() {
        assertTrue(ReglasEstado.puedeEditar("SUPERVISOR", 1, 9, "CONFIRMADA"))
        assertTrue(ReglasEstado.puedeEditar("EJECUTIVO", 2, 2, "NUEVA"))
        assertTrue(ReglasEstado.puedeEditar("EJECUTIVO", 2, 2, "EN_GESTION"))
        assertFalse(ReglasEstado.puedeEditar("EJECUTIVO", 2, 2, "CONFIRMADA"))
        assertFalse(ReglasEstado.puedeEditar("EJECUTIVO", 2, 3, "NUEVA"))
        assertFalse(ReglasEstado.puedeEditar("EJECUTIVO", null, 2, "NUEVA"))
        assertFalse(ReglasEstado.puedeEditar(null, 2, 2, "NUEVA"))
        assertFalse(ReglasEstado.puedeEditar("OTRO", 2, 2, "NUEVA"))
    }

    @Test
    fun puede_eliminar() {
        assertTrue(ReglasEstado.puedeEliminar("SUPERVISOR", 1, 9, "CONFIRMADA"))
        assertTrue(ReglasEstado.puedeEliminar("EJECUTIVO", 2, 2, "NUEVA"))
        assertFalse(ReglasEstado.puedeEliminar("EJECUTIVO", 2, 2, "EN_GESTION"))
        assertFalse(ReglasEstado.puedeEliminar("EJECUTIVO", 2, 3, "NUEVA"))
        assertFalse(ReglasEstado.puedeEliminar("EJECUTIVO", null, 2, "NUEVA"))
        assertFalse(ReglasEstado.puedeEliminar(null, 2, 2, "NUEVA"))
    }
}