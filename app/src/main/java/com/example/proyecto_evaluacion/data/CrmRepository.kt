package com.example.proyecto_evaluacion.data

import com.example.proyecto_evaluacion.data.remote.AsignacionRequest
import com.example.proyecto_evaluacion.data.remote.EstadoRequest
import com.example.proyecto_evaluacion.data.remote.LoginRequest
import com.example.proyecto_evaluacion.data.remote.OportunidadesApi
import com.example.proyecto_evaluacion.data.remote.RetrofitClient
import com.example.proyecto_evaluacion.data.remote.UsuariosApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import retrofit2.HttpException
import retrofit2.Response

/**
 * Único punto de acceso a los datos: habla con los microservicios por Retrofit.
 *
 * Mantiene la misma interfaz que tenía con Room (funciones que devuelven Flow), así las
 * pantallas se actualizan solas: cada vez que se guarda, edita o elimina algo se llama a
 * [refrescar] y todas las listas observadas se vuelven a pedir al servidor.
 */
class CrmRepository(
    private val usuariosApi: UsuariosApi = RetrofitClient.usuariosApi,
    private val oportunidadesApi: OportunidadesApi = RetrofitClient.oportunidadesApi
) {

    // Cada cambio de este contador hace que los Flow vuelvan a consultar el servidor
    private val version = MutableStateFlow(0)

    private val _errorConexion = MutableStateFlow<String?>(null)

    /** Mensaje del último error al cargar datos, o null si todo funciona. */
    val errorConexion: StateFlow<String?> = _errorConexion.asStateFlow()

    fun refrescar() {
        version.update { it + 1 }
    }

    // === Usuarios ===

    /** Devuelve el usuario, o null si las credenciales son incorrectas. */
    suspend fun login(correo: String, pass: String): Usuario? {
        return try {
            ejecutar { usuariosApi.login(LoginRequest(correo, pass)) }
        } catch (e: ErrorAplicacion) {
            if (e.codigoHttp == 401) null else throw e
        }
    }

    fun getEjecutivos(): Flow<List<Usuario>> =
        observar(emptyList()) { usuariosApi.obtenerEjecutivos() }

    fun getUsuarios(): Flow<List<Usuario>> =
        observar(emptyList()) { usuariosApi.obtenerUsuarios() }

    // === Organizaciones ===

    fun getOrganizaciones(): Flow<List<Organizacion>> =
        observar(emptyList()) {
            oportunidadesApi.obtenerOrganizaciones().sortedBy { it.nombre.lowercase() }
        }

    suspend fun guardarOrganizacion(org: Organizacion) {
        ejecutar { oportunidadesApi.crearOrganizacion(org) }
        refrescar()
    }

    suspend fun actualizarOrganizacion(org: Organizacion) {
        ejecutar { oportunidadesApi.actualizarOrganizacion(org.id, org) }
        refrescar()
    }

    suspend fun eliminarOrganizacion(id: Int) {
        ejecutar { comprobarRespuesta(oportunidadesApi.eliminarOrganizacion(id)) }
        refrescar()
    }

    // === Contactos ===

    fun getContactos(orgId: Int): Flow<List<Contacto>> =
        observar(emptyList()) { oportunidadesApi.obtenerContactos(orgId) }

    fun getTodosContactos(): Flow<List<Contacto>> =
        observar(emptyList()) { oportunidadesApi.obtenerContactos() }

    suspend fun guardarContacto(contacto: Contacto) {
        ejecutar { oportunidadesApi.crearContacto(contacto) }
        refrescar()
    }

    suspend fun actualizarContacto(contacto: Contacto) {
        ejecutar { oportunidadesApi.actualizarContacto(contacto.id, contacto) }
        refrescar()
    }

    suspend fun eliminarContacto(id: Int) {
        ejecutar { comprobarRespuesta(oportunidadesApi.eliminarContacto(id)) }
        refrescar()
    }

    // === Oportunidades ===

    fun getTodasOportunidades(): Flow<List<Oportunidad>> =
        observar(emptyList()) {
            oportunidadesApi.obtenerOportunidades().sortedByDescending { it.id }
        }

    fun getOportunidadesPorEjecutivo(ejecutivoId: Int): Flow<List<Oportunidad>> =
        observar(emptyList()) {
            oportunidadesApi.obtenerOportunidades(ejecutivoId).sortedByDescending { it.id }
        }

    fun observarOportunidad(id: Int): Flow<Oportunidad?> =
        observar<Oportunidad?>(null) { getOportunidadPorId(id) }

    /** Devuelve la oportunidad, o null si no existe (404). */
    suspend fun getOportunidadPorId(id: Int): Oportunidad? {
        return try {
            ejecutar { oportunidadesApi.obtenerOportunidad(id) }
        } catch (e: ErrorAplicacion) {
            if (e.codigoHttp == 404) null else throw e
        }
    }

    suspend fun guardarOportunidad(oportunidad: Oportunidad) {
        ejecutar { oportunidadesApi.crearOportunidad(oportunidad) }
        refrescar()
    }

    suspend fun actualizarOportunidad(oportunidad: Oportunidad) {
        ejecutar { oportunidadesApi.actualizarOportunidad(oportunidad.id, oportunidad) }
        refrescar()
    }

    suspend fun eliminarOportunidad(id: Int) {
        ejecutar { comprobarRespuesta(oportunidadesApi.eliminarOportunidad(id)) }
        refrescar()
    }

    suspend fun cambiarEstadoOportunidad(oportunidadId: Int, estado: String) {
        ejecutar { oportunidadesApi.cambiarEstado(oportunidadId, EstadoRequest(estado)) }
        refrescar()
    }

    suspend fun reasignarEjecutivo(oportunidadId: Int, ejecutivoId: Int) {
        ejecutar { oportunidadesApi.reasignarEjecutivo(oportunidadId, AsignacionRequest(ejecutivoId)) }
        refrescar()
    }

    suspend fun actualizarFotoOportunidad(oportunidadId: Int, fotoUri: String?) {
        ejecutar {
            val actual = oportunidadesApi.obtenerOportunidad(oportunidadId)
            oportunidadesApi.actualizarOportunidad(oportunidadId, actual.copy(fotoUri = fotoUri))
        }
        refrescar()
    }

    // === Seguimientos ===

    fun getSeguimientos(oportunidadId: Int): Flow<List<Seguimiento>> =
        observar(emptyList()) {
            oportunidadesApi.obtenerSeguimientos(oportunidadId).sortedByDescending { it.id }
        }

    fun getTodosSeguimientos(): Flow<List<Seguimiento>> =
        observar(emptyList()) {
            oportunidadesApi.obtenerSeguimientos().sortedByDescending { it.id }
        }

    suspend fun guardarSeguimiento(seguimiento: Seguimiento) {
        ejecutar { oportunidadesApi.crearSeguimiento(seguimiento) }
        refrescar()
    }

    suspend fun eliminarSeguimiento(id: Int) {
        ejecutar { comprobarRespuesta(oportunidadesApi.eliminarSeguimiento(id)) }
        refrescar()
    }

    // === Utilidades internas ===

    /**
     * Flow que consulta al servidor al empezar y cada vez que se llama a [refrescar].
     * Si la consulta falla no se cae la app: se conserva el último dato bueno y se
     * publica el mensaje en [errorConexion].
     */
    private fun <T> observar(inicial: T, cargar: suspend () -> T): Flow<T> {
        var ultimo = inicial
        return version.map {
            try {
                val resultado = ejecutar(cargar)
                _errorConexion.value = null
                ultimo = resultado
                resultado
            } catch (e: ErrorAplicacion) {
                _errorConexion.value = e.message
                ultimo
            }
        }
    }

    // Retrofit entrega Response<Void> aunque el servidor haya respondido 400, 404 o 500
    private fun comprobarRespuesta(respuesta: Response<Void>) {
        if (!respuesta.isSuccessful) {
            throw HttpException(respuesta)
        }
    }

    private suspend fun <T> ejecutar(operacion: suspend () -> T): T {
        return try {
            operacion()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw traducirError(e)
        }
    }

    companion object {
        /** Instancia compartida por todos los ViewModels. */
        val instancia: CrmRepository by lazy { CrmRepository() }
    }
}