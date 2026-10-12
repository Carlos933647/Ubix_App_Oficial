package com.ubix.app.remote


data class IncidenciaRequest(
    val client_uuid: String,
    val categoria: String,
    val zona: String,
    val descripcion: String,
    val prioridad_sugerida: String,
    val estado: String,
    val latitud: Double?,
    val longitud: Double?,
    val foto: String?
)

