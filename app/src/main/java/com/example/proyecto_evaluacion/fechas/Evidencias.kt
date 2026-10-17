package com.example.proyecto_evaluacion.fechas
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

object Evidencias {

    private const val CARPETA = "evidencias"

    // Crea el archivo vacío donde la cámara dejará la foto y devuelve su URI segura
    fun crearDestino(context: Context, oportunidadId: Int): Uri {
        val carpeta = File(context.filesDir, CARPETA)
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw IOException("No se pudo crear la carpeta de evidencias")
        }

        val archivo = File(carpeta, "oportunidad_${oportunidadId}_${System.currentTimeMillis()}.jpg")
        archivo.createNewFile()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
    }

    // Borra el archivo asociado a una URI; se usa al cancelar o al reemplazar una foto
    fun eliminar(context: Context, uri: String?) {
        if (uri.isNullOrBlank()) return
        val nombre = Uri.parse(uri).lastPathSegment ?: return
        File(File(context.filesDir, CARPETA), nombre).delete()
    }
}
