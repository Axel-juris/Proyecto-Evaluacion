package com.example.proyecto_evaluacion.data.remote

import com.example.proyecto_evaluacion.data.fechas.Feriado
import retrofit2.http.GET
import retrofit2.http.Path

/** API externa Nager.Date: feriados públicos por país y año (gratuita, sin clave). */
interface FeriadosApi {

    @GET("api/v3/PublicHolidays/{anio}/{pais}")
    suspend fun obtenerFeriados(
        @Path("anio") anio: Int,
        @Path("pais") pais: String
    ): List<Feriado>
}