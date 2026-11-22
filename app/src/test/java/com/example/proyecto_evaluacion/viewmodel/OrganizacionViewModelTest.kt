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
    // AGREGAR estos tests DENTRO de la clase OrganizacionViewModelTest existente
// (antes de la última llave "}"). No hacen falta imports nuevos.

    @Test
    fun editar_carga_los_datos_de_la_organizacion_en_el_formulario() {
        vm.editar(organizacion(1, "Acme SpA"))

        val e = vm.uiState.value
        assertEquals(1, e.organizacionEditandoId)
        assertEquals("Acme SpA", e.nombre)
        assertEquals("Empresa", e.tipo)
        assertEquals("Retail", e.rubro)
        assertEquals("Santiago", e.comuna)
        assertEquals("Metropolitana", e.region)
        assertNull(e.errorNombre)
    }

    @Test
    fun guardar_en_modo_edicion_actualiza_y_no_crea() {
        val enviada = slot<Organizacion>()
        coEvery { repo.actualizarOrganizacion(capture(enviada)) } just Runs
        vm.editar(organizacion(1, "Acme SpA"))
        vm.cambiarRubro("Servicios")

        vm.guardar()

        assertEquals(1, enviada.captured.id)
        assertEquals("Servicios", enviada.captured.rubro)
        coVerify(exactly = 1) { repo.actualizarOrganizacion(any()) }
        coVerify(exactly = 0) { repo.guardarOrganizacion(any()) }
        val e = vm.uiState.value
        assertTrue(e.guardadoExitoso)
        assertEquals("Organización actualizada correctamente", e.mensajeExito)
        assertNull(e.organizacionEditandoId)
        assertEquals("", e.nombre)
    }

    @Test
    fun si_falla_la_actualizacion_se_muestra_error_y_sigue_en_edicion() {
        coEvery { repo.actualizarOrganizacion(any()) } throws RuntimeException("falló")
        vm.editar(organizacion(1, "Acme SpA"))

        vm.guardar()

        val e = vm.uiState.value
        assertEquals("No se pudo actualizar la organización, intenta de nuevo", e.errorOperacion)
        assertEquals(1, e.organizacionEditandoId)
        assertFalse(e.guardadoExitoso)
    }

    @Test
    fun editar_con_datos_invalidos_no_llama_al_servidor() {
        vm.editar(organizacion(1, "Acme SpA"))
        vm.cambiarNombre("")

        vm.guardar()

        assertNotNull(vm.uiState.value.errorNombre)
        coVerify(exactly = 0) { repo.actualizarOrganizacion(any()) }
    }

    @Test
    fun cancelar_edicion_deja_el_formulario_vacio() {
        vm.editar(organizacion(1, "Acme SpA"))

        vm.cancelarEdicion()

        val e = vm.uiState.value
        assertNull(e.organizacionEditandoId)
        assertEquals("", e.nombre)
        assertEquals("", e.tipo)
    }

    @Test
    fun eliminar_llama_al_repositorio_y_muestra_confirmacion() {
        coEvery { repo.eliminarOrganizacion(1) } just Runs

        vm.eliminar(organizacion(1, "Acme SpA"))

        coVerify(exactly = 1) { repo.eliminarOrganizacion(1) }
        assertEquals("Organización eliminada correctamente", vm.uiState.value.mensajeExito)
        assertNull(vm.uiState.value.errorOperacion)
    }

    @Test
    fun eliminar_la_organizacion_que_se_edita_limpia_el_formulario() {
        coEvery { repo.eliminarOrganizacion(1) } just Runs
        vm.editar(organizacion(1, "Acme SpA"))

        vm.eliminar(organizacion(1, "Acme SpA"))

        val e = vm.uiState.value
        assertNull(e.organizacionEditandoId)
        assertEquals("", e.nombre)
    }

    @Test
    fun si_falla_eliminar_se_muestra_error() {
        coEvery { repo.eliminarOrganizacion(1) } throws RuntimeException("falló")

        vm.eliminar(organizacion(1, "Acme SpA"))

        assertNotNull(vm.uiState.value.errorOperacion)
        assertNull(vm.uiState.value.mensajeExito)
    }
}