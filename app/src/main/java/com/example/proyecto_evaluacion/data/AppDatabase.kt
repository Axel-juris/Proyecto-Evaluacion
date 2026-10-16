package com.example.proyecto_evaluacion.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.proyecto_evaluacion.dataDao.ContactoDao
import com.example.proyecto_evaluacion.dataDao.OportunidadDao
import com.example.proyecto_evaluacion.dataDao.OrganizacionDao
import com.example.proyecto_evaluacion.dataDao.SeguimientoDao
import com.example.proyecto_evaluacion.dataDao.UsuarioDao

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
            // Cuentas ficticias del checklist: 1 supervisor y 3 ejecutivos.
            // Se insertan con SQL dentro de onCreate para que existan antes del primer login.
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL("INSERT INTO usuarios (nombre, correo, password, rol) VALUES ('Carlos Supervisor', 'supervisor@duoc.cl', 'clave123', 'SUPERVISOR')")
                db.execSQL("INSERT INTO usuarios (nombre, correo, password, rol) VALUES ('Ana Ejecutivo', 'ana@duoc.cl', 'clave123', 'EJECUTIVO')")
                db.execSQL("INSERT INTO usuarios (nombre, correo, password, rol) VALUES ('Bruno Ejecutivo', 'bruno@duoc.cl', 'clave123', 'EJECUTIVO')")
                db.execSQL("INSERT INTO usuarios (nombre, correo, password, rol) VALUES ('Carla Ejecutivo', 'carla@duoc.cl', 'clave123', 'EJECUTIVO')")
            }
        }
    }
}