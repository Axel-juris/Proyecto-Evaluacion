package com.example.proyecto_evaluacion.viewmodel

import com.example.proyecto_evaluacion.MainDispatcherRule
import com.example.proyecto_evaluacion.contacto
import com.example.proyecto_evaluacion.data.Contacto
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.mantenerActivo
import com.example.proyecto_evaluacion.organizacion
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ContactoViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<CrmRepository>()
    private lateinit var vm: ContactoViewModel

    @Before
    fun setUp() {
        every { repo.getOrganizaciones() } returns flowOf(listOf(organizacion(1, "Acme SpA")))
        every { repo.getContactos(1) } returns flowOf(listOf(contacto(7, 1)))
        vm = ContactoViewModel(repo)
    }

    private fun llenarFormularioValido() {
        vm.seleccionarOrganizacion(1, "Acme SpA")
        vm.cambiarNombre(" Ana Pérez ")
        vm.cambiarCargo("Jefa de Eventos")
        vm.cambiarArea("eventos")
        vm.cambiarCorreo("ana@acme.cl")
        vm.cambiarTelefono("+56912345678")
        vm.cambiarMedio("Correo")
        vm.cambiarObservaciones("Prefiere la mañana")
    }

    @Test
    fun cada_campo_se_valida_al_cambiar() {
        vm.seleccionarOrganizacion(1, "")
        vm.cambiarNombre("")
        vm.cambiarCargo("")
        vm.cambiarArea("Cocina")
        vm.cambiarCorreo("mal")
        vm.cambiarTelefono("1")
        vm.cambiarMedio("Paloma")
        vm.cambiarObservaciones("a".repeat(201))

        val e = vm.uiState.value
        assertNotNull(e.errorOrganizacion)
        assertNotNull(e.errorNombre)
        assertNotNull(e.errorCargo)
        assertNotNull(e.errorArea)
        assertNotNull(e.errorCorreo)
        assertNotNull(e.errorTelefono)
        assertNotNull(e.errorMedio)
        assertNotNull(e.errorObservaciones)

        llenarFormularioValido()
        val ok = vm.uiState.value
        assertNull(ok.errorOrganizacion)
        assertNull(ok.errorNombre)
        assertNull(ok.errorCargo)
        assertNull(ok.errorArea)
        assertNull(ok.errorCorreo)
        assertNull(ok.errorTelefono)
        assertNull(ok.errorMedio)
        assertNull(ok.errorObservaciones)
    }

    @Test
    fun guardar_formulario_vacio_no_llama_al_servidor() {
        vm.guardar()

        assertNotNull(vm.uiState.value.errorNombre)
        assertFalse(vm.uiState.value.guardadoExitoso)
        coVerify(exactly = 0) { repo.guardarContacto(any()) }
    }

    @Test
    fun guardar_valido_envia_datos_limpios_y_conserva_la_organizacion() {
        val enviado = slot<Contacto>()
        coEvery { repo.guardarContacto(capture(enviado)) } just Runs
        llenarFormularioValido()

        vm.guardar()

        assertEquals(1, enviado.captured.organizacionId)
        assertEquals("Ana Pérez", enviado.captured.nombre)
        assertEquals("ana@acme.cl", enviado.captured.correo)
        assertTrue(vm.uiState.value.guardadoExitoso)
        assertEquals("", vm.uiState.value.nombre)
        assertEquals(1, vm.uiState.value.organizacionId)
        assertEquals("Acme SpA", vm.uiState.value.organizacionNombre)
    }

    @Test
    fun si_falla_el_servidor_se_muestra_error() {
        coEvery { repo.guardarContacto(any()) } throws RuntimeException("falló")
        llenarFormularioValido()

        vm.guardar()

        assertEquals("No se pudo guardar el contacto, intenta de nuevo", vm.uiState.value.errorOperacion)
        assertFalse(vm.uiState.value.guardadoExitoso)
    }

    @Test
    fun lista_los_contactos_de_la_organizacion_seleccionada() = runTest {
        mantenerActivo(vm.contactos)
        assertTrue(vm.contactos.value.isEmpty())

        vm.seleccionarOrganizacion(1, "Acme SpA")

        assertEquals(listOf(7), vm.contactos.value.map { it.id })
    }

    @Test
    fun expone_las_organizaciones_para_el_menu() = runTest {
        mantenerActivo(vm.organizaciones)

        assertEquals(1, vm.organizaciones.value.size)
    }
}