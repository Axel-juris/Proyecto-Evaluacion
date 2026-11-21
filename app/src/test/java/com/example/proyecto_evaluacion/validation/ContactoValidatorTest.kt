package com.example.proyecto_evaluacion.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ContactoValidatorTest {

    @Test
    fun nombre() {
        assertEquals("El nombre es obligatorio", ContactoValidator.validarNombre(" "))
        assertEquals("Debe tener al menos 3 caracteres", ContactoValidator.validarNombre("Al"))
        assertEquals("Solo se permiten letras y espacios", ContactoValidator.validarNombre("Ana123"))
        assertEquals("Máximo 60 caracteres", ContactoValidator.validarNombre("a".repeat(61)))
        assertNull(ContactoValidator.validarNombre("Ana Pérez"))
    }

    @Test
    fun organizacion() {
        assertEquals("La organización es obligatoria", ContactoValidator.validarOrganizacion(""))
        assertEquals("Debe tener al menos 3 caracteres", ContactoValidator.validarOrganizacion("ab"))
        assertEquals("Máximo 80 caracteres", ContactoValidator.validarOrganizacion("a".repeat(81)))
        assertNull(ContactoValidator.validarOrganizacion("Acme SpA"))
    }

    @Test
    fun cargo() {
        assertEquals("El cargo es obligatorio", ContactoValidator.validarCargo(""))
        assertEquals("Debe tener al menos 3 caracteres", ContactoValidator.validarCargo("ab"))
        assertEquals("Máximo 60 caracteres", ContactoValidator.validarCargo("a".repeat(61)))
        assertNull(ContactoValidator.validarCargo("Jefa de Eventos"))
    }

    @Test
    fun area() {
        assertEquals("El área es obligatoria", ContactoValidator.validarArea(""))
        assertNotNull(ContactoValidator.validarArea("Cocina"))
        assertNull(ContactoValidator.validarArea("eventos"))
        assertNull(ContactoValidator.validarArea(" Recursos Humanos "))
    }

    @Test
    fun correo() {
        assertEquals("El correo es obligatorio", ContactoValidator.validarCorreo(""))
        assertEquals("No debe contener espacios", ContactoValidator.validarCorreo("a b@c.cl"))
        assertNotNull(ContactoValidator.validarCorreo("sin-arroba.cl"))
        assertNull(ContactoValidator.validarCorreo("ana@acme.cl"))
    }

    @Test
    fun telefono() {
        assertEquals("El teléfono es obligatorio", ContactoValidator.validarTelefono(""))
        assertNotNull(ContactoValidator.validarTelefono("12345"))
        assertNotNull(ContactoValidator.validarTelefono("abc123456789"))
        assertNull(ContactoValidator.validarTelefono("912345678"))
        assertNull(ContactoValidator.validarTelefono("+56912345678"))
    }

    @Test
    fun medio() {
        assertEquals("El medio preferente es obligatorio", ContactoValidator.validarMedio(""))
        assertEquals("Escribe Correo o Teléfono", ContactoValidator.validarMedio("Paloma"))
        assertNull(ContactoValidator.validarMedio("correo"))
        assertNull(ContactoValidator.validarMedio("Teléfono"))
    }

    @Test
    fun observaciones_es_opcional_con_maximo() {
        assertNull(ContactoValidator.validarObservaciones(""))
        assertNull(ContactoValidator.validarObservaciones("a".repeat(200)))
        assertEquals("Máximo 200 caracteres", ContactoValidator.validarObservaciones("a".repeat(201)))
    }
}