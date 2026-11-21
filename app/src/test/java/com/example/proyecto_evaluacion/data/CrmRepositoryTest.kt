package com.example.proyecto_evaluacion.data

import com.example.proyecto_evaluacion.EJECUTIVO
import com.example.proyecto_evaluacion.SUPERVISOR
import com.example.proyecto_evaluacion.contacto
import com.example.proyecto_evaluacion.data.remote.AsignacionRequest
import com.example.proyecto_evaluacion.data.remote.EstadoRequest
import com.example.proyecto_evaluacion.data.remote.LoginRequest
import com.example.proyecto_evaluacion.data.remote.OportunidadesApi
import com.example.proyecto_evaluacion.data.remote.UsuariosApi
import com.example.proyecto_evaluacion.esperarError
import com.example.proyecto_evaluacion.httpError
import com.example.proyecto_evaluacion.oportunidad
import com.example.proyecto_evaluacion.organizacion
import com.example.proyecto_evaluacion.respuestaError
import com.example.proyecto_evaluacion.respuestaOk
import com.example.proyecto_evaluacion.seguimiento
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CrmRepositoryTest {

    private val usuariosApi = mockk<UsuariosApi>()
    private val api = mockk<OportunidadesApi>()
    private lateinit var repo: CrmRepository

    @Before
    fun setUp() {
        repo = CrmRepository(usuariosApi, api)
    }

    // ---------- Usuarios ----------

    @Test
    fun login_correcto_devuelve_el_usuario() = runTest {
        coEvery { usuariosApi.login(LoginRequest("sofia@duoc.cl", "secreto")) } returns SUPERVISOR

        assertEquals(SUPERVISOR, repo.login("sofia@duoc.cl", "secreto"))
    }

    @Test
    fun login_con_401_devuelve_null() = runTest {
        coEvery { usuariosApi.login(any()) } throws httpError(401)

        assertNull(repo.login("sofia@duoc.cl", "mala"))
    }

    @Test
    fun login_con_otro_error_se_propaga_traducido() = runTest {
        coEvery { usuariosApi.login(any()) } throws httpError(500)

        val error = esperarError<ErrorAplicacion> { repo.login("a@b.cl", "123456") }
        assertEquals(500, error.codigoHttp)
    }

    @Test
    fun obtiene_ejecutivos_y_usuarios() = runTest {
        coEvery { usuariosApi.obtenerEjecutivos() } returns listOf(EJECUTIVO)
        coEvery { usuariosApi.obtenerUsuarios() } returns listOf(SUPERVISOR, EJECUTIVO)

        assertEquals(listOf(EJECUTIVO), repo.getEjecutivos().first())
        assertEquals(2, repo.getUsuarios().first().size)
    }

    // ---------- Organizaciones ----------

    @Test
    fun organizaciones_se_ordenan_por_nombre_sin_distinguir_mayusculas() = runTest {
        coEvery { api.obtenerOrganizaciones() } returns
                listOf(organizacion(1, "zeta"), organizacion(2, "Alfa"), organizacion(3, "beta"))

        assertEquals(listOf("Alfa", "beta", "zeta"), repo.getOrganizaciones().first().map { it.nombre })
    }

    @Test
    fun crud_de_organizaciones() = runTest {
        val org = organizacion(4, "Nueva")
        coEvery { api.crearOrganizacion(org) } returns org
        coEvery { api.actualizarOrganizacion(4, org) } returns org
        coEvery { api.eliminarOrganizacion(4) } returns respuestaOk()

        repo.guardarOrganizacion(org)
        repo.actualizarOrganizacion(org)
        repo.eliminarOrganizacion(4)

        coVerify(exactly = 1) { api.crearOrganizacion(org) }
        coVerify(exactly = 1) { api.actualizarOrganizacion(4, org) }
        coVerify(exactly = 1) { api.eliminarOrganizacion(4) }
    }

    @Test
    fun eliminar_con_respuesta_de_error_lanza_ErrorAplicacion() = runTest {
        coEvery { api.eliminarOrganizacion(9) } returns respuestaError(404)

        val error = esperarError<ErrorAplicacion> { repo.eliminarOrganizacion(9) }
        assertEquals(404, error.codigoHttp)
        assertEquals("El registro solicitado no existe.", error.message)
    }

    // ---------- Contactos ----------

    @Test
    fun consulta_contactos_por_organizacion_y_todos() = runTest {
        coEvery { api.obtenerContactos(1) } returns listOf(contacto(7, 1))
        coEvery { api.obtenerContactos(null) } returns listOf(contacto(7, 1), contacto(8, 2))

        assertEquals(1, repo.getContactos(1).first().size)
        assertEquals(2, repo.getTodosContactos().first().size)
    }

    @Test
    fun crud_de_contactos() = runTest {
        val c = contacto()
        coEvery { api.crearContacto(c) } returns c
        coEvery { api.actualizarContacto(7, c) } returns c
        coEvery { api.eliminarContacto(7) } returns respuestaOk()

        repo.guardarContacto(c)
        repo.actualizarContacto(c)
        repo.eliminarContacto(7)

        coVerify { api.crearContacto(c) }
        coVerify { api.actualizarContacto(7, c) }
        coVerify { api.eliminarContacto(7) }
    }

    @Test
    fun eliminar_contacto_con_error() = runTest {
        coEvery { api.eliminarContacto(1) } returns respuestaError(409)

        val error = esperarError<ErrorAplicacion> { repo.eliminarContacto(1) }
        assertEquals(409, error.codigoHttp)
    }

    // ---------- Oportunidades ----------

    @Test
    fun oportunidades_se_ordenan_de_la_mas_nueva_a_la_mas_antigua() = runTest {
        coEvery { api.obtenerOportunidades(null) } returns
                listOf(oportunidad(id = 1), oportunidad(id = 3), oportunidad(id = 2))
        coEvery { api.obtenerOportunidades(2) } returns listOf(oportunidad(id = 4), oportunidad(id = 9))

        assertEquals(listOf(3, 2, 1), repo.getTodasOportunidades().first().map { it.id })
        assertEquals(listOf(9, 4), repo.getOportunidadesPorEjecutivo(2).first().map { it.id })
    }

    @Test
    fun oportunidad_por_id_devuelve_null_si_no_existe() = runTest {
        coEvery { api.obtenerOportunidad(1) } returns oportunidad(id = 1)
        coEvery { api.obtenerOportunidad(2) } throws httpError(404)
        coEvery { api.obtenerOportunidad(3) } throws httpError(500)

        assertEquals(1, repo.getOportunidadPorId(1)?.id)
        assertNull(repo.getOportunidadPorId(2))
        esperarError<ErrorAplicacion> { repo.getOportunidadPorId(3) }
    }

    @Test
    fun observar_oportunidad_emite_la_oportunidad() = runTest {
        coEvery { api.obtenerOportunidad(1) } returns oportunidad(id = 1)
        coEvery { api.obtenerOportunidad(2) } throws httpError(404)

        assertEquals(1, repo.observarOportunidad(1).first()?.id)
        assertNull(repo.observarOportunidad(2).first())
    }

    @Test
    fun crud_de_oportunidades() = runTest {
        val op = oportunidad(id = 5)
        coEvery { api.crearOportunidad(op) } returns op
        coEvery { api.actualizarOportunidad(5, op) } returns op
        coEvery { api.eliminarOportunidad(5) } returns respuestaOk()

        repo.guardarOportunidad(op)
        repo.actualizarOportunidad(op)
        repo.eliminarOportunidad(5)

        coVerify { api.crearOportunidad(op) }
        coVerify { api.actualizarOportunidad(5, op) }
        coVerify { api.eliminarOportunidad(5) }
    }

    @Test
    fun eliminar_oportunidad_con_error() = runTest {
        coEvery { api.eliminarOportunidad(5) } returns respuestaError(403)

        val error = esperarError<ErrorAplicacion> { repo.eliminarOportunidad(5) }
        assertEquals("No tienes permisos para esta operación.", error.message)
    }

    @Test
    fun cambia_estado_y_reasigna_ejecutivo() = runTest {
        coEvery { api.cambiarEstado(5, EstadoRequest("CONFIRMADA")) } returns oportunidad(estado = "CONFIRMADA")
        coEvery { api.reasignarEjecutivo(5, AsignacionRequest(3)) } returns oportunidad(ejecutivoId = 3)

        repo.cambiarEstadoOportunidad(5, "CONFIRMADA")
        repo.reasignarEjecutivo(5, 3)

        coVerify { api.cambiarEstado(5, EstadoRequest("CONFIRMADA")) }
        coVerify { api.reasignarEjecutivo(5, AsignacionRequest(3)) }
    }

    @Test
    fun actualizar_foto_conserva_el_resto_de_la_oportunidad() = runTest {
        val actual = oportunidad(id = 5, titulo = "Original")
        coEvery { api.obtenerOportunidad(5) } returns actual
        coEvery { api.actualizarOportunidad(5, any()) } returns actual

        repo.actualizarFotoOportunidad(5, "content://foto.jpg")

        coVerify { api.actualizarOportunidad(5, actual.copy(fotoUri = "content://foto.jpg")) }
    }

    // ---------- Seguimientos ----------

    @Test
    fun seguimientos_se_ordenan_del_mas_reciente_al_mas_antiguo() = runTest {
        coEvery { api.obtenerSeguimientos(5) } returns listOf(seguimiento(id = 1), seguimiento(id = 4))
        coEvery { api.obtenerSeguimientos(null) } returns
                listOf(seguimiento(id = 2), seguimiento(id = 8), seguimiento(id = 5))

        assertEquals(listOf(4, 1), repo.getSeguimientos(5).first().map { it.id })
        assertEquals(listOf(8, 5, 2), repo.getTodosSeguimientos().first().map { it.id })
    }

    @Test
    fun guardar_y_eliminar_seguimiento() = runTest {
        val s = seguimiento(id = 0)
        coEvery { api.crearSeguimiento(s) } returns s
        coEvery { api.eliminarSeguimiento(3) } returns respuestaOk()

        repo.guardarSeguimiento(s)
        repo.eliminarSeguimiento(3)

        coVerify { api.crearSeguimiento(s) }
        coVerify { api.eliminarSeguimiento(3) }
    }

    @Test
    fun eliminar_seguimiento_con_error() = runTest {
        coEvery { api.eliminarSeguimiento(3) } returns respuestaError(404)

        esperarError<ErrorAplicacion> { repo.eliminarSeguimiento(3) }
    }

    // ---------- Refresco y errores de conexión ----------

    @Test
    fun un_flow_vuelve_a_consultar_al_servidor_despues_de_guardar() = runTest {
        val primera = listOf(organizacion(1, "Alfa"))
        val segunda = listOf(organizacion(1, "Alfa"), organizacion(2, "Beta"))
        coEvery { api.obtenerOrganizaciones() } returnsMany listOf(primera, segunda)
        coEvery { api.crearOrganizacion(any()) } returns organizacion(2, "Beta")

        val emisiones = mutableListOf<List<Organizacion>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.getOrganizaciones().toList(emisiones)
        }
        repo.guardarOrganizacion(organizacion(0, "Beta"))

        assertEquals(2, emisiones.size)
        assertEquals(1, emisiones[0].size)
        assertEquals(2, emisiones[1].size)
    }

    @Test
    fun si_el_servidor_falla_se_conserva_el_ultimo_dato_y_se_publica_el_error() = runTest {
        val datos = listOf(organizacion(1, "Alfa"))
        coEvery { api.obtenerOrganizaciones() } returns datos

        val emisiones = mutableListOf<List<Organizacion>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.getOrganizaciones().toList(emisiones)
        }
        assertNull(repo.errorConexion.value)

        coEvery { api.obtenerOrganizaciones() } throws IOException("sin red")
        repo.refrescar()

        assertEquals(2, emisiones.size)
        assertEquals(datos, emisiones[1])
        assertEquals(
            "No hay conexión con el servidor. Revisa que los microservicios estén encendidos.",
            repo.errorConexion.value
        )

        // Cuando el servidor vuelve, el mensaje de error desaparece
        coEvery { api.obtenerOrganizaciones() } returns datos
        repo.refrescar()
        assertNull(repo.errorConexion.value)
    }

    @Test
    fun sin_datos_previos_un_error_deja_la_lista_vacia() = runTest {
        coEvery { api.obtenerOrganizaciones() } throws httpError(500)

        assertTrue(repo.getOrganizaciones().first().isEmpty())
        assertEquals("Error interno del servidor.", repo.errorConexion.value)
    }
}