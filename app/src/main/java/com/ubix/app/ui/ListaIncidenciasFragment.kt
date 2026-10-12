package com.ubix.app.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.ubix.app.MainActivity
import com.ubix.app.R
import com.ubix.app.adapter.IncidenciaAdapter
import com.ubix.app.data.IncidenciaStore
import com.ubix.app.databinding.FragmentListaIncidenciasBinding
import com.ubix.app.model.Incidencia
import com.ubix.app.remote.ApiCliente
import com.ubix.app.util.InsetsUtil

class ListaIncidenciasFragment : Fragment() {
    private var _binding: FragmentListaIncidenciasBinding? = null
    private val binding get() = _binding!!

    private lateinit var incidenciaAdapter: IncidenciaAdapter
    private lateinit var incidenciaStore: IncidenciaStore

    private var listaCompleta: List<Incidencia> = emptyList()
    private var filtroEstado = FILTRO_TODAS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incidenciaStore = IncidenciaStore(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListaIncidenciasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        InsetsUtil.aplicar(binding.layoutHeader, arriba = true, abajo = false)
        InsetsUtil.aplicar(binding.root, arriba = false, abajo = true)

        binding.tvUsuario.text = arguments?.getString(ARG_CORREO) ?: getString(R.string.sin_dato)

        incidenciaAdapter = IncidenciaAdapter { incidencia ->
            (requireActivity() as MainActivity).mostrarDetalle(incidencia.id)
        }
        configurarRecyclerView()
        configurarBuscador()
        configurarFiltro()
        configurarBotones()
    }

    override fun onResume() {
        super.onResume()
        cargarIncidencias()
    }

    private fun configurarRecyclerView() {
        binding.rvIncidencias.layoutManager = LinearLayoutManager(requireContext())
        binding.rvIncidencias.adapter = incidenciaAdapter
    }

    private fun configurarBuscador() {
        binding.edtBuscar.doAfterTextChanged {
            aplicarFiltros()
        }
    }

    private fun configurarFiltro() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, idsMarcados ->
            filtroEstado = when (idsMarcados.firstOrNull()) {
                R.id.chipPorEnviar -> FILTRO_POR_ENVIAR
                R.id.chipSolucionadas -> FILTRO_SOLUCIONADAS
                else -> FILTRO_TODAS
            }
            aplicarFiltros()
        }
    }

    private fun configurarBotones() {
        binding.btnNuevaIncidencia.setOnClickListener {
            (requireActivity() as MainActivity).mostrarRegistro()
        }
        binding.btnSincronizar.setOnClickListener {
            sincronizarPendientes()
        }
    }

    private fun cargarIncidencias() {
        Thread {
            val incidencias = incidenciaStore.listarTodas()

            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                listaCompleta = incidencias
                aplicarFiltros()
            }
        }.start()
    }

    private fun aplicarFiltros() {
        val bindingActual = _binding ?: return
        val texto = bindingActual.edtBuscar.text?.toString()?.trim().orEmpty().lowercase()
        val solucionada = getString(R.string.estado_solucionada)

        val listaFiltrada = listaCompleta.filter { incidencia ->
            val coincideTexto = incidencia.categoria.lowercase().contains(texto) ||
                    incidencia.zona.lowercase().contains(texto) ||
                    incidencia.descripcion.lowercase().contains(texto) ||
                    incidencia.estado.lowercase().contains(texto)
            val coincideEstado = when (filtroEstado) {
                FILTRO_POR_ENVIAR -> !incidencia.sincronizada
                FILTRO_SOLUCIONADAS -> incidencia.estado == solucionada
                else -> true
            }
            coincideTexto && coincideEstado
        }

        incidenciaAdapter.actualizar(listaFiltrada)
        bindingActual.tvContador.text = getString(
            R.string.contador_formato,
            listaFiltrada.size,
            listaCompleta.count { !it.sincronizada }
        )
        bindingActual.rvIncidencias.visibility =
            if (listaFiltrada.isEmpty()) View.GONE else View.VISIBLE
        bindingActual.layoutVacio.visibility =
            if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun sincronizarPendientes() {
        val pendientes = incidenciaStore.listarTodas().filter { !it.sincronizada }
        if (pendientes.isEmpty()) {
            Toast.makeText(requireContext(), R.string.msg_sin_pendientes, Toast.LENGTH_SHORT).show()
            consultarServidor()
            return
        }

        binding.btnSincronizar.isEnabled = false
        var procesadas = 0
        var sincronizadas = 0

        for (incidencia in pendientes) {
            ApiCliente.enviarIncidencia(incidencia) { exito, _ ->
                procesadas += 1
                if (exito) {
                    incidenciaStore.marcarSincronizada(incidencia.id)
                    sincronizadas += 1
                }
                if (procesadas == pendientes.size) {
                    val bindingActual = _binding ?: return@enviarIncidencia
                    bindingActual.btnSincronizar.isEnabled = true
                    cargarIncidencias()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.msg_sincronizadas_formato, sincronizadas, pendientes.size),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun consultarServidor() {
        ApiCliente.obtenerIncidencias { exitoIncidencias, _ ->
            ApiCliente.obtenerCatalogos { exitoCatalogos, _, _ ->
                if (_binding == null) return@obtenerCatalogos
                val mensaje = if (exitoIncidencias || exitoCatalogos) {
                    R.string.msg_consulta_ok
                } else {
                    R.string.msg_sin_conexion
                }
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CORREO = "arg_correo"
        private const val FILTRO_TODAS = 0
        private const val FILTRO_POR_ENVIAR = 1
        private const val FILTRO_SOLUCIONADAS = 2

        fun nueva(correo: String): ListaIncidenciasFragment {
            return ListaIncidenciasFragment().apply {
                arguments = Bundle().apply { putString(ARG_CORREO, correo) }
            }
        }
    }
}