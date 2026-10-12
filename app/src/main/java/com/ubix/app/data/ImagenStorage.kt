package com.ubix.app.data

import android.content.Context
import android.net.Uri
import java.io.File

class ImagenStorage(context: Context) {

    private val appContext = context.applicationContext

    fun guardarImagen(uriOrigen: Uri): String {
        val directorio = File(appContext.filesDir, DIRECTORIO_IMAGENES)
        if (!directorio.exists()) {
            directorio.mkdirs()
        }
        val destino = File(directorio, "$PREFIJO_ARCHIVO${System.currentTimeMillis()}.img")
        appContext.contentResolver.openInputStream(uriOrigen)?.use { entrada ->
            destino.outputStream().use { salida ->
                entrada.copyTo(salida)
            }
        }
        return destino.absolutePath
    }

    companion object {
        private const val DIRECTORIO_IMAGENES = "imagenes_incidencias"
        private const val PREFIJO_ARCHIVO = "incidencia_"
    }
}
