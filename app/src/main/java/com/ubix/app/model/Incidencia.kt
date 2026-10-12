package com.ubix.app.model

data class Incidencia(
    val id: Long = 0L,
    val uuid: String,
    val categoria: String,
    val zona: String,
    val descripcion: String,
    val prioridad: String,
    val estado: String,
    val sincronizada: Boolean = false,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoPath: String? = null
)