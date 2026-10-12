package com.ubix.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.ubix.app.databinding.ActivityLoginBinding
import com.ubix.app.remote.ApiCliente
import com.ubix.app.util.InsetsUtil

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InsetsUtil.aplicar(binding.root)

        configurarEventosDeTexto()

        binding.btnIngresar.setOnClickListener {
            intentarIngresar()
        }
    }

    private fun configurarEventosDeTexto() {

        binding.edtCorreo.doAfterTextChanged {
            binding.tilCorreo.error = null
        }
        binding.edtClave.doAfterTextChanged {
            binding.tilClave.error = null
        }
    }

    private fun intentarIngresar() {
        val correo = binding.edtCorreo.text?.toString()?.trim().orEmpty()
        val clave = binding.edtClave.text?.toString()?.trim().orEmpty()

        binding.tilCorreo.error = null
        binding.tilClave.error = null

        var formularioValido = true

        if (correo.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.tilCorreo.error = getString(R.string.error_correo)
            formularioValido = false
        }

        if (clave.length < CLAVE_MINIMA) {
            binding.tilClave.error = getString(R.string.error_clave)
            formularioValido = false
        }

        if (!formularioValido) {
            return
        }
        binding.btnIngresar.isEnabled = false

        if (correo.equals(CORREO_DEMO, ignoreCase = true) && clave == CLAVE_DEMO) {
            abrirPrincipal(correo)
            return
        }

        ApiCliente.login(correo, clave) { exito, _ ->
            binding.btnIngresar.isEnabled = true
            if (exito) {
                abrirPrincipal(correo)
            } else {

                binding.tilClave.error = getString(R.string.error_login_servidor)
            }
        }
    }

    private fun abrirPrincipal(correo: String) {
        binding.btnIngresar.isEnabled = true
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_CORREO, correo)
        }
        startActivity(intent)

        finish()
    }

    companion object {
        private const val CLAVE_MINIMA = 4
        private const val CORREO_DEMO = "campo@urbix.pe"
        private const val CLAVE_DEMO = "1234"
    }
}