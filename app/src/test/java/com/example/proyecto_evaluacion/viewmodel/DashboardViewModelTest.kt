package com.example.proyecto_evaluacion.viewmodel

import com.example.proyecto_evaluacion.EJECUTIVO
import com.example.proyecto_evaluacion.MainDispatcherRule
import com.example.proyecto_evaluacion.SUPERVISOR
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.ErrorAplicacion
import com.example.proyecto_evaluacion.data.FeriadosRepository
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.data.fechas.Feriado
import com.example.proyecto_evaluacion.fechaRelativa
import com.example.proyecto_evaluacion.mantenerActivo
import com.example.proyecto_evaluacion.oportunidad
import com.example.proyecto_evaluacion.organizacion
import com.example.proyecto_evaluacion.seguimiento
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<CrmRepository>()
    private val feriadosRepo = mockk<FeriadosRepository>()
    private val navidad = Feriado("2030-12-25", "Navidad", "Christmas Day")

    @Before
    fun setUp() {
        SessionManager.cerrarSesion()
        coEvery { feriadosRepo.proximosFeriados(any(), any(), any()) } returns listOf(navidad)
        every { repo.getTodosSeguimientos() } returns flowOf(emptyList())
        every { repo.getOrganizaciones() } returns flowOf(listOf(organizacion(1, "Acme SpA")))
        every { repo.getTodasOportunidades() } returns flowOf(emptyList())
        every { repo.getOportunidadesPorEjecutivo(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() = SessionManager.cerrarSesion()

    // ---------- Feriados (API externa) ----------

    @Test
    fun carga_los_proximos_feriados_al_iniciar() {
        val vm = DashboardViewModel(repo, feriadosRepo)

        val estado = vm.feriados.value
        assertEquals(listOf(navidad), estado.feriados)
        assertFalse(estado.cargando)
        assertNull(estado.error)
    }

    @Test
    fun si_la_api_falla_se_muestra_el_mensaje_sin_romper_la_app() {
        coEvery { feriadosRepo.proximosFeriados(any(), any(), any()) } throws
                ErrorAplicacion(null, "No hay conexión")

        val vm = DashboardViewModel(repo, feriadosRepo)

        assertEquals("No hay conexión", vm.feriados.value.error)
        assertFalse(vm.feriados.value.cargando)
        assertTrue(vm.feriados.value.feriados.isEmpty())
    }

    @Test
    fun error_sin_mensaje_usa_texto_por_defecto() {
        coEvery { feriadosRepo.proximosFeriados(any(), any(), any()) } throws RuntimeException()

        val vm = DashboardViewModel(repo, feriadosRepo)

        assertEquals("No se pudieron cargar los feriados", vm.feriados.value.error)
    }

    @Test
    fun se_puede_reintentar_la_carga_de_feriados() {
        coEvery { feriadosRepo.proximosFeriados(any(), any(), any()) } throws RuntimeException("falló")
        val vm = DashboardViewModel(repo, feriadosRepo)
        assertEquals("falló", vm.feriados.value.error)

        coEvery { feriadosRepo.proximosFeriados(any(), any(), any()) } returns listOf(navidad)
        vm.cargarFeriados()

        assertNull(vm.feriados.value.error)
        assertEquals(listOf(navidad), vm.feriados.value.feriados)
    }

    // ---------- Dashboard según rol ----------

    @Test
    fun el_ejecutivo_consulta_solo_sus_oportunidades() = runTest {
        SessionManager.iniciarSesion(EJECUTIVO)
        val vm = DashboardViewModel(repo, feriadosRepo)
        mantenerActivo(vm.uiState)

        verify { repo.getOportunidadesPorEjecutivo(EJECUTIVO.id) }
        verify(exactly = 0) { repo.getTodasOportunidades() }
        assertFalse(vm.uiState.value.cargando)
    }

    @Test
    fun sin_sesion_el_dashboard_queda_vacio() = runTest {
        val vm = DashboardViewModel(repo, feriadosRepo)
        mantenerActivo(vm.uiState)

        assertEquals(0, vm.uiState.value.totalActivas)
        assertFalse(vm.uiState.value.cargando)
    }

    @Test
    fun calcula_activas_proximos_pendientes_atrasadas_y_sin_gestion() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        every { repo.getTodasOportunidades() } returns flowOf(
            listOf(
                // Activa, evento en 3 días, sin gestiones
                oportunidad(id = 1, titulo = "Sin gestión", estado = "NUEVA", fechaEvento = fechaRelativa(3)),
                // Activa, próxima gestión en 5 días
                oportunidad(id = 2, titulo = "Pendiente", estado = "EN_GESTION", fechaEvento = fechaRelativa(30)),
                // Activa, última gestión con próxima fecha vencida hace 2 días
                oportunidad(id = 3, titulo = "Atrasada", estado = "EN_GESTION", fechaEvento = fechaRelativa(40)),
                // Descartada: no cuenta como activa ni como próxima ni como sin asignar
                oportunidad(id = 4, titulo = "Descartada", estado = "DESCARTADA", ejecutivoId = null, fechaEvento = fechaRelativa(0)),
                // Confirmada sin responsable, evento mañana
                oportunidad(id = 5, titulo = "Confirmada", estado = "CONFIRMADA", ejecutivoId = null, fechaEvento = fechaRelativa(1)),
                // Evento fuera de la ventana de 14 días y organización desconocida
                oportunidad(id = 6, titulo = "Lejana", estado = "CONFIRMADA", organizacionId = 99, fechaEvento = fechaRelativa(60)),
                // Evento de hoy
                oportunidad(id = 7, titulo = "Hoy", estado = "CONFIRMADA", fechaEvento = fechaRelativa(0))
            )
        )
        every { repo.getTodosSeguimientos() } returns flowOf(
            listOf(
                seguimiento(id = 1, oportunidadId = 2, proxima = fechaRelativa(5)),
                seguimiento(id = 2, oportunidadId = 3, proxima = fechaRelativa(-10)),
                seguimiento(id = 3, oportunidadId = 3, proxima = fechaRelativa(-2))
            )
        )
        val vm = DashboardViewModel(repo, feriadosRepo)
        mantenerActivo(vm.uiState)

        val estado = vm.uiState.value
        assertFalse(estado.cargando)
        assertEquals(3, estado.totalActivas)
        assertEquals(1, estado.totalSinAsignar) // la descartada no cuenta

        // Próximos (0..14 días), ordenados por cercanía, sin descartadas
        assertEquals(listOf("Hoy", "Confirmada", "Sin gestión"), estado.proximos.map { it.titulo })
        assertTrue(estado.proximos[0].detalle.endsWith("hoy"))
        assertTrue(estado.proximos[1].detalle.endsWith("mañana"))
        assertTrue(estado.proximos[2].detalle.endsWith("en 3 días"))
        assertEquals("Acme SpA", estado.proximos[0].organizacion)

        assertEquals(listOf("Pendiente"), estado.pendientes.map { it.titulo })
        assertEquals("Próxima gestión: ${fechaRelativa(5)} (en 5 días)", estado.pendientes[0].detalle)

        assertEquals(listOf("Atrasada"), estado.atrasadas.map { it.titulo })
        assertEquals("Gestión atrasada: ${fechaRelativa(-2)} (hace 2 días)", estado.atrasadas[0].detalle)

        assertEquals(listOf("Sin gestión"), estado.sinGestion.map { it.titulo })
        assertEquals("Detectada el 01/01/2030, sin gestiones registradas", estado.sinGestion[0].detalle)
    }

    @Test
    fun gestion_atrasada_hace_un_dia_usa_singular() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        every { repo.getTodasOportunidades() } returns flowOf(
            listOf(oportunidad(id = 1, titulo = "Ayer", estado = "NUEVA"))
        )
        every { repo.getTodosSeguimientos() } returns flowOf(
            listOf(seguimiento(id = 1, oportunidadId = 1, proxima = fechaRelativa(-1)))
        )
        val vm = DashboardViewModel(repo, feriadosRepo)
        mantenerActivo(vm.uiState)

        assertTrue(vm.uiState.value.atrasadas.single().detalle.endsWith("(hace 1 día)"))
    }

    @Test
    fun gestion_para_manana_y_para_hoy() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        every { repo.getTodasOportunidades() } returns flowOf(
            listOf(
                oportunidad(id = 1, titulo = "Mañana", estado = "NUEVA"),
                oportunidad(id = 2, titulo = "Hoy", estado = "NUEVA"),
                oportunidad(id = 3, titulo = "Sin próxima fecha", estado = "NUEVA")
            )
        )
        every { repo.getTodosSeguimientos() } returns flowOf(
            listOf(
                seguimiento(id = 1, oportunidadId = 1, proxima = fechaRelativa(1)),
                seguimiento(id = 2, oportunidadId = 2, proxima = fechaRelativa(0)),
                seguimiento(id = 3, oportunidadId = 3, proxima = null)
            )
        )
        val vm = DashboardViewModel(repo, feriadosRepo)
        mantenerActivo(vm.uiState)

        val pendientes = vm.uiState.value.pendientes
        assertEquals(listOf("Hoy", "Mañana"), pendientes.map { it.titulo })
        assertTrue(pendientes[0].detalle.endsWith("(hoy)"))
        assertTrue(pendientes[1].detalle.endsWith("(mañana)"))
        // La que no tiene próxima fecha no es pendiente, atrasada ni sin gestión
        assertTrue(vm.uiState.value.atrasadas.isEmpty())
        assertTrue(vm.uiState.value.sinGestion.isEmpty())
        coVerify { feriadosRepo.proximosFeriados(any(), any(), any()) }
    }
}