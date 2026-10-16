package com.example.proyecto_evaluacion.dataDao
import androidx.room.*
import com.example.proyecto_evaluacion.data.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND password = :password LIMIT 1")
    suspend fun login(correo: String, password: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE rol = 'EJECUTIVO'")
    fun getEjecutivos(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios")
    fun getAllUsuarios(): Flow<List<Usuario>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usuarios: List<Usuario>)
}