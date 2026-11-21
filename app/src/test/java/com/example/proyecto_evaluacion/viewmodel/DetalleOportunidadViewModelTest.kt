package com.example.proyecto_evaluacion.viewmodel

import android.app.Application
import com.example.proyecto_evaluacion.EJECUTIVO
import com.example.proyecto_evaluacion.MainDispatcherRule
import com.example.proyecto_evaluacion.OTRO_EJECUTIVO
import com.example.proyecto_evaluacion.SUPERVISOR
import com.example.proyecto_evaluacion.contacto
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.Seguimiento
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.data.Usuario
import com.example.proyecto_evaluacion.esperarHasta
import com.example.proyecto_evaluacion.fechaRelativa
import com.example.proyecto_evaluacion.fechas.Evidencias
import com.example.proyecto_evaluacion.mantenerActivo
import com.example.proyecto_evaluacion.oportunidad
import com.example.proyecto_evaluacion.organizacion
import com.example.proyecto_evaluacion.seguimiento
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
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

class DetalleOportunidadViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<CrmRepository>()
    private val app = mockk<Application>(relaxed = true)

    private val usuarios = listOf(SUPERVISOR, EJECUTIVO, OTRO_EJECUTIVO)

    @Before
    fun setUp() {
        SessionManager.cerrarSesion()
        mockkObject(Evidencias)
        every { Evidencias.eliminar(any(), any()) } just Runs
        every { repo.getOrganizaciones() } returns flowOf(listOf(organizacion(1, "Acme SpA")))
        every { repo.getUsuarios() } returns flowOf(usuarios)
        every { repo.getTodosContactos() } returns flowOf(listOf(contacto(7, 1)))
        every { repo.observarOportunidad(5) } returns flowOf(oportunidad(id = 5, contactoId = 7))
        every { repo.getSeguimientos(5) } returns flowOf(listOf(seguimiento(id = 1, ejecutivoId = 2)))
    }

    @After
    fun tearDown() {
        unmockkAll()
        SessionManager.cerrarSesion()
    }

    /** Crea el ViewModel con la sesión indicada, carga la oportunidad 5 y mantiene el estado activo. */
    private fun TestScope.abrirDetalle(usuario: Usuario?): DetalleOportunidadViewModel {
        usuario?.let { SessionManager.iniciarSesion(it) }
        val vm = DetalleOportunidadViewModel(app, repo)
        mantenerActivo(vm.uiState)
        vm.cargar(5)
        return vm
    }

    // ---------- Estado de la pantalla ----------

    @Test
    fun antes_de_cargar_esta_cargando() = runTest {
        val vm = DetalleOportunidadViewModel(app, repo)
        mantenerActivo(vm.uiState)

        assertTrue(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.oportunidad)
    }

    @Test
    fun oportunidad_inexistente_deja_de_cargar() = runTest {
        every { repo.observarOportunidad(5) } returns flowOf(null)

        val vm = abrirDetalle(SUPERVISOR)

        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.oportunidad)
        assertTrue(vm.uiState.value.esSupervisor)
    }

    @Test
    fun el_supervisor_ve_todos_los_datos_y_tiene_permisos() = runTest {
        val vm = abrirDetalle(SUPERVISOR)

        val e = vm.uiState.value
        assertEquals(5, e.oportunidad?.id)
        assertEquals("Acme SpA", e.organizacionNombre)
        assertEquals("Ana Pérez (Jefa de Eventos)", e.contactoNombre)
        assertEquals("Eva Ejecutiva", e.responsable)
        assertEquals(listOf(2, 3), e.ejecutivos.map { it.id })
        assertEquals("Eva Ejecutiva", e.seguimientos.single().autor)
        assertTrue(e.esSupervisor)
        assertTrue(e.autorizado)
        assertTrue(e.puedeEditar)
        assertTrue(e.puedeEliminar)
        assertEquals(listOf("EN_GESTION", "DESCARTADA"), e.transicionesPermitidas)
    }

    @Test
    fun el_ejecutivo_asignado_esta_autorizado_con_permisos_limitados() = runTest {
        val vm = abrirDetalle(EJECUTIVO)

        val e = vm.uiState.value
        assertTrue(e.autorizado)
        assertFalse(e.esSupervisor)
        assertTrue(e.puedeEditar)
        assertTrue(e.puedeEliminar)
        assertEquals(listOf("EN_GESTION"), e.transicionesPermitidas)
    }

    @Test
    fun un_ejecutivo_ajeno_no_esta_autorizado() = runTest {
        val vm = abrirDetalle(OTRO_EJECUTIVO)

        val e = vm.uiState.value
        assertFalse(e.autorizado)
        assertFalse(e.puedeEditar)
        assertTrue(e.transicionesPermitidas.isEmpty())
    }

    @Test
    fun datos_faltantes_muestran_valores_por_defecto() = runTest {
        every { repo.observarOportunidad(5) } returns flowOf(
            oportunidad(id = 5, organizacionId = 99, contactoId = null, ejecutivoId = null)
        )
        every { repo.getSeguimientos(5) } returns flowOf(listOf(seguimiento(id = 1, ejecutivoId = 404)))

        val vm = abrirDetalle(SUPERVISOR)

        val e = vm.uiState.value
        assertEquals("Sin organización", e.organizacionNombre)
        assertEquals("Sin contacto", e.contactoNombre)
        assertEquals("Sin asignar", e.responsable)
        assertEquals("Desconocido", e.seguimientos.single().autor)
    }

    @Test
    fun informar_y_limpiar_mensajes() = runTest {
        val vm = abrirDetalle(SUPERVISOR)

        vm.informar("listo", null)
        assertEquals("listo", vm.uiState.value.form.mensajeExito)
        vm.informar(null, "falló")
        assertEquals("falló", vm.uiState.value.form.mensajeError)
        assertNull(vm.uiState.value.form.mensajeExito)

        vm.limpiarMensajes()
        assertNull(vm.uiState.value.form.mensajeError)
        assertNull(vm.uiState.value.form.mensajeExito)
    }

    // ---------- Formulario de gestión ----------

    @Test
    fun el_formulario_de_gestion_valida_al_cambiar() = runTest {
        val vm = abrirDetalle(EJECUTIVO)

        vm.cambiarTipoGestion("")
        vm.cambiarObservacion("hi")
        vm.cambiarProximaFecha(fechaRelativa(-1))
        val e = vm.uiState.value.form
        assertNotNull(e.errorTipo)
        assertNotNull(e.errorObservacion)
        assertNotNull(e.errorProximaFecha)

        vm.cambiarTipoGestion("LLAMADA")
        vm.cambiarObservacion("Se llamó al contacto")
        vm.cambiarProximaFecha(fechaRelativa(2))
        val ok = vm.uiState.value.form
        assertNull(ok.errorTipo)
        assertNull(ok.errorObservacion)
        assertNull(ok.errorProximaFecha)
    }

    @Test
    fun guardar_seguimiento_invalido_no_llama_al_servidor() = runTest {
        val vm = abrirDetalle(EJECUTIVO)

        vm.guardarSeguimiento()

        assertNotNull(vm.uiState.value.form.errorTipo)
        assertNotNull(vm.uiState.value.form.errorObservacion)
        coVerify(exactly = 0) { repo.guardarSeguimiento(any()) }
    }

    @Test
    fun guardar_seguimiento_valido_lo_envia_y_limpia_el_formulario() = runTest {
        val enviado = slot<Seguimiento>()
        coEvery { repo.guardarSeguimiento(capture(enviado)) } just Runs
        val vm = abrirDetalle(EJECUTIVO)
        vm.cambiarTipoGestion("REUNION")
        vm.cambiarObservacion("  Reunión de coordinación  ")
        vm.cambiarProximaFecha(fechaRelativa(7))

        vm.guardarSeguimiento()

        assertEquals(5, enviado.captured.oportunidadId)
        assertEquals(EJECUTIVO.id, enviado.captured.ejecutivoId)
        assertEquals("REUNION", enviado.captured.tipoGestion)
        assertEquals("Reunión de coordinación", enviado.captured.observacion)
        assertEquals(fechaRelativa(7), enviado.captured.proximaGestionFecha)
        val form = vm.uiState.value.form
        assertTrue(form.seguimientoGuardado)
        assertEquals("Gestión registrada correctamente", form.mensajeExito)
        assertEquals("", form.observacion)
    }

    @Test
    fun seguimiento_sin_proxima_fecha_se_guarda_con_null() = runTest {
        val enviado = slot<Seguimiento>()
        coEvery { repo.guardarSeguimiento(capture(enviado)) } just Runs
        val vm = abrirDetalle(EJECUTIVO)
        vm.cambiarTipoGestion("CORREO")
        vm.cambiarObservacion("Se envió correo")

        vm.guardarSeguimiento()

        assertNull(enviado.captured.proximaGestionFecha)
    }

    @Test
    fun error_del_servidor_al_guardar_seguimiento() = runTest {
        coEvery { repo.guardarSeguimiento(any()) } throws RuntimeException("falló")
        val vm = abrirDetalle(EJECUTIVO)
        vm.cambiarTipoGestion("VISITA")
        vm.cambiarObservacion("Visita al local")

        vm.guardarSeguimiento()

        assertEquals("No se pudo guardar la gestión, intenta de nuevo", vm.uiState.value.form.mensajeError)
        assertFalse(vm.uiState.value.form.seguimientoGuardado)
    }

    @Test
    fun un_ejecutivo_ajeno_no_puede_registrar_gestiones() = runTest {
        val vm = abrirDetalle(OTRO_EJECUTIVO)

        vm.guardarSeguimiento()

        assertEquals("No tienes permiso para registrar gestiones aquí", vm.uiState.value.form.mensajeError)
        coVerify(exactly = 0) { repo.guardarSeguimiento(any()) }
    }

    @Test
    fun guardar_seguimiento_sin_sesion_no_hace_nada() = runTest {
        val vm = abrirDetalle(null)

        vm.guardarSeguimiento()

        coVerify(exactly = 0) { repo.guardarSeguimiento(any()) }
    }

    // ---------- Cambio de estado ----------

    @Test
    fun cambio_de_estado_permitido() = runTest {
        coEvery { repo.cambiarEstadoOportunidad(5, "EN_GESTION") } just Runs
        val vm = abrirDetalle(EJECUTIVO)

        vm.cambiarEstado("EN_GESTION")

        coVerify { repo.cambiarEstadoOportunidad(5, "EN_GESTION") }
        assertEquals("Estado actualizado correctamente", vm.uiState.value.form.mensajeExito)
    }

    @Test
    fun cambio_de_estado_no_permitido_se_rechaza_en_el_viewmodel() = runTest {
        val vm = abrirDetalle(EJECUTIVO)

        vm.cambiarEstado("CONFIRMADA")

        assertEquals("No tienes permiso para este cambio de estado", vm.uiState.value.form.mensajeError)
        coVerify(exactly = 0) { repo.cambiarEstadoOportunidad(any(), any()) }
    }

    @Test
    fun error_del_servidor_al_cambiar_estado() = runTest {
        coEvery { repo.cambiarEstadoOportunidad(any(), any()) } throws RuntimeException("falló")
        val vm = abrirDetalle(SUPERVISOR)

        vm.cambiarEstado("DESCARTADA")

        assertEquals("No se pudo actualizar el estado, intenta de nuevo", vm.uiState.value.form.mensajeError)
    }

    @Test
    fun cambiar_estado_sin_oportunidad_no_hace_nada() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        val vm = DetalleOportunidadViewModel(app, repo)
        mantenerActivo(vm.uiState)

        vm.cambiarEstado("EN_GESTION")

        coVerify(exactly = 0) { repo.cambiarEstadoOportunidad(any(), any()) }
    }

    // ---------- Asignar ----------

    @Test
    fun el_supervisor_puede_asignar_responsable() = runTest {
        coEvery { repo.reasignarEjecutivo(5, 3) } just Runs
        val vm = abrirDetalle(SUPERVISOR)

        vm.asignar(3)

        coVerify { repo.reasignarEjecutivo(5, 3) }
        assertEquals("Responsable asignado correctamente", vm.uiState.value.form.mensajeExito)
    }

    @Test
    fun el_ejecutivo_no_puede_asignar() = runTest {
        val vm = abrirDetalle(EJECUTIVO)

        vm.asignar(3)

        assertEquals("Solo el supervisor puede asignar responsables", vm.uiState.value.form.mensajeError)
        coVerify(exactly = 0) { repo.reasignarEjecutivo(any(), any()) }
    }

    @Test
    fun error_del_servidor_al_asignar() = runTest {
        coEvery { repo.reasignarEjecutivo(any(), any()) } throws RuntimeException("falló")
        val vm = abrirDetalle(SUPERVISOR)

        vm.asignar(3)

        assertEquals("No se pudo asignar el responsable, intenta de nuevo", vm.uiState.value.form.mensajeError)
    }

    // ---------- Eliminar ----------

    @Test
    fun eliminar_oportunidad_permitido() = runTest {
        coEvery { repo.eliminarOportunidad(5) } just Runs
        val vm = abrirDetalle(SUPERVISOR)

        vm.eliminarOportunidad()

        coVerify { repo.eliminarOportunidad(5) }
        assertTrue(vm.uiState.value.form.oportunidadEliminada)
    }

    @Test
    fun eliminar_oportunidad_sin_permiso_se_rechaza() = runTest {
        every { repo.observarOportunidad(5) } returns flowOf(oportunidad(id = 5, estado = "EN_GESTION"))
        val vm = abrirDetalle(EJECUTIVO) // el ejecutivo solo elimina mientras esté NUEVA

        vm.eliminarOportunidad()

        assertEquals("No tienes permiso para eliminar esta oportunidad", vm.uiState.value.form.mensajeError)
        coVerify(exactly = 0) { repo.eliminarOportunidad(any()) }
    }

    @Test
    fun error_del_servidor_al_eliminar() = runTest {
        coEvery { repo.eliminarOportunidad(any()) } throws RuntimeException("falló")
        val vm = abrirDetalle(SUPERVISOR)

        vm.eliminarOportunidad()

        assertEquals("No se pudo eliminar la oportunidad, intenta de nuevo", vm.uiState.value.form.mensajeError)
        assertFalse(vm.uiState.value.form.oportunidadEliminada)
    }

    // ---------- Foto de evidencia ----------

    @Test
    fun guardar_foto_sin_permiso_borra_el_archivo_nuevo() = runTest {
        val vm = abrirDetalle(OTRO_EJECUTIVO)

        vm.guardarFoto("content://nueva.jpg")

        assertEquals(
            "No tienes permiso para registrar evidencias en esta oportunidad",
            vm.uiState.value.form.mensajeError
        )
        verify { Evidencias.eliminar(any(), "content://nueva.jpg") }
        coVerify(exactly = 0) { repo.actualizarFotoOportunidad(any(), any()) }
    }

    @Test
    fun guardar_foto_correcta_actualiza_y_borra_la_anterior() = runTest {
        every { repo.observarOportunidad(5) } returns flowOf(oportunidad(id = 5, fotoUri = "content://vieja.jpg"))
        coEvery { repo.actualizarFotoOportunidad(5, "content://nueva.jpg") } just Runs
        val vm = abrirDetalle(SUPERVISOR)

        vm.guardarFoto("content://nueva.jpg")
        esperarHasta { vm.uiState.value.form.mensajeExito != null }

        coVerify { repo.actualizarFotoOportunidad(5, "content://nueva.jpg") }
        verify { Evidencias.eliminar(any(), "content://vieja.jpg") }
        assertEquals("Evidencia fotográfica guardada correctamente", vm.uiState.value.form.mensajeExito)
    }

    @Test
    fun si_falla_guardar_la_foto_se_borra_el_archivo_nuevo() = runTest {
        coEvery { repo.actualizarFotoOportunidad(any(), any()) } throws RuntimeException("falló")
        val vm = abrirDetalle(SUPERVISOR)

        vm.guardarFoto("content://nueva.jpg")
        esperarHasta { vm.uiState.value.form.mensajeError != null }

        assertEquals("No se pudo guardar la foto, intenta de nuevo", vm.uiState.value.form.mensajeError)
        verify { Evidencias.eliminar(any(), "content://nueva.jpg") }
    }
}