package com.example.proyecto_evaluacion.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OportunidadValidatorTest {

    @Test
    fun nombre() {
        assertEquals("El nombre es obligatorio", OportunidadValidator.validarNombre(""))
        assertEquals("Debe tener al menos 3 caracteres", OportunidadValidator.validarNombre("ab"))
        assertEquals("Máximo 60 caracteres", OportunidadValidator.validarNombre("a".repeat(61)))
        assertNull(OportunidadValidator.validarNombre("Feria de beneficios"))
    }

    @Test
    fun descripcion() {
        assertEquals("La descripción es obligatoria", OportunidadValidator.validarDescripcion(""))
        assertEquals("Debe tener al menos 10 caracteres", OportunidadValidator.validarDescripcion("corta"))
        assertEquals("Máximo 200 caracteres", OportunidadValidator.validarDescripcion("a".repeat(201)))
        assertNull(OportunidadValidator.validarDescripcion("Descripción suficiente"))
    }

    @Test
    fun selectores_obligatorios() {
        assertEquals("Selecciona el tipo de evento", OportunidadValidator.validarTipo(""))
        assertNull(OportunidadValidator.validarTipo("Feria empresarial"))
        assertEquals("Selecciona la prioridad", OportunidadValidator.validarPrioridad(""))
        assertNull(OportunidadValidator.validarPrioridad("ALTA"))
        assertEquals("Selecciona una organización", OportunidadValidator.validarOrganizacion(""))
        assertNull(OportunidadValidator.validarOrganizacion("Acme"))
    }

    @Test
    fun lugar() {
        assertEquals("El lugar es obligatorio", OportunidadValidator.validarLugar(""))
        assertEquals("Debe tener al menos 3 caracteres", OportunidadValidator.validarLugar("ab"))
        assertEquals("Máximo 80 caracteres", OportunidadValidator.validarLugar("a".repeat(81)))
        assertNull(OportunidadValidator.validarLugar("Centro de eventos"))
    }

    @Test
    fun reutiliza_las_reglas_de_EventoValidator() {
        assertNotNull(OportunidadValidator.validarFechaInicio("mal"))
        assertNull(OportunidadValidator.validarFechaInicio("15/03/2030"))
        assertNotNull(OportunidadValidator.validarFechaTermino("15/03/2030", "10/03/2030"))
        assertNull(OportunidadValidator.validarFechaTermino("15/03/2030", "16/03/2030"))
        assertNotNull(OportunidadValidator.validarHorario("99:99"))
        assertNull(OportunidadValidator.validarHorario("10:00"))
        assertNotNull(OportunidadValidator.validarPublico("0"))
        assertNull(OportunidadValidator.validarPublico("10"))
        assertNotNull(OportunidadValidator.validarCosto("-3"))
        assertNull(OportunidadValidator.validarCosto("0"))
    }
}