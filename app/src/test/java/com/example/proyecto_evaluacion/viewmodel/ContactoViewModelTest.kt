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
    // AGREGAR estos tests DENTRO de la clase ContactoViewModelTest existente
// (antes de la última llave "}"). No hacen falta imports nuevos.

    @Test
    fun editar_carga_los_datos_del_contacto_en_el_formulario() {
        vm.seleccionarOrganizacion(1, "Acme SpA")

        vm.editar(contacto(7, 1))

        val e = vm.uiState.value
        assertEquals(7, e.contactoEditandoId)
        assertEquals("Ana Pérez", e.nombre)
        assertEquals("Jefa de Eventos", e.cargo)
        assertEquals("Eventos", e.area)
        assertEquals("ana@acme.cl", e.correo)
        assertEquals("Correo", e.medio)
        assertEquals("Acme SpA", e.organizacionNombre)
        assertNull(e.errorNombre)
    }

    @Test
    fun guardar_en_modo_edicion_actualiza_y_no_crea() {
        val enviado = slot<Contacto>()
        coEvery { repo.actualizarContacto(capture(enviado)) } just Runs
        vm.seleccionarOrganizacion(1, "Acme SpA")
        vm.editar(contacto(7, 1))
        vm.cambiarCargo("Directora de Eventos")

        vm.guardar()

        assertEquals(7, enviado.captured.id)
        assertEquals("Directora de Eventos", enviado.captured.cargo)
        coVerify(exactly = 1) { repo.actualizarContacto(any()) }
        coVerify(exactly = 0) { repo.guardarContacto(any()) }
        val e = vm.uiState.value
        assertTrue(e.guardadoExitoso)
        assertEquals("Contacto actualizado correctamente", e.mensajeExito)
        assertNull(e.contactoEditandoId)
        assertEquals("", e.nombre)
        assertEquals(1, e.organizacionId)
    }

    @Test
    fun si_falla_la_actualizacion_se_muestra_error_y_sigue_en_edicion() {
        coEvery { repo.actualizarContacto(any()) } throws RuntimeException("falló")
        vm.seleccionarOrganizacion(1, "Acme SpA")
        vm.editar(contacto(7, 1))

        vm.guardar()

        val e = vm.uiState.value
        assertEquals("No se pudo actualizar el contacto, intenta de nuevo", e.errorOperacion)
        assertEquals(7, e.contactoEditandoId)
        assertFalse(e.guardadoExitoso)
    }

    @Test
    fun editar_con_datos_invalidos_no_llama_al_servidor() {
        vm.seleccionarOrganizacion(1, "Acme SpA")
        vm.editar(contacto(7, 1))
        vm.cambiarCorreo("mal")

        vm.guardar()

        assertNotNull(vm.uiState.value.errorCorreo)
        coVerify(exactly = 0) { repo.actualizarContacto(any()) }
    }

    @Test
    fun cancelar_edicion_limpia_el_formulario_y_conserva_la_organizacion() {
        vm.seleccionarOrganizacion(1, "Acme SpA")
        vm.editar(contacto(7, 1))

        vm.cancelarEdicion()

        val e = vm.uiState.value
        assertNull(e.contactoEditandoId)
        assertEquals("", e.nombre)
        assertEquals(1, e.organizacionId)
        assertEquals("Acme SpA", e.organizacionNombre)
    }

    @Test
    fun cambiar_de_organizacion_abandona_la_edicion() {
        vm.seleccionarOrganizacion(1, "Acme SpA")
        vm.editar(contacto(7, 1))

        vm.seleccionarOrganizacion(2, "Otra SpA")

        assertNull(vm.uiState.value.contactoEditandoId)
    }

    @Test
    fun eliminar_llama_al_repositorio_y_muestra_confirmacion() {
        coEvery { repo.eliminarContacto(7) } just Runs
        vm.seleccionarOrganizacion(1, "Acme SpA")

        vm.eliminar(contacto(7, 1))

        coVerify(exactly = 1) { repo.eliminarContacto(7) }
        assertEquals("Contacto eliminado correctamente", vm.uiState.value.mensajeExito)
        assertNull(vm.uiState.value.errorOperacion)
    }

    @Test
    fun eliminar_el_contacto_que_se_edita_limpia_el_formulario() {
        coEvery { repo.eliminarContacto(7) } just Runs
        vm.seleccionarOrganizacion(1, "Acme SpA")
        vm.editar(contacto(7, 1))

        vm.eliminar(contacto(7, 1))

        val e = vm.uiState.value
        assertNull(e.contactoEditandoId)
        assertEquals("", e.nombre)
        assertEquals(1, e.organizacionId)
    }

    @Test
    fun si_falla_eliminar_se_muestra_error() {
        coEvery { repo.eliminarContacto(7) } throws RuntimeException("falló")
        vm.seleccionarOrganizacion(1, "Acme SpA")

        vm.eliminar(contacto(7, 1))

        assertNotNull(vm.uiState.value.errorOperacion)
        assertNull(vm.uiState.value.mensajeExito)
    }
}