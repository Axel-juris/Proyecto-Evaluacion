package com.example.proyecto_evaluacion.viewmodel

import com.example.proyecto_evaluacion.MainDispatcherRule
import com.example.proyecto_evaluacion.SUPERVISOR
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.ErrorAplicacion
import com.example.proyecto_evaluacion.data.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<CrmRepository>()
    private lateinit var vm: LoginViewModel

    @Before
    fun setUp() {
        SessionManager.cerrarSesion()
        vm = LoginViewModel(repo)
    }

    @After
    fun tearDown() = SessionManager.cerrarSesion()

    @Test
    fun cambiar_correo_valida_en_el_acto() {
        vm.cambiarCorreo("mal")
        assertNotNull(vm.uiState.value.errorCorreo)
        vm.cambiarCorreo("ana@duoc.cl")
        assertNull(vm.uiState.value.errorCorreo)
        assertEquals("ana@duoc.cl", vm.uiState.value.correo)
    }

    @Test
    fun cambiar_clave_valida_en_el_acto() {
        vm.cambiarClave("123")
        assertNotNull(vm.uiState.value.errorClave)
        vm.cambiarClave("123456")
        assertNull(vm.uiState.value.errorClave)
    }

    @Test
    fun ingresar_con_datos_invalidos_no_llama_al_servidor() {
        var navego = false

        vm.ingresar { navego = true }

        assertNotNull(vm.uiState.value.errorCorreo)
        assertNotNull(vm.uiState.value.errorClave)
        assertFalse(navego)
        assertFalse(vm.uiState.value.cargando)
        coVerify(exactly = 0) { repo.login(any(), any()) }
    }

    @Test
    fun ingreso_correcto_inicia_sesion_y_navega() {
        coEvery { repo.login("sofia@duoc.cl", "secreto1") } returns SUPERVISOR
        var navego = false
        vm.cambiarCorreo("sofia@duoc.cl")
        vm.cambiarClave("secreto1")

        vm.ingresar { navego = true }

        assertTrue(navego)
        assertEquals(SUPERVISOR, SessionManager.usuarioActual.value)
        assertFalse(vm.uiState.value.cargando)
        assertEquals("", vm.uiState.value.clave)
        assertNull(vm.uiState.value.errorLogin)
    }

    @Test
    fun credenciales_incorrectas_muestran_error() {
        coEvery { repo.login(any(), any()) } returns null
        var navego = false
        vm.cambiarCorreo("sofia@duoc.cl")
        vm.cambiarClave("incorrecta")

        vm.ingresar { navego = true }

        assertFalse(navego)
        assertEquals("Correo o clave incorrectos", vm.uiState.value.errorLogin)
        assertFalse(vm.uiState.value.cargando)
        assertNull(SessionManager.usuarioActual.value)
    }

    @Test
    fun error_de_conexion_se_muestra_con_el_mensaje_del_servidor() {
        coEvery { repo.login(any(), any()) } throws ErrorAplicacion(null, "No hay conexión")
        vm.cambiarCorreo("sofia@duoc.cl")
        vm.cambiarClave("secreto1")

        vm.ingresar { }

        assertEquals("No hay conexión", vm.uiState.value.errorLogin)
        assertFalse(vm.uiState.value.cargando)
    }

    @Test
    fun excepcion_sin_mensaje_usa_texto_por_defecto() {
        coEvery { repo.login(any(), any()) } throws RuntimeException()
        vm.cambiarCorreo("sofia@duoc.cl")
        vm.cambiarClave("secreto1")

        vm.ingresar { }

        assertEquals("No se pudo iniciar sesión", vm.uiState.value.errorLogin)
    }

    @Test
    fun editar_un_campo_limpia_el_error_de_login() {
        coEvery { repo.login(any(), any()) } returns null
        vm.cambiarCorreo("sofia@duoc.cl")
        vm.cambiarClave("incorrecta")
        vm.ingresar { }
        assertNotNull(vm.uiState.value.errorLogin)

        vm.cambiarClave("otra-clave")

        assertNull(vm.uiState.value.errorLogin)
    }
}