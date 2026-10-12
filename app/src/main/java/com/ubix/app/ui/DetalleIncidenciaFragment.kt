package com.ubix.app.ui

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ubix.app.MainActivity
import com.ubix.app.R
import com.ubix.app.data.IncidenciaStore
import com.ubix.app.databinding.FragmentDetalleIncidenciaBinding
import com.ubix.app.model.Incidencia
import com.ubix.app.remote.ApiCliente
import com.ubix.app.util.InsetsUtil
import java.io.File

class DetalleIncidenciaFragment : Fragment() {
    private var _binding: FragmentDetalleIncidenciaBinding? = null
    private val binding get() = _binding!!

    private lateinit var incidenciaStore: IncidenciaStore

    private var incidenciaActual: Incidencia? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incidenciaStore = IncidenciaStore(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetalleIncidenciaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        InsetsUtil.aplicar(binding.tvHeaderDetalle, arriba = true, abajo = false)
        InsetsUtil.aplicar(binding.contenidoDetalle, arriba = false, abajo = true)

        mostrarIncidencia(obtenerIncidencia())
        configurarBotones()
    }

    private fun configurarBotones() {
        binding.btnEnProceso.setOnClickListener {
            cambiarEstado(getString(R.string.estado_en_proceso))
        }
        binding.btnSolucionada.setOnClickListener {
            cambiarEstado(getString(R.string.estado_solucionada))
        }
        binding.btnEditar.setOnClickListener {
            val actual = incidenciaActual ?: return@setOnClickListener
            (requireActivity() as MainActivity).editarDesdeDetalle(actual.id)
        }
        binding.btnEliminar.setOnClickListener {
            confirmarEliminacion()
        }
        binding.btnVolver.setOnClickListener {
            (requireActivity() as MainActivity).volver()
        }
    }

    private fun obtenerIncidencia(): Incidencia {
        val id = arguments?.getLong(ARG_ID, 0L) ?: 0L
        val encontrada = if (id != 0L) incidenciaStore.obtenerPorId(id) else null
        return encontrada ?: Incidencia(
            uuid = getString(R.string.sin_dato),
            categoria = getString(R.string.sin_dato),
            zona = getString(R.string.sin_dato),
            descripcion = getString(R.string.sin_dato),
            prioridad = getString(R.string.prioridad_baja),
            estado = getString(R.string.estado_reportada)
        )
    }

    private fun mostrarIncidencia(incidencia: Incidencia) {
        incidenciaActual = incidencia
        binding.tvCodigoValor.text = getString(R.string.detalle_uuid_formato, incidencia.uuid)
        binding.tvCategoriaValor.text = incidencia.categoria
        binding.tvZonaValor.text = incidencia.zona
        binding.tvDescripcionValor.text = incidencia.descripcion
        binding.tvPrioridadValor.text = incidencia.prioridad
        binding.tvEstadoValor.text = incidencia.estado
        binding.tvEstadoValor.setTextColor(requireContext().getColor(colorDelEstado(incidencia.estado)))

        if (incidencia.latitud != null && incidencia.longitud != null) {
            binding.tvUbicacionValor.text = getString(
                R.string.gps_formato,
                incidencia.latitud,
                incidencia.longitud
            )
        } else {
            binding.tvUbicacionValor.text = getString(R.string.gps_pendiente)
        }

        val archivo = incidencia.fotoPath?.let(::File)?.takeIf { it.exists() }
        if (archivo == null) {
            binding.imageDetalle.visibility = View.GONE
        } else {
            binding.imageDetalle.visibility = View.VISIBLE
            binding.imageDetalle.setImageURI(Uri.fromFile(archivo))
        }

        if (incidencia.sincronizada) {
            binding.tvSyncValor.text = getString(R.string.sync_si)
        } else {
            binding.tvSyncValor.text = getString(R.string.sync_no)
        }
    }

    private fun colorDelEstado(estado: String): Int {
        return when (estado) {
            getString(R.string.estado_solucionada) -> R.color.status_solucionada
            getString(R.string.estado_en_proceso) -> R.color.status_en_proceso
            else -> R.color.status_reportada
        }
    }

    private fun cambiarEstado(nuevoEstado: String) {
        val actual = incidenciaActual ?: return
        val estabaSincronizada = actual.sincronizada
        val actualizada = actual.copy(estado = nuevoEstado, sincronizada = false)

        incidenciaStore.actualizar(actualizada)
        mostrarIncidencia(actualizada)

        if (estabaSincronizada) {
            ApiCliente.actualizarEstado(actual.uuid, nuevoEstado) { exito, _ ->
                if (exito) {
                    incidenciaStore.marcarSincronizada(actual.id)
                    if (_binding != null) {
                        mostrarIncidencia(actualizada.copy(sincronizada = true))
                    }
                }
            }
        }
        Toast.makeText(
            requireContext(),
            getString(R.string.msg_estado_formato, nuevoEstado),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun confirmarEliminacion() {
        val actual = incidenciaActual ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.titulo_eliminar)
            .setMessage(R.string.msg_eliminar)
            .setPositiveButton(R.string.btn_si) { _, _ ->
                actual.fotoPath?.let(::File)?.delete()
                incidenciaStore.eliminar(actual.id)
                Toast.makeText(requireContext(), R.string.msg_eliminado, Toast.LENGTH_SHORT).show()
                (requireActivity() as MainActivity).volver()
            }
            .setNegativeButton(R.string.btn_no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID = "arg_id"

        fun nueva(id: Long): DetalleIncidenciaFragment {
            return DetalleIncidenciaFragment().apply {
                arguments = Bundle().apply { putLong(ARG_ID, id) }
            }
        }
    }
}