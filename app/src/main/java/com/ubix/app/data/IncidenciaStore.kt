package com.ubix.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.ubix.app.data.IncidenciaContract.COL_CATEGORIA
import com.ubix.app.data.IncidenciaContract.COL_DESCRIPCION
import com.ubix.app.data.IncidenciaContract.COL_ESTADO
import com.ubix.app.data.IncidenciaContract.COL_FOTO
import com.ubix.app.data.IncidenciaContract.COL_ID
import com.ubix.app.data.IncidenciaContract.COL_LATITUD
import com.ubix.app.data.IncidenciaContract.COL_LONGITUD
import com.ubix.app.data.IncidenciaContract.COL_PRIORIDAD
import com.ubix.app.data.IncidenciaContract.COL_SINCRONIZADA
import com.ubix.app.data.IncidenciaContract.COL_UUID
import com.ubix.app.data.IncidenciaContract.COL_ZONA
import com.ubix.app.data.IncidenciaContract.TABLA_BORRADORES
import com.ubix.app.model.Incidencia
import kotlin.text.insert

class IncidenciaStore(context: Context) {

    private val dbHelper = IncidenciaDbHelper(context.applicationContext)

    fun insertar(incidencia: Incidencia): Long {
        val valores = ContentValues().apply {
            put(COL_UUID, incidencia.uuid)
            put(COL_CATEGORIA, incidencia.categoria)
            put(COL_ZONA, incidencia.zona)
            put(COL_DESCRIPCION, incidencia.descripcion)
            put(COL_PRIORIDAD, incidencia.prioridad)
            put(COL_ESTADO, incidencia.estado)
            put(COL_SINCRONIZADA, if (incidencia.sincronizada) 1 else 0)
            ponerEvidencia(this, incidencia)
        }
        return dbHelper.writableDatabase.insert(TABLA_BORRADORES, null, valores)
    }

    fun listarTodas(): List<Incidencia> {
        val lista = mutableListOf<Incidencia>()
        dbHelper.readableDatabase.query(
            TABLA_BORRADORES, null, null, null, null, null, "$COL_ID DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leerFila(cursor))
            }
        }
        return lista.toList()
    }

    fun buscarPorTexto(texto: String): List<Incidencia> {
        val lista = mutableListOf<Incidencia>()
        val filtro = "%$texto%"
        dbHelper.readableDatabase.query(
            TABLA_BORRADORES,
            null,
            "$COL_CATEGORIA LIKE ? OR $COL_ZONA LIKE ? OR $COL_DESCRIPCION LIKE ? OR $COL_ESTADO LIKE ?",
            arrayOf(filtro, filtro, filtro, filtro),
            null, null, "$COL_ID DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leerFila(cursor))
            }
        }
        return lista.toList()
    }

    fun obtenerPorId(id: Long): Incidencia? {

        dbHelper.readableDatabase.query(
            TABLA_BORRADORES, null, "$COL_ID = ?",
            arrayOf(id.toString()), null, null, null
        ).use { cursor ->
            return if (cursor.moveToFirst()) leerFila(cursor) else null
        }
    }

    fun actualizar(incidencia: Incidencia): Int {
        val valores = ContentValues().apply {
            put(COL_CATEGORIA, incidencia.categoria)
            put(COL_ZONA, incidencia.zona)
            put(COL_DESCRIPCION, incidencia.descripcion)
            put(COL_PRIORIDAD, incidencia.prioridad)
            put(COL_ESTADO, incidencia.estado)
            put(COL_SINCRONIZADA, 0)
            ponerEvidencia(this, incidencia)
        }
        return dbHelper.writableDatabase.update(
            TABLA_BORRADORES, valores, "$COL_ID = ?",
            arrayOf(incidencia.id.toString())
        )
    }

    fun eliminar(id: Long): Int {
        return dbHelper.writableDatabase.delete(
            TABLA_BORRADORES, "$COL_ID = ?", arrayOf(id.toString())
        )
    }
    fun marcarSincronizada(id: Long): Int {
        val valores = ContentValues().apply {
            put(COL_SINCRONIZADA, 1)
        }
        return dbHelper.writableDatabase.update(
            TABLA_BORRADORES, valores, "$COL_ID = ?",
            arrayOf(id.toString())
        )
    }

    fun contarPendientes(): Int {
        dbHelper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $TABLA_BORRADORES WHERE $COL_SINCRONIZADA = 0", null
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    private fun ponerEvidencia(valores: ContentValues, incidencia: Incidencia) {
        val lat = incidencia.latitud
        val lng = incidencia.longitud
        if (lat != null) valores.put(COL_LATITUD, lat) else valores.putNull(COL_LATITUD)
        if (lng != null) valores.put(COL_LONGITUD, lng) else valores.putNull(COL_LONGITUD)
        val foto = incidencia.fotoPath
        if (foto != null) valores.put(COL_FOTO, foto) else valores.putNull(COL_FOTO)
    }

    private fun leerFila(cursor: Cursor): Incidencia {
        val indiceLat = cursor.getColumnIndexOrThrow(COL_LATITUD)
        val indiceLng = cursor.getColumnIndexOrThrow(COL_LONGITUD)
        val indiceFoto = cursor.getColumnIndexOrThrow(COL_FOTO)
        return Incidencia(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            uuid = cursor.getString(cursor.getColumnIndexOrThrow(COL_UUID)),
            categoria = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORIA)),
            zona = cursor.getString(cursor.getColumnIndexOrThrow(COL_ZONA)),
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION)),
            prioridad = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRIORIDAD)),
            estado = cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO)),
            sincronizada = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SINCRONIZADA)) == 1,
            latitud = if (cursor.isNull(indiceLat)) null else cursor.getDouble(indiceLat),
            longitud = if (cursor.isNull(indiceLng)) null else cursor.getDouble(indiceLng),
            fotoPath = if (cursor.isNull(indiceFoto)) null else cursor.getString(indiceFoto)
        )
    }
}

