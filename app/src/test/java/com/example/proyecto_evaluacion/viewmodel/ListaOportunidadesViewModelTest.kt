package com.example.proyecto_evaluacion.viewmodel

import com.example.proyecto_evaluacion.EJECUTIVO
import com.example.proyecto_evaluacion.MainDispatcherRule
import com.example.proyecto_evaluacion.SUPERVISOR
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.mantenerActivo
import com.example.proyecto_evaluacion.oportunidad
import com.example.proyecto_evaluacion.organizacion
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ListaOportunidadesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<CrmRepository>()

    private val oportunidades = listOf(
        oportunidad(id = 1, titulo = "Feria Verano", estado = "NUEVA", prioridad = "ALTA", ejecutivoId = 2, organizacionId = 1),
        oportunidad(id = 2, titulo = "Jornada Salud", estado = "EN_GESTION", prioridad = "BAJA", ejecutivoId = null, organizacionId = 2),
        oportunidad(id = 3, titulo = "Expo Otoño", estado = "CONFIRMADA", prioridad = "ALTA", ejecutivoId = 99, organizacionId = 77)
    )

    @Before
    fun setUp() {
        SessionManager.cerrarSesion()
        every { repo.getOrganizaciones() } returns
                flowOf(listOf(organizacion(1, "Acme SpA"), organizacion(2, "Municipalidad Maipú")))
        every { repo.getEjecutivos() } returns flowOf(listOf(EJECUTIVO))
        every { repo.getTodasOportunidades() } returns flowOf(oportunidades)
        every { repo.getOportunidadesPorEjecutivo(2) } returns flowOf(oportunidades.take(1))
    }

    @After
    fun tearDown() = SessionManager.cerrarSesion()

    @Test
    fun supervisor_ve_todas_y_se_muestran_nombres_de_organizacion_y_responsable() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        val vm = ListaOportunidadesViewModel(repo)
        mantenerActivo(vm.uiState)

        val estado = vm.uiState.value
        assertEquals(3, estado.items.size)
        assertEquals(3, estado.totalSinFiltrar)
        assertEquals("Acme SpA", estado.items[0].organizacionNombre)
        assertEquals("Eva Ejecutiva", estado.items[0].responsable)
        assertEquals("Sin asignar", estado.items[1].responsable)
        assertEquals("Desconocido", estado.items[2].responsable)
        assertEquals("Sin organización", estado.items[2].organizacionNombre)
    }

    @Test
    fun ejecutivo_solo_ve_las_suyas() = runTest {
        SessionManager.iniciarSesion(EJECUTIVO)
        val vm = ListaOportunidadesViewModel(repo)
        mantenerActivo(vm.uiState)

        assertEquals(listOf(1), vm.uiState.value.items.map { it.oportunidad.id })
        verify(exactly = 0) { repo.getTodasOportunidades() }
    }

    @Test
    fun sin_sesion_la_lista_esta_vacia() = runTest {
        val vm = ListaOportunidadesViewModel(repo)
        mantenerActivo(vm.uiState)

        assertTrue(vm.uiState.value.items.isEmpty())
        assertEquals(0, vm.uiState.value.totalSinFiltrar)
    }

    @Test
    fun busca_por_titulo_y_por_nombre_de_organizacion() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        val vm = ListaOportunidadesViewModel(repo)
        mantenerActivo(vm.uiState)

        vm.cambiarBusqueda("verano")
        assertEquals(listOf(1), vm.uiState.value.items.map { it.oportunidad.id })

        vm.cambiarBusqueda("maipú")
        assertEquals(listOf(2), vm.uiState.value.items.map { it.oportunidad.id })

        vm.cambiarBusqueda("   ")
        assertEquals(3, vm.uiState.value.items.size)
    }

    @Test
    fun filtra_por_estado_y_prioridad() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        val vm = ListaOportunidadesViewModel(repo)
        mantenerActivo(vm.uiState)

        vm.cambiarEstado("EN_GESTION")
        assertEquals(listOf(2), vm.uiState.value.items.map { it.oportunidad.id })

        vm.cambiarEstado(TODOS)
        vm.cambiarPrioridad("ALTA")
        assertEquals(listOf(1, 3), vm.uiState.value.items.map { it.oportunidad.id })

        vm.cambiarEstado("CONFIRMADA")
        assertEquals(listOf(3), vm.uiState.value.items.map { it.oportunidad.id })
        assertEquals(3, vm.uiState.value.totalSinFiltrar)
    }

    @Test
    fun limpiar_filtros_restaura_la_lista_completa() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        val vm = ListaOportunidadesViewModel(repo)
        mantenerActivo(vm.uiState)
        vm.cambiarBusqueda("zzz")
        vm.cambiarEstado("NUEVA")
        vm.cambiarPrioridad("BAJA")
        assertTrue(vm.uiState.value.items.isEmpty())

        vm.limpiarFiltros()

        assertEquals(FiltrosLista(), vm.filtros.value)
        assertEquals(3, vm.uiState.value.items.size)
    }
}