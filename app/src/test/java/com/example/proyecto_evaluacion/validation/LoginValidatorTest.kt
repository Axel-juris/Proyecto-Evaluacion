package com.example.proyecto_evaluacion.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginValidatorTest {

    @Test
    fun correo_valido_no_tiene_error() {
        assertNull(LoginValidator.validarCorreo("ana@duoc.cl"))
        assertNull(LoginValidator.validarCorreo("  ana@duoc.cl"))
    }

    @Test
    fun correo_vacio_con_espacios_o_formato_invalido() {
        assertEquals("El correo es obligatorio", LoginValidator.validarCorreo(""))
        assertEquals("El correo es obligatorio", LoginValidator.validarCorreo("   "))
        assertEquals("No debe contener espacios", LoginValidator.validarCorreo("ana @duoc.cl"))
        assertEquals("Formato inválido, por ejemplo ana@duoc.cl", LoginValidator.validarCorreo("ana"))
        assertEquals("Formato inválido, por ejemplo ana@duoc.cl", LoginValidator.validarCorreo("ana@duoc"))
    }

    @Test
    fun clave_se_valida_por_largo_minimo() {
        assertEquals("La clave es obligatoria", LoginValidator.validarClave(""))
        assertEquals("Debe tener al menos 6 caracteres", LoginValidator.validarClave("12345"))
        assertNull(LoginValidator.validarClave("123456"))
    }
}