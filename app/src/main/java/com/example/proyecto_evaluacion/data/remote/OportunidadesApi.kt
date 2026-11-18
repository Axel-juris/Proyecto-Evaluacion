package com.example.proyecto_evaluacion.data.remote

import com.example.proyecto_evaluacion.data.Contacto
import com.example.proyecto_evaluacion.data.Oportunidad
import com.example.proyecto_evaluacion.data.Organizacion
import com.example.proyecto_evaluacion.data.Seguimiento
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Microservicio ms-oportunidades (puerto 8082). */
interface OportunidadesApi {

    // ---- Organizaciones ----
    @GET("api/organizaciones")
    suspend fun obtenerOrganizaciones(): List<Organizacion>

    @POST("api/organizaciones")
    suspend fun crearOrganizacion(@Body organizacion: Organizacion): Organizacion

    @PUT("api/organizaciones/{id}")
    suspend fun actualizarOrganizacion(
        @Path("id") id: Int,
        @Body organizacion: Organizacion
    ): Organizacion

    @DELETE("api/organizaciones/{id}")
    suspend fun eliminarOrganizacion(@Path("id") id: Int): Response<Void>

    // ---- Contactos ----
    @GET("api/contactos")
    suspend fun obtenerContactos(
        @Query("organizacionId") organizacionId: Int? = null
    ): List<Contacto>

    @POST("api/contactos")
    suspend fun crearContacto(@Body contacto: Contacto): Contacto

    @PUT("api/contactos/{id}")
    suspend fun actualizarContacto(
        @Path("id") id: Int,
        @Body contacto: Contacto
    ): Contacto

    @DELETE("api/contactos/{id}")
    suspend fun eliminarContacto(@Path("id") id: Int): Response<Void>

    // ---- Oportunidades ----
    @GET("api/oportunidades")
    suspend fun obtenerOportunidades(
        @Query("ejecutivoId") ejecutivoId: Int? = null
    ): List<Oportunidad>

    @GET("api/oportunidades/{id}")
    suspend fun obtenerOportunidad(@Path("id") id: Int): Oportunidad

    @POST("api/oportunidades")
    suspend fun crearOportunidad(@Body oportunidad: Oportunidad): Oportunidad

    @PUT("api/oportunidades/{id}")
    suspend fun actualizarOportunidad(
        @Path("id") id: Int,
        @Body oportunidad: Oportunidad
    ): Oportunidad

    @PATCH("api/oportunidades/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: Int,
        @Body solicitud: EstadoRequest
    ): Oportunidad

    @PATCH("api/oportunidades/{id}/ejecutivo")
    suspend fun reasignarEjecutivo(
        @Path("id") id: Int,
        @Body solicitud: AsignacionRequest
    ): Oportunidad

    @DELETE("api/oportunidades/{id}")
    suspend fun eliminarOportunidad(@Path("id") id: Int): Response<Void>

    // ---- Seguimientos ----
    @GET("api/seguimientos")
    suspend fun obtenerSeguimientos(
        @Query("oportunidadId") oportunidadId: Int? = null
    ): List<Seguimiento>

    @POST("api/seguimientos")
    suspend fun crearSeguimiento(@Body seguimiento: Seguimiento): Seguimiento

    @DELETE("api/seguimientos/{id}")
    suspend fun eliminarSeguimiento(@Path("id") id: Int): Response<Void>
}