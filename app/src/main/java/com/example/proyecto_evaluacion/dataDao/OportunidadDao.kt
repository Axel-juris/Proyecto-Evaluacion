package com.example.proyecto_evaluacion.dataDao

import androidx.room.*
import com.example.proyecto_evaluacion.data.Oportunidad
import kotlinx.coroutines.flow.Flow

@Dao
interface OportunidadDao {
    @Query("SELECT * FROM oportunidades ORDER BY id DESC")
    fun getAllOportunidades(): Flow<List<Oportunidad>>

    @Query("SELECT * FROM oportunidades WHERE ejecutivoId = :ejecutivoId ORDER BY id DESC")
    fun getOportunidadesPorEjecutivo(ejecutivoId: Int): Flow<List<Oportunidad>>

    @Query("SELECT * FROM oportunidades WHERE id = :id LIMIT 1")
    suspend fun getOportunidadPorId(id: Int): Oportunidad?

    @Query("SELECT * FROM oportunidades WHERE id = :id LIMIT 1")
    fun observarOportunidadPorId(id: Int): Flow<Oportunidad?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(oportunidad: Oportunidad): Long

    @Update
    suspend fun update(oportunidad: Oportunidad)

    @Query("UPDATE oportunidades SET ejecutivoId = :ejecutivoId WHERE id = :oportunidadId")
    suspend fun reasignarEjecutivo(oportunidadId: Int, ejecutivoId: Int)

    @Query("UPDATE oportunidades SET estado = :nuevoEstado WHERE id = :oportunidadId")
    suspend fun cambiarEstado(oportunidadId: Int, nuevoEstado: String)

    @Query("UPDATE oportunidades SET fotoUri = :fotoUri WHERE id = :oportunidadId")
    suspend fun actualizarFoto(oportunidadId: Int, fotoUri: String?)
}