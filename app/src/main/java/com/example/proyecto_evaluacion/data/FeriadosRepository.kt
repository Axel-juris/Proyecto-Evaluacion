package com.example.proyecto_evaluacion.data

import com.example.proyecto_evaluacion.data.fechas.Feriado
import com.example.proyecto_evaluacion.data.remote.FeriadosApi
import com.example.proyecto_evaluacion.data.remote.RetrofitClient
import com.example.proyecto_evaluacion.fechas.FeriadosHelper
import kotlinx.coroutines.CancellationException

/**
 * Acceso a la API externa de feriados. Está separado de CrmRepository a propósito:
 * así los feriados nunca se mezclan ni interfieren con los datos de los microservicios propios.
 */
class FeriadosRepository(
    private val api: FeriadosApi = RetrofitClient.feriadosApi
) {

    /** Feriados de Chile de un año completo. */
    suspend fun obtenerFeriados(anio: Int): List<Feriado> {
        return try {
            api.obtenerFeriados(anio, "CL")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw traducirError(e)
        }
    }

    /**
     * Próximos feriados desde [hoyIso]. Se piden dos años para que, a fin de año,
     * también aparezcan los de enero del año siguiente.
     */
    suspend fun proximosFeriados(anio: Int, hoyIso: String, cantidad: Int = 3): List<Feriado> {
        val todos = obtenerFeriados(anio) + obtenerFeriados(anio + 1)
        return FeriadosHelper.proximos(todos, hoyIso, cantidad)
    }
}