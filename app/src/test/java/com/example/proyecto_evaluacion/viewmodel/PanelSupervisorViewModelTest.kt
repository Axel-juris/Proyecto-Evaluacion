package com.example.proyecto_evaluacion.viewmodel

import com.example.proyecto_evaluacion.EJECUTIVO
import com.example.proyecto_evaluacion.MainDispatcherRule
import com.example.proyecto_evaluacion.OTRO_EJECUTIVO
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PanelSupervisorViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<CrmRepository>()

    @Before
    fun setUp() {
        SessionManager.cerrarSesion()
    }

    @After
    fun tearDown() = SessionManager.cerrarSesion()

    @Test
    fun un_ejecutivo_no_esta_autorizado_y_no_se_consultan_datos_globales() {
        SessionManager.iniciarSesion(EJECUTIVO)

        val vm = PanelSupervisorViewModel(repo)

        assertFalse(vm.uiState.value.autorizado)
        assertFalse(vm.uiState.value.cargando)
        verify(exactly = 0) { repo.getTodasOportunidades() }
    }

    @Test
    fun sin_sesion_tampoco_esta_autorizado() {
        val vm = PanelSupervisorViewModel(repo)

        assertFalse(vm.uiState.value.autorizado)
    }

    @Test
    fun el_supervisor_ve_totales_carga_por_ejecutivo_y_sin_asignar() = runTest {
        SessionManager.iniciarSesion(SUPERVISOR)
        every { repo.getTodasOportunidades() } returns flowOf(
            listOf(
                oportunidad(id = 1, estado = "NUEVA", ejecutivoId = 2),
                oportunidad(id = 2, estado = "EN_GESTION", ejecutivoId = 2),
                oportunidad(id = 3, estado = "CONFIRMADA", ejecutivoId = 2),
                oportunidad(id = 4, estado = "NUEVA", ejecutivoId = null, titulo = "Sin dueño", organizacionId = 1),
                oportunidad(id = 5, estado = "DESCARTADA", ejecutivoId = null),
                oportunidad(id = 6, estado = "NUEVA", ejecutivoId = null, titulo = "Huérfana", organizacionId = 50)
            )
        )
        every { repo.getEjecutivos() } returns flowOf(listOf(EJECUTIVO, OTRO_EJECUTIVO))
        every { repo.getOrganizaciones() } returns flowOf(listOf(organizacion(1, "Acme SpA")))
        val vm = PanelSupervisorViewModel(repo)
        mantenerActivo(vm.uiState)

        val estado = vm.uiState.value
        assertTrue(estado.autorizado)
        assertFalse(estado.cargando)
        assertEquals(6, estado.totalOportunidades)
        assertEquals(3, estado.porEstado["NUEVA"])
        assertEquals(1, estado.porEstado["EN_GESTION"])

        val eva = estado.cargaEjecutivos.first { it.nombre == "Eva Ejecutiva" }
        assertEquals(2, eva.activas)
        assertEquals(3, eva.total)
        val omar = estado.cargaEjecutivos.first { it.nombre == "Omar Ejecutivo" }
        assertEquals(0, omar.activas)
        assertEquals(0, omar.total)

        // La descartada no aparece como pendiente de asignar
        assertEquals(listOf(4, 6), estado.sinAsignar.map { it.oportunidadId })
        assertEquals("Acme SpA", estado.sinAsignar[0].organizacion)
        assertEquals("Sin organización", estado.sinAsignar[1].organizacion)
        assertEquals("Feria empresarial · 15/03/2030", estado.sinAsignar[0].detalle)
    }
}