package com.example.proyecto_evaluacion.data

import android.content.Context
import com.example.proyecto_evaluacion.dataDao.*
import kotlinx.coroutines.flow.Flow

class CrmRepository(
    private val usuarioDao: UsuarioDao,
    private val organizacionDao: OrganizacionDao,
    private val contactoDao: ContactoDao,
    private val oportunidadDao: OportunidadDao,
    private val seguimientoDao: SeguimientoDao
) {
    // === Usuarios ===
    suspend fun login(correo: String, pass: String) = usuarioDao.login(correo, pass)
    fun getEjecutivos(): Flow<List<Usuario>> = usuarioDao.getEjecutivos()
    fun getUsuarios(): Flow<List<Usuario>> = usuarioDao.getAllUsuarios()

    // === Organizaciones y Contactos ===
    fun getOrganizaciones(): Flow<List<Organizacion>> = organizacionDao.getAllOrganizaciones()
    suspend fun guardarOrganizacion(org: Organizacion) = organizacionDao.insert(org)

    fun getContactos(orgId: Int): Flow<List<Contacto>> = contactoDao.getContactosPorOrganizacion(orgId)
    fun getTodosContactos(): Flow<List<Contacto>> = contactoDao.getAllContactos()
    suspend fun guardarContacto(contacto: Contacto) = contactoDao.insert(contacto)

    // === Oportunidades ===
    fun getTodasOportunidades(): Flow<List<Oportunidad>> = oportunidadDao.getAllOportunidades()
    fun getOportunidadesPorEjecutivo(ejecutivoId: Int): Flow<List<Oportunidad>> =
        oportunidadDao.getOportunidadesPorEjecutivo(ejecutivoId)
    suspend fun actualizarFotoOportunidad(oportunidadId: Int, fotoUri: String?) =
        oportunidadDao.actualizarFoto(oportunidadId, fotoUri)
    fun observarOportunidad(id: Int): Flow<Oportunidad?> = oportunidadDao.observarOportunidadPorId(id)
    suspend fun getOportunidadPorId(id: Int) = oportunidadDao.getOportunidadPorId(id)
    suspend fun guardarOportunidad(oportunidad: Oportunidad) = oportunidadDao.insert(oportunidad)
    suspend fun actualizarOportunidad(oportunidad: Oportunidad) = oportunidadDao.update(oportunidad)
    suspend fun reasignarEjecutivo(oportunidadId: Int, ejecutivoId: Int) =
        oportunidadDao.reasignarEjecutivo(oportunidadId, ejecutivoId)
    suspend fun cambiarEstadoOportunidad(oportunidadId: Int, estado: String) =
        oportunidadDao.cambiarEstado(oportunidadId, estado)

    // === Seguimientos ===
    fun getSeguimientos(oportunidadId: Int): Flow<List<Seguimiento>> =
        seguimientoDao.getSeguimientosPorOportunidad(oportunidadId)
    suspend fun guardarSeguimiento(seguimiento: Seguimiento) = seguimientoDao.insert(seguimiento)
    fun getTodosSeguimientos(): Flow<List<Seguimiento>> = seguimientoDao.getAllSeguimientos()
    companion object {
        @Volatile
        private var INSTANCE: CrmRepository? = null

        fun getInstance(context: Context): CrmRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val repo = CrmRepository(
                    db.usuarioDao(),
                    db.organizacionDao(),
                    db.contactoDao(),
                    db.oportunidadDao(),
                    db.seguimientoDao()
                )
                INSTANCE = repo
                repo
            }
        }
    }
}