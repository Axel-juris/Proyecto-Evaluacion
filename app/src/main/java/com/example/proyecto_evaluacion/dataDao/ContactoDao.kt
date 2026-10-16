package com.example.proyecto_evaluacion.dataDao

import androidx.room.*
import com.example.proyecto_evaluacion.data.Contacto
import kotlinx.coroutines.flow.Flow
@Dao
interface ContactoDao {
    @Query("SELECT * FROM contactos WHERE organizacionId = :organizacionId")
    fun getContactosPorOrganizacion(organizacionId: Int): Flow<List<Contacto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contacto: Contacto)
}