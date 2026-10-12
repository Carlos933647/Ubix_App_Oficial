package com.ubix.app.data

object IncidenciaContract {
    const val NOMBRE_BASE = "urbix.db"
    const val VERSION_BASE = 3

    const val TABLA_BORRADORES = "borradores"
    const val COL_ID = "_id"
    const val COL_UUID = "uuid"
    const val COL_CATEGORIA = "categoria"
    const val COL_ZONA = "zona"
    const val COL_DESCRIPCION = "descripcion"
    const val COL_PRIORIDAD = "prioridad"
    const val COL_ESTADO = "estado"
    const val COL_SINCRONIZADA = "sincronizada"
    const val COL_LATITUD = "latitud"
    const val COL_LONGITUD = "longitud"
    const val COL_FOTO = "foto_path"

    const val SQL_CREAR_TABLA =
        "CREATE TABLE $TABLA_BORRADORES (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_UUID TEXT UNIQUE NOT NULL, " +
                "$COL_CATEGORIA TEXT NOT NULL, " +
                "$COL_ZONA TEXT NOT NULL, " +
                "$COL_DESCRIPCION TEXT NOT NULL, " +
                "$COL_PRIORIDAD TEXT NOT NULL, " +
                "$COL_ESTADO TEXT NOT NULL, " +
                "$COL_SINCRONIZADA INTEGER DEFAULT 0, " +
                "$COL_LATITUD REAL, " +
                "$COL_LONGITUD REAL, " +
                "$COL_FOTO TEXT)"

    const val SQL_ELIMINAR_TABLA = "DROP TABLE IF EXISTS $TABLA_BORRADORES"
}