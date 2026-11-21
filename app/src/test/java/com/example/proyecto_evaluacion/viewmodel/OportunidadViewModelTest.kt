package com.example.proyecto_evaluacion.viewmodel

import com.example.proyecto_evaluacion.EJECUTIVO
import com.example.proyecto_evaluacion.MainDispatcherRule
import com.example.proyecto_evaluacion.OTRO_EJECUTIVO
import com.example.proyecto_evaluacion.SUPERVISOR
import com.example.proyecto_evaluacion.contacto
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.Oportunidad
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.mantenerActivo
import com.example.proyecto_evaluacion.oportunidad
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OportunidadViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<CrmRepository>()
    private lateinit var vm: OportunidadViewModel

    @Before
    fun setUp() {
        SessionManager.cerrarSesion()
        every { repo.getOrganizaciones() } returns flowOf(listOf(organizacion(1, "Acme SpA")))
        every { repo.getContactos(any()) } returns flowOf(listOf(contacto(7, 1)))
        vm = OportunidadViewModel(repo)
    }

    @After
    fun tearDown() = SessionManager.cerrarSesion()

    private fun llenarFormularioValido() {
        vm.cambiarTitulo("  Feria de beneficios ")
        vm.cambiarDescripcion("Descripción suficientemente larga")
        vm.cambiarTipo("Feria empresarial")
        vm.cambiarPrioridad("ALTA")
        vm.seleccionarOrganizacion(1, "Acme SpA")
        vm.seleccionarContacto(7, "Ana Pérez (Jefa de Eventos)")
        vm.cambiarFechaInicio("15/03/2030")
        vm.cambiarFechaTermino("16/03/2030")
        vm.cambiarHorario("09:30")
        vm.cambiarLugar("Centro de eventos")
        vm.cambiarPublico("150")
        vm.cambiarCosto("50000")
    }

    // ---------- Validación de campos ----------

    @Test
    fun cada_campo_se_valida_al_cambiar() {
        vm.cambiarTitulo("")
        vm.cambiarDescripcion("corta")
        vm.cambiarTipo("")
        vm.cambiarPrioridad("")
        vm.seleccionarOrganizacion(1, "")
        vm.cambiarFechaInicio("mal")
        vm.cambiarFechaTermino("mal")
        vm.cambiarHorario("99:99")
        vm.cambiarLugar("")
        vm.cambiarPublico("0")
        vm.cambiarCosto("-1")

        val e = vm.uiState.value
        assertNotNull(e.errorTitulo)
        assertNotNull(e.errorDescripcion)
        assertNotNull(e.errorTipo)
        assertNotNull(e.errorPrioridad)
        assertNotNull(e.errorOrganizacion)
        assertNotNull(e.errorFechaInicio)
        assertNotNull(e.errorFechaTermino)
        assertNotNull(e.errorHorario)
        assertNotNull(e.errorLugar)
        assertNotNull(e.errorPublico)
        assertNotNull(e.errorCosto)

        llenarFormularioValido()
        val ok = vm.uiState.value
        assertNull(ok.errorTitulo)
        assertNull(ok.errorDescripcion)
        assertNull(ok.errorTipo)
        assertNull(ok.errorPrioridad)
        assertNull(ok.errorOrganizacion)
        assertNull(ok.errorFechaInicio)
        assertNull(ok.errorFechaTermino)
        assertNull(ok.errorHorario)
        assertNull(ok.errorLugar)
        assertNull(ok.errorPublico)
        assertNull(ok.errorCosto)
    }

    @Test
    fun cambiar_la_fecha_de_inicio_revalida_la_fecha_de_termino() {
        vm.cambiarFechaInicio("10/03/2030")
        vm.cambiarFechaTermino("12/03/2030")
        assertNull(vm.uiState.value.errorFechaTermino)

        vm.cambiarFechaInicio("20/03/2030")

        assertNotNull(vm.uiState.value.errorFechaTermino)
    }

    @Test
    fun cambiar_la_fecha_de_inicio_sin_termino_no_marca_error_en_termino() {
        vm.cambiarFechaInicio("10/03/2030")

        assertNull(vm.uiState.value.errorFechaTermino)
    }

    @Test
    fun cambiar_de_organizacion_limpia_el_contacto() {
        vm.seleccionarOrganizacion(1, "Acme SpA")
        vm.seleccionarContacto(7, "Ana Pérez")
        assertEquals(7, vm.uiState.value.contactoId)

        vm.seleccionarOrganizacion(2, "Otra")

        assertNull(vm.uiState.value.contactoId)
        assertEquals("", vm.uiState.value.contactoNombre)
    }

    @Test
    fun expone_organizaciones_y_contactos_de_la_organizacion_elegida() = runTest {
        mantenerActivo(vm.organizaciones)
        mantenerActivo(vm.contactos)
        assertEquals(1, vm.organizaciones.value.size)
        assertTrue(vm.contactos.value.isEmpty())

        vm.seleccionarOrganizacion(1, "Acme SpA")

        assertEquals(listOf(7), vm.contactos.value.map { it.id })
    }

    // ---------- Crear ----------

    @Test
    fun guardar_formulario_vacio_no_llama_al_servidor() {
        SessionManager.iniciarSesion(EJECUTIVO)

        vm.guardar()

        assertNotNull(vm.uiState.value.errorTitulo)
        assertFalse(vm.uiState.value.guardadoExitoso)
        coVerify(exactly = 0) { repo.guardarOportunidad(any()) }
    }

    @Test
    fun ejecutivo_crea_una_oportunidad_asignada_a_si_mismo() {
        val enviada = slot<Oportunidad>()
        coEvery { repo.guardarOportunidad(capture(enviada)) } just Runs
        SessionManager.iniciarSesion(EJECUTIVO)
        llenarFormularioValido()

        vm.guardar()

        val op = enviada.captured
        assertEquals("Feria de beneficios", op.titulo)
        assertEquals("NUEVA", op.estado)
        assertEquals(EJECUTIVO.id, op.ejecutivoId)
        assertEquals(1, op.organizacionId)
        assertEquals(7, op.contactoId)
        assertEquals(50000.0, op.monto, 0.0)
        assertEquals(150, op.publicoEsperado)
        assertTrue(vm.uiState.value.guardadoExitoso)
        assertEquals("", vm.uiState.value.titulo)
    }

    @Test
    fun supervisor_crea_una_oportunidad_sin_asignar() {
        val enviada = slot<Oportunidad>()
        coEvery { repo.guardarOportunidad(capture(enviada)) } just Runs
        SessionManager.iniciarSesion(SUPERVISOR)
        llenarFormularioValido()

        vm.guardar()

        assertNull(enviada.captured.ejecutivoId)
    }

    @Test
    fun sin_sesion_no_se_puede_guardar() {
        llenarFormularioValido()

        vm.guardar()

        assertEquals(
            "Tu sesión no es válida, vuelve a iniciar sesión",
            vm.uiState.value.errorOperacion
        )
        coVerify(exactly = 0) { repo.guardarOportunidad(any()) }
    }

    @Test
    fun si_falla_el_servidor_al_crear_se_muestra_error() {
        coEvery { repo.guardarOportunidad(any()) } throws RuntimeException("falló")
        SessionManager.iniciarSesion(EJECUTIVO)
        llenarFormularioValido()

        vm.guardar()

        assertEquals(
            "No se pudo guardar la oportunidad, intenta de nuevo",
            vm.uiState.value.errorOperacion
        )
        assertFalse(vm.uiState.value.guardadoExitoso)
    }

    // ---------- Editar ----------

    @Test
    fun cargar_para_edicion_rellena_el_formulario() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        coEvery { repo.getOportunidadPorId(5) } returns oportunidad(id = 5, contactoId = 7)

        vm.cargarParaEdicion(5)

        val e = vm.uiState.value
        assertEquals(5, e.editandoId)
        assertFalse(e.cargandoEdicion)
        assertNull(e.mensajeBloqueo)
        assertEquals("Feria de beneficios", e.titulo)
        assertEquals("Acme SpA", e.organizacionNombre)
        assertEquals("Ana Pérez (Jefa de Eventos)", e.contactoNombre)
        assertEquals("100", e.publico)
        assertEquals("1000", e.costo)
        assertEquals("15/03/2030", e.fechaInicio)
    }

    @Test
    fun cargar_para_edicion_sin_contacto_muestra_sin_contacto() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        coEvery { repo.getOportunidadPorId(5) } returns oportunidad(id = 5, contactoId = null)

        vm.cargarParaEdicion(5)

        assertEquals("Sin contacto", vm.uiState.value.contactoNombre)
    }

    @Test
    fun cargar_dos_veces_la_misma_oportunidad_no_vuelve_a_pedirla() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        coEvery { repo.getOportunidadPorId(5) } returns oportunidad(id = 5)

        vm.cargarParaEdicion(5)
        vm.cargarParaEdicion(5)

        coVerify(exactly = 1) { repo.getOportunidadPorId(5) }
    }

    @Test
    fun editar_una_oportunidad_inexistente_la_bloquea() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        coEvery { repo.getOportunidadPorId(99) } returns null

        vm.cargarParaEdicion(99)

        assertEquals("No se encontró la oportunidad", vm.uiState.value.mensajeBloqueo)
        assertFalse(vm.uiState.value.cargandoEdicion)
    }

    @Test
    fun un_ejecutivo_no_puede_editar_la_de_otro() = runTest {
        SessionManager.iniciarSesion(OTRO_EJECUTIVO)
        coEvery { repo.getOportunidadPorId(5) } returns oportunidad(id = 5, ejecutivoId = 2)

        vm.cargarParaEdicion(5)

        assertEquals("No tienes permiso para editar esta oportunidad", vm.uiState.value.mensajeBloqueo)
    }

    @Test
    fun error_al_cargar_para_edicion() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        coEvery { repo.getOportunidadPorId(5) } throws RuntimeException("falló")

        vm.cargarParaEdicion(5)

        assertEquals(
            "No se pudo cargar la oportunidad, intenta de nuevo",
            vm.uiState.value.mensajeBloqueo
        )
    }

    @Test
    fun guardar_en_modo_edicion_actualiza_y_conserva_estado_y_responsable() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        val original = oportunidad(id = 5, estado = "EN_GESTION", ejecutivoId = 2, fotoUri = "content://foto")
        coEvery { repo.getOportunidadPorId(5) } returns original
        val actualizada = slot<Oportunidad>()
        coEvery { repo.actualizarOportunidad(capture(actualizada)) } just Runs
        vm.cargarParaEdicion(5)
        vm.cambiarTitulo("Título corregido")

        vm.guardar()

        assertEquals(5, actualizada.captured.id)
        assertEquals("Título corregido", actualizada.captured.titulo)
        assertEquals("EN_GESTION", actualizada.captured.estado)
        assertEquals(2, actualizada.captured.ejecutivoId)
        assertEquals("content://foto", actualizada.captured.fotoUri)
        assertTrue(vm.uiState.value.guardadoExitoso)
        coVerify(exactly = 0) { repo.guardarOportunidad(any()) }
    }

    @Test
    fun guardar_en_modo_edicion_sin_permiso_no_actualiza() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        coEvery { repo.getOportunidadPorId(5) } returnsMany listOf(
            oportunidad(id = 5),
            null // al guardar, la oportunidad ya no existe
        )
        vm.cargarParaEdicion(5)

        vm.guardar()

        assertEquals(
            "No tienes permiso para editar esta oportunidad",
            vm.uiState.value.errorOperacion
        )
        coVerify(exactly = 0) { repo.actualizarOportunidad(any()) }
    }
}