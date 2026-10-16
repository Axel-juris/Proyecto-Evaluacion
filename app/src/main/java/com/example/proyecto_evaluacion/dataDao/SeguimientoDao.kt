package com.example.proyecto_evaluacion.dataDao

import androidx.room.*
import com.example.proyecto_evaluacion.data.Seguimiento
import kotlinx.coroutines.flow.Flow

@Dao
interface SeguimientoDao {
    @Query("SELECT * FROM seguimientos WHERE oportunidadId = :oportunidadId ORDER BY id DESC")
    fun getSeguimientosPorOportunidad(oportunidadId: Int): Flow<List<Seguimiento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(seguimiento: Seguimiento)
}