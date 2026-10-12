package com.ubix.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ubix.app.data.IncidenciaContract.COL_FOTO
import com.ubix.app.data.IncidenciaContract.COL_LATITUD
import com.ubix.app.data.IncidenciaContract.COL_LONGITUD
import com.ubix.app.data.IncidenciaContract.NOMBRE_BASE
import com.ubix.app.data.IncidenciaContract.SQL_CREAR_TABLA
import com.ubix.app.data.IncidenciaContract.SQL_ELIMINAR_TABLA
import com.ubix.app.data.IncidenciaContract.TABLA_BORRADORES
import com.ubix.app.data.IncidenciaContract.VERSION_BASE

class IncidenciaDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BASE, null, VERSION_BASE) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_TABLA)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        if (oldVersion < 2) {
            db.execSQL(SQL_ELIMINAR_TABLA)
            onCreate(db)
            return
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLA_BORRADORES ADD COLUMN $COL_LATITUD REAL")
            db.execSQL("ALTER TABLE $TABLA_BORRADORES ADD COLUMN $COL_LONGITUD REAL")
            db.execSQL("ALTER TABLE $TABLA_BORRADORES ADD COLUMN $COL_FOTO TEXT")
        }
    }
}