package com.example.proyecto_evaluacion.data

import com.example.proyecto_evaluacion.data.fechas.Feriado
import com.example.proyecto_evaluacion.data.remote.FeriadosApi
import com.example.proyecto_evaluacion.esperarError
import com.example.proyecto_evaluacion.httpError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class FeriadosRepositoryTest {

    private val api = mockk<FeriadosApi>()
    private val repo = FeriadosRepository(api)

    private val navidad = Feriado("2026-12-25", "Navidad", "Christmas Day")
    private val anioNuevo = Feriado("2027-01-01", "Año Nuevo", "New Year's Day")

    @Test
    fun obtiene_los_feriados_de_chile() = runTest {
        coEvery { api.obtenerFeriados(2026, "CL") } returns listOf(navidad)

        assertEquals(listOf(navidad), repo.obtenerFeriados(2026))
        coVerify(exactly = 1) { api.obtenerFeriados(2026, "CL") }
    }

    @Test
    fun traduce_los_errores_de_red() = runTest {
        coEvery { api.obtenerFeriados(any(), any()) } throws IOException("sin red")

        val error = esperarError<ErrorAplicacion> { repo.obtenerFeriados(2026) }
        assertEquals("No hay conexión con el servidor. Revisa que los microservicios estén encendidos.", error.message)
    }

    @Test
    fun traduce_los_errores_http() = runTest {
        coEvery { api.obtenerFeriados(any(), any()) } throws httpError(500)

        val error = esperarError<ErrorAplicacion> { repo.obtenerFeriados(2026) }
        assertEquals(500, error.codigoHttp)
    }

    @Test
    fun no_traga_la_cancelacion() = runTest {
        coEvery { api.obtenerFeriados(any(), any()) } throws CancellationException("cancelado")

        esperarError<CancellationException> { repo.obtenerFeriados(2026) }
    }

    @Test
    fun proximos_feriados_combina_el_anio_actual_y_el_siguiente() = runTest {
        coEvery { api.obtenerFeriados(2026, "CL") } returns listOf(navidad)
        coEvery { api.obtenerFeriados(2027, "CL") } returns listOf(anioNuevo)

        val resultado = repo.proximosFeriados(2026, "2026-12-20", 3)

        assertEquals(listOf("2026-12-25", "2027-01-01"), resultado.map { it.date })
    }

    @Test
    fun proximos_feriados_respeta_la_cantidad() = runTest {
        coEvery { api.obtenerFeriados(2026, "CL") } returns listOf(navidad)
        coEvery { api.obtenerFeriados(2027, "CL") } returns listOf(anioNuevo)

        assertEquals(1, repo.proximosFeriados(2026, "2026-12-20", 1).size)
    }
}