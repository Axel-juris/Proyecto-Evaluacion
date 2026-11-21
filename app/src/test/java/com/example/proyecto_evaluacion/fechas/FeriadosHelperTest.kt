package com.example.proyecto_evaluacion.fechas

import com.example.proyecto_evaluacion.data.fechas.Feriado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeriadosHelperTest {

    private val feriados = listOf(
        Feriado("2026-12-25", "Navidad", "Christmas Day"),
        Feriado("2026-01-01", "Año Nuevo", "New Year's Day"),
        Feriado("2026-12-08", "Inmaculada Concepción", "Immaculate Conception"),
        Feriado("2027-01-01", "Año Nuevo", "New Year's Day"),
        Feriado("2026-09-18", "Fiestas Patrias", "Independence Day")
    )

    @Test
    fun proximos_ordena_por_fecha_y_limita_la_cantidad() {
        val resultado = FeriadosHelper.proximos(feriados, "2026-09-01", 3)
        assertEquals(listOf("2026-09-18", "2026-12-08", "2026-12-25"), resultado.map { it.date })
    }

    @Test
    fun proximos_incluye_el_dia_de_hoy() {
        val resultado = FeriadosHelper.proximos(feriados, "2026-12-08", 1)
        assertEquals("2026-12-08", resultado.single().date)
    }

    @Test
    fun proximos_cantidad_por_defecto_es_tres() {
        assertEquals(3, FeriadosHelper.proximos(feriados, "2026-01-01").size)
    }

    @Test
    fun proximos_sin_resultados() {
        assertTrue(FeriadosHelper.proximos(feriados, "2030-01-01").isEmpty())
        assertTrue(FeriadosHelper.proximos(emptyList(), "2026-01-01").isEmpty())
    }

    @Test
    fun formatear_fecha_iso() {
        assertEquals("08/12/2026", FeriadosHelper.formatear("2026-12-08"))
        assertEquals("sin-formato", FeriadosHelper.formatear("sin-formato"))
    }
}