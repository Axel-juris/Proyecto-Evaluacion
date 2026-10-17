package com.example.proyecto_evaluacion.fechas

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract

object Calendario {

    // Abre la app de calendario con el evento ya completado, sin pedir permisos.
    // Devuelve null si todo salió bien, o el mensaje de error para mostrar.
    fun agendarEvento(
        context: Context,
        titulo: String,
        descripcion: String,
        lugar: String,
        inicioMillis: Long,
        finMillis: Long,
        todoElDia: Boolean
    ): String? {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, titulo)
            putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
            putExtra(CalendarContract.Events.EVENT_LOCATION, lugar)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicioMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, finMillis)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, todoElDia)
        }

        return try {
            context.startActivity(intent)
            null
        } catch (e: ActivityNotFoundException) {
            "Este dispositivo no tiene una app de calendario disponible"
        } catch (e: Exception) {
            "No se pudo abrir el calendario, intenta de nuevo"
        }
    }
}