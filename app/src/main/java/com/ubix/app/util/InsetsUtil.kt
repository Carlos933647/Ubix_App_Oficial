package com.ubix.app.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object InsetsUtil {

    fun aplicar(vista: View, arriba: Boolean = true, abajo: Boolean = true) {
        val arribaInicial = vista.paddingTop
        val abajoInicial = vista.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(vista) { v, insets ->
            val barras = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                v.paddingLeft,
                arribaInicial + if (arriba) barras.top else 0,
                v.paddingRight,
                abajoInicial + if (abajo) maxOf(barras.bottom, teclado.bottom) else 0
            )
            insets
        }

        ViewCompat.requestApplyInsets(vista)
    }
}