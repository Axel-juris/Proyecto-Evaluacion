package com.example.proyecto_evaluacion.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OrganizacionValidatorTest {

    @Test
    fun nombre() {
        assertEquals("El nombre es obligatorio", OrganizacionValidator.validarNombre(""))
        assertEquals("Debe tener al menos 3 caracteres", OrganizacionValidator.validarNombre("ab"))
        assertEquals("Máximo 80 caracteres", OrganizacionValidator.validarNombre("a".repeat(81)))
        assertNull(OrganizacionValidator.validarNombre("Acme SpA"))
    }

    @Test
    fun tipo() {
        assertEquals("El tipo de organización es obligatorio", OrganizacionValidator.validarTipo(""))
        assertNotNull(OrganizacionValidator.validarTipo("Banco"))
        assertNull(OrganizacionValidator.validarTipo("empresa"))
    }

    @Test
    fun rubro() {
        assertEquals("El rubro es obligatorio", OrganizacionValidator.validarRubro(""))
        assertEquals("Debe tener al menos 3 caracteres", OrganizacionValidator.validarRubro("ab"))
        assertEquals("Máximo 60 caracteres", OrganizacionValidator.validarRubro("a".repeat(61)))
        assertNull(OrganizacionValidator.validarRubro("Retail"))
    }

    @Test
    fun comuna_y_region() {
        assertEquals("La comuna es obligatoria", OrganizacionValidator.validarComuna(""))
        assertEquals("Solo se permiten letras y espacios", OrganizacionValidator.validarComuna("Maipú1"))
        assertEquals("Debe tener al menos 3 caracteres", OrganizacionValidator.validarComuna("Ab"))
        assertNull(OrganizacionValidator.validarComuna("Las Condes"))

        assertEquals("La región es obligatoria", OrganizacionValidator.validarRegion(""))
        assertEquals("Debe tener al menos 3 caracteres", OrganizacionValidator.validarRegion("RM"))
        assertNull(OrganizacionValidator.validarRegion("Metropolitana"))
    }

    @Test
    fun sitio_web_es_opcional() {
        assertNull(OrganizacionValidator.validarSitioWeb(""))
        assertEquals("No debe contener espacios", OrganizacionValidator.validarSitioWeb("www. acme.cl"))
        assertNotNull(OrganizacionValidator.validarSitioWeb("acme"))
        assertNull(OrganizacionValidator.validarSitioWeb("www.acme.cl"))
    }

    @Test
    fun observaciones_es_opcional_con_maximo() {
        assertNull(OrganizacionValidator.validarObservaciones(""))
        assertEquals("Máximo 200 caracteres", OrganizacionValidator.validarObservaciones("a".repeat(201)))
    }
}