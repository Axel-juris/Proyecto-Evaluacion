package com.example.proyecto_evaluacion.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EventoValidatorTest {

    @Test
    fun nombre() {
        assertEquals("El nombre es obligatorio", EventoValidator.validarNombre(""))
        assertEquals("Debe tener al menos 3 caracteres", EventoValidator.validarNombre("ab"))
        assertEquals("Máximo 80 caracteres", EventoValidator.validarNombre("a".repeat(81)))
        assertNull(EventoValidator.validarNombre("Feria"))
    }

    @Test
    fun tipo() {
        assertEquals("El tipo de evento es obligatorio", EventoValidator.validarTipo(""))
        assertNotNull(EventoValidator.validarTipo("Fiesta"))
        assertNull(EventoValidator.validarTipo("feria empresarial"))
    }

    @Test
    fun fecha_inicio() {
        assertEquals("La fecha de inicio es obligatoria", EventoValidator.validarFechaInicio(""))
        assertNotNull(EventoValidator.validarFechaInicio("2030-03-15"))
        assertNotNull(EventoValidator.validarFechaInicio("31/02/2030"))
        assertNotNull(EventoValidator.validarFechaInicio("15/13/2030"))
        assertNull(EventoValidator.validarFechaInicio("15/03/2030"))
        assertNull(EventoValidator.validarFechaInicio(" 15/03/2030 "))
    }

    @Test
    fun fecha_termino() {
        assertEquals("La fecha de término es obligatoria", EventoValidator.validarFechaTermino("15/03/2030", ""))
        assertNotNull(EventoValidator.validarFechaTermino("15/03/2030", "mañana"))
        assertEquals(
            "No puede ser anterior a la fecha de inicio",
            EventoValidator.validarFechaTermino("15/03/2030", "14/03/2030")
        )
        assertNull(EventoValidator.validarFechaTermino("15/03/2030", "15/03/2030"))
        assertNull(EventoValidator.validarFechaTermino("15/03/2030", "20/03/2030"))
        // Si el inicio es inválido, no se compara (ese error se muestra en su propio campo)
        assertNull(EventoValidator.validarFechaTermino("xx", "20/03/2030"))
    }

    @Test
    fun horario() {
        assertEquals("El horario es obligatorio", EventoValidator.validarHorario(""))
        assertNotNull(EventoValidator.validarHorario("9:30"))
        assertNotNull(EventoValidator.validarHorario("24:00"))
        assertNotNull(EventoValidator.validarHorario("12:60"))
        assertNull(EventoValidator.validarHorario("09:30"))
        assertNull(EventoValidator.validarHorario("23:59"))
    }

    @Test
    fun comuna_y_region() {
        assertEquals("La comuna es obligatoria", EventoValidator.validarComuna(""))
        assertEquals("Solo se permiten letras y espacios", EventoValidator.validarComuna("Maipú2"))
        assertEquals("Debe tener al menos 3 caracteres", EventoValidator.validarComuna("Ab"))
        assertNull(EventoValidator.validarComuna("Puente Alto"))

        assertEquals("La región es obligatoria", EventoValidator.validarRegion(""))
        assertEquals("Debe tener al menos 3 caracteres", EventoValidator.validarRegion("RM"))
        assertNull(EventoValidator.validarRegion("Metropolitana"))
    }

    @Test
    fun organizacion() {
        assertEquals("La organización es obligatoria", EventoValidator.validarOrganizacion(""))
        assertEquals("Debe tener al menos 3 caracteres", EventoValidator.validarOrganizacion("ab"))
        assertEquals("Máximo 80 caracteres", EventoValidator.validarOrganizacion("a".repeat(81)))
        assertNull(EventoValidator.validarOrganizacion("Acme"))
    }

    @Test
    fun asistentes() {
        assertEquals("El público esperado es obligatorio", EventoValidator.validarAsistentes(""))
        assertEquals("Ingresa solo números", EventoValidator.validarAsistentes("abc"))
        assertEquals("Debe ser mayor a 0", EventoValidator.validarAsistentes("0"))
        assertEquals("Debe ser mayor a 0", EventoValidator.validarAsistentes("-5"))
        assertEquals("El valor es demasiado alto", EventoValidator.validarAsistentes("100001"))
        assertNull(EventoValidator.validarAsistentes("150"))
    }

    @Test
    fun costo() {
        assertEquals("El costo referencial es obligatorio (puede ser 0)", EventoValidator.validarCosto(""))
        assertEquals("Ingresa solo números, sin puntos ni símbolos", EventoValidator.validarCosto("1.000"))
        assertEquals("No puede ser negativo", EventoValidator.validarCosto("-1"))
        assertEquals("El valor es demasiado alto", EventoValidator.validarCosto("100000001"))
        assertNull(EventoValidator.validarCosto("0"))
        assertNull(EventoValidator.validarCosto("50000"))
    }

    @Test
    fun condiciones_es_opcional_con_maximo() {
        assertNull(EventoValidator.validarCondiciones(""))
        assertEquals("Máximo 200 caracteres", EventoValidator.validarCondiciones("a".repeat(201)))
    }
}