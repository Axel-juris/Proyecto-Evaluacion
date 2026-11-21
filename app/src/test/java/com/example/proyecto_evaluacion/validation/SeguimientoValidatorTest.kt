package com.example.proyecto_evaluacion.validation

import com.example.proyecto_evaluacion.fechaRelativa
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SeguimientoValidatorTest {

    @Test
    fun etiqueta_del_tipo_de_gestion() {
        assertEquals("Llamada", SeguimientoValidator.etiquetaTipo("LLAMADA"))
        assertEquals("Reunión", SeguimientoValidator.etiquetaTipo("REUNION"))
        assertEquals("DESCONOCIDO", SeguimientoValidator.etiquetaTipo("DESCONOCIDO"))
    }

    @Test
    fun tipo() {
        assertEquals("Selecciona el tipo de gestión", SeguimientoValidator.validarTipo(""))
        assertNull(SeguimientoValidator.validarTipo("LLAMADA"))
    }

    @Test
    fun observacion() {
        assertEquals("La observación es obligatoria", SeguimientoValidator.validarObservacion(""))
        assertEquals("Debe tener al menos 5 caracteres", SeguimientoValidator.validarObservacion("hola"))
        assertEquals("Máximo 200 caracteres", SeguimientoValidator.validarObservacion("a".repeat(201)))
        assertNull(SeguimientoValidator.validarObservacion("Se llamó al contacto"))
    }

    @Test
    fun proxima_fecha_es_opcional() {
        assertNull(SeguimientoValidator.validarProximaFecha(""))
        assertNull(SeguimientoValidator.validarProximaFecha("   "))
    }

    @Test
    fun proxima_fecha_con_formato_invalido() {
        assertNotNull(SeguimientoValidator.validarProximaFecha("2030-03-15"))
        assertNotNull(SeguimientoValidator.validarProximaFecha("31/02/2030"))
    }

    @Test
    fun proxima_fecha_no_puede_ser_anterior_a_hoy() {
        assertEquals("No puede ser anterior a hoy", SeguimientoValidator.validarProximaFecha(fechaRelativa(-1)))
        assertNull(SeguimientoValidator.validarProximaFecha(fechaRelativa(0)))
        assertNull(SeguimientoValidator.validarProximaFecha(fechaRelativa(3)))
    }
}