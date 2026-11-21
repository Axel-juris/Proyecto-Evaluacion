package com.example.proyecto_evaluacion.fechas

import com.example.proyecto_evaluacion.fechaRelativa
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class FechasTest {

    @Test
    fun parsear_fecha_valida_e_invalida() {
        assertNotNull(Fechas.parsear("15/03/2030"))
        assertNotNull(Fechas.parsear("  15/03/2030 "))
        assertNull(Fechas.parsear(null))
        assertNull(Fechas.parsear(""))
        assertNull(Fechas.parsear("31/02/2030"))
        assertNull(Fechas.parsear("texto"))
    }

    @Test
    fun hoy_es_medianoche() {
        val calendario = Calendar.getInstance().apply { time = Fechas.hoy() }
        assertEquals(0, calendario.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendario.get(Calendar.MINUTE))
        assertEquals(0, calendario.get(Calendar.SECOND))
        assertEquals(0, calendario.get(Calendar.MILLISECOND))
    }

    @Test
    fun dias_desde_hoy() {
        assertEquals(0, Fechas.diasDesdeHoy(fechaRelativa(0)))
        assertEquals(1, Fechas.diasDesdeHoy(fechaRelativa(1)))
        assertEquals(10, Fechas.diasDesdeHoy(fechaRelativa(10)))
        assertEquals(-1, Fechas.diasDesdeHoy(fechaRelativa(-1)))
        assertEquals(-30, Fechas.diasDesdeHoy(fechaRelativa(-30)))
        assertNull(Fechas.diasDesdeHoy("no es fecha"))
        assertNull(Fechas.diasDesdeHoy(null))
    }

    @Test
    fun milis_local_con_hora() {
        val milis = Fechas.milisLocal("15/03/2030", "09:30")
        assertNotNull(milis)
        val calendario = Calendar.getInstance().apply { timeInMillis = milis!! }
        assertEquals(2030, calendario.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, calendario.get(Calendar.MONTH))
        assertEquals(15, calendario.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, calendario.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, calendario.get(Calendar.MINUTE))
    }

    @Test
    fun milis_local_invalidos() {
        assertNull(Fechas.milisLocal(null, "09:30"))
        assertNull(Fechas.milisLocal("", "09:30"))
        assertNull(Fechas.milisLocal("15/03/2030", "25:99"))
        assertNull(Fechas.milisLocal("fecha", "09:30"))
    }

    @Test
    fun milis_inicio_dia_utc() {
        assertEquals(0L, Fechas.milisInicioDiaUtc("01/01/1970"))
        assertEquals(86_400_000L, Fechas.milisInicioDiaUtc("02/01/1970"))
        assertNull(Fechas.milisInicioDiaUtc(null))
        assertNull(Fechas.milisInicioDiaUtc(" "))
        assertNull(Fechas.milisInicioDiaUtc("32/01/2030"))
    }
}