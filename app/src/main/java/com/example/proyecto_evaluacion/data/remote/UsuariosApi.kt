package com.example.proyecto_evaluacion.data.remote

import com.example.proyecto_evaluacion.data.Usuario
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Microservicio ms-usuarios (puerto 8081). */
interface UsuariosApi {

    @POST("api/auth/login")
    suspend fun login(@Body solicitud: LoginRequest): Usuario

    @GET("api/usuarios")
    suspend fun obtenerUsuarios(): List<Usuario>

    @GET("api/usuarios/ejecutivos")
    suspend fun obtenerEjecutivos(): List<Usuario>
}