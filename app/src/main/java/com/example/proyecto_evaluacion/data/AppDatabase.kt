package com.example.proyecto_evaluacion.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.proyecto_evaluacion.data.*
import com.example.proyecto_evaluacion.dataDao.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Usuario::class,
        Organizacion::class,
        Contacto::class,
        Oportunidad::class,
        Seguimiento::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun organizacionDao(): OrganizacionDao
    abstract fun contactoDao(): ContactoDao
    abstract fun oportunidadDao(): OportunidadDao
    abstract fun seguimientoDao(): SeguimientoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crm_evaluacion.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        // Cuentas ficticias de prueba solicitadas por el checklist oficial
                        val usuariosPrueba = listOf(
                            Usuario(
                                nombre = "Carlos Supervisor",
                                correo = "supervisor@duoc.cl",
                                password = "123",
                                rol = "SUPERVISOR"
                            ),
                            Usuario(
                                nombre = "Ana Ejecutivo",
                                correo = "ana@duoc.cl",
                                password = "123",
                                rol = "EJECUTIVO"
                            ),
                            Usuario(
                                nombre = "Bruno Ejecutivo",
                                correo = "bruno@duoc.cl",
                                password = "123",
                                rol = "EJECUTIVO"
                            ),
                            Usuario(
                                nombre = "Carla Ejecutivo",
                                correo = "carla@duoc.cl",
                                password = "123",
                                rol = "EJECUTIVO"
                            )
                        )
                        database.usuarioDao().insertAll(usuariosPrueba)
                    }
                }
            }
        }
    }
}