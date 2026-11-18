package com.example.proyecto_evaluacion.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 10.0.2.2 = el PC desde el emulador. Con un teléfono real, usa la IP de tu PC
    // (y agrégala también en res/xml/network_security_config.xml).
    private const val HOST = "10.0.2.2"

    private const val URL_USUARIOS = "http://$HOST:8081/"
    private const val URL_OPORTUNIDADES = "http://$HOST:8082/"

    // Si el servidor está apagado, se falla en pocos segundos en vez de quedar cargando
    private val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private fun <T> crear(baseUrl: String, api: Class<T>): T =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)

    val usuariosApi: UsuariosApi by lazy { crear(URL_USUARIOS, UsuariosApi::class.java) }

    val oportunidadesApi: OportunidadesApi by lazy {
        crear(URL_OPORTUNIDADES, OportunidadesApi::class.java)
    }
}