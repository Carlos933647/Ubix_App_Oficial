package com.ubix.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.ubix.app.databinding.ActivityMainBinding
import com.ubix.app.ui.DetalleIncidenciaFragment
import com.ubix.app.ui.RegistroIncidenciaFragment
import com.ubix.app.ui.ListaIncidenciasFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val correo = intent.getStringExtra(EXTRA_CORREO) ?: getString(R.string.sin_dato)
            mostrarFragmento(ListaIncidenciasFragment.nueva(correo), guardarEnPila = false)
        }
    }
    fun mostrarRegistro(idEdicion: Long = 0L) {
        mostrarFragmento(RegistroIncidenciaFragment.nueva(idEdicion))
    }
    fun mostrarDetalle(id: Long) {
        mostrarFragmento(DetalleIncidenciaFragment.nueva(id))
    }
    fun editarDesdeDetalle(id: Long) {
        supportFragmentManager.popBackStack()
        mostrarRegistro(id)
    }
    fun volver() {
        supportFragmentManager.popBackStack()
    }
    private fun mostrarFragmento(fragment: Fragment, guardarEnPila: Boolean = true) {
        val transaccion = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
        if (guardarEnPila) {
            transaccion.addToBackStack(null)
        }
        transaccion.commit()
    }

    companion object {
        const val EXTRA_CORREO = "extra_correo"
    }
}