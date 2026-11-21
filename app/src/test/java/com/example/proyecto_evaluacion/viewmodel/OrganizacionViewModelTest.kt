package com.example.proyecto_evaluacion.viewmodel

import com.example.proyecto_evaluacion.MainDispatcherRule
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.Organizacion
import com.example.proyecto_evaluacion.mantenerActivo
import com.example.proyecto_evaluacion.organizacion
import io.mockk.Runs
import io.mockk.every
import io.mockk.coEvery
import io.mockk.coVerify
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

class OrganizacionViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<CrmRepository>()
    private lateinit var vm: OrganizacionViewModel

    @Before
    fun setUp() {
        every { repo.getOrganizaciones() } returns flowOf(listOf(organizacion(1, "Acme SpA")))
        vm = OrganizacionViewModel(repo)
    }

    private fun llenarFormularioValido() {
        vm.cambiarNombre("  Acme Ltda ")
        vm.cambiarTipo("empresa")
        vm.cambiarRubro("Retail")
        vm.cambiarComuna("Santiago")
        vm.cambiarRegion("Metropolitana")
        vm.cambiarSitioWeb("www.acme.cl")
        vm.cambiarObservaciones("Cliente frecuente")
    }

    @Test
    fun cada_campo_se_valida_al_cambiar() {
        vm.cambiarNombre("")
        vm.cambiarTipo("Banco")
        vm.cambiarRubro("")
        vm.cambiarComuna("")
        vm.cambiarRegion("")
        vm.cambiarSitioWeb("sin punto")
        vm.cambiarObservaciones("a".repeat(201))

        val estado = vm.uiState.value
        assertNotNull(estado.errorNombre)
        assertNotNull(estado.errorTipo)
        assertNotNull(estado.errorRubro)
        assertNotNull(estado.errorComuna)
        assertNotNull(estado.errorRegion)
        assertNotNull(estado.errorSitioWeb)
        assertNotNull(estado.errorObservaciones)

        llenarFormularioValido()
        val corregido = vm.uiState.value
        assertNull(corregido.errorNombre)
        assertNull(corregido.errorTipo)
        assertNull(corregido.errorRubro)
        assertNull(corregido.errorComuna)
        assertNull(corregido.errorRegion)
        assertNull(corregido.errorSitioWeb)
        assertNull(corregido.errorObservaciones)
    }

    @Test
    fun guardar_formulario_vacio_muestra_errores_y_no_llama_al_servidor() {
        vm.guardar()

        assertNotNull(vm.uiState.value.errorNombre)
        assertNotNull(vm.uiState.value.errorTipo)
        assertFalse(vm.uiState.value.guardadoExitoso)
        coVerify(exactly = 0) { repo.guardarOrganizacion(any()) }
    }

    @Test
    fun guardar_formulario_valido_envia_datos_limpios_y_reinicia_el_formulario() {
        val enviada = slot<Organizacion>()
        coEvery { repo.guardarOrganizacion(capture(enviada)) } just Runs
        llenarFormularioValido()

        vm.guardar()

        assertEquals("Acme Ltda", enviada.captured.nombre)
        assertEquals("Empresa", enviada.captured.tipo) // escritura oficial de la lista
        assertEquals("www.acme.cl", enviada.captured.sitioWeb)
        assertTrue(vm.uiState.value.guardadoExitoso)
        assertEquals("", vm.uiState.value.nombre)
    }

    @Test
    fun si_falla_el_servidor_se_muestra_error_y_se_conservan_los_datos() {
        coEvery { repo.guardarOrganizacion(any()) } throws RuntimeException("falló")
        llenarFormularioValido()

        vm.guardar()

        assertEquals(
            "No se pudo guardar la organización, intenta de nuevo",
            vm.uiState.value.errorOperacion
        )
        assertFalse(vm.uiState.value.guardadoExitoso)
        assertEquals("  Acme Ltda ", vm.uiState.value.nombre)
    }

    @Test
    fun expone_la_lista_de_organizaciones() = runTest {
        mantenerActivo(vm.organizaciones)

        assertEquals(listOf("Acme SpA"), vm.organizaciones.value.map { it.nombre })
    }
}