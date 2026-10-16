package com.example.proyecto_evaluacion.data

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

    // === Organizaciones y Contactos ===
    fun getOrganizaciones(): Flow<List<Organizacion>> = organizacionDao.getAllOrganizaciones()
    suspend fun guardarOrganizacion(org: Organizacion) = organizacionDao.insert(org)

    fun getContactos(orgId: Int): Flow<List<Contacto>> = contactoDao.getContactosPorOrganizacion(orgId)
    suspend fun guardarContacto(contacto: Contacto) = contactoDao.insert(contacto)

    // === Oportunidades ===
    fun getTodasOportunidades(): Flow<List<Oportunidad>> = oportunidadDao.getAllOportunidades()
    fun getOportunidadesPorEjecutivo(ejecutivoId: Int): Flow<List<Oportunidad>> =
        oportunidadDao.getOportunidadesPorEjecutivo(ejecutivoId)

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
}