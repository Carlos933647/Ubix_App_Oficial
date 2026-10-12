package com.ubix.app.model

import java.util.UUID

object IncidenciaFactory {

    fun crearIncidencia(
        categoria: String,
        zona: String,
        descripcion: String,
        prioridad: String,
        estado: String,
        uuid: String = UUID.randomUUID().toString(),
        id: Long = 0L,
        sincronizada: Boolean = false,
        latitud: Double? = null,
        longitud: Double? = null,
        fotoPath: String? = null
    ): Incidencia {
        return Incidencia(
            id = id,
            uuid = uuid,
            categoria = categoria,
            zona = zona,
            descripcion = descripcion,
            prioridad = prioridad,
            estado = estado,
            sincronizada = sincronizada,
            latitud = latitud,
            longitud = longitud,
            fotoPath = fotoPath
        )
    }
}
