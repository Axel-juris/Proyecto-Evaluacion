package com.example.proyecto_evaluacion.dataDao
import androidx.room.*
import com.example.proyecto_evaluacion.data.Organizacion
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizacionDao {
    @Query("SELECT * FROM organizaciones ORDER BY nombre ASC")
    fun getAllOrganizaciones(): Flow<List<Organizacion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(organizacion: Organizacion): Long
}