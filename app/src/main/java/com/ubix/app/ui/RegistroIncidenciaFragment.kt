package com.ubix.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.gms.location.LocationServices
import com.ubix.app.MainActivity
import com.ubix.app.R
import com.ubix.app.data.ImagenStorage
import com.ubix.app.data.IncidenciaStore
import com.ubix.app.databinding.FragmentRegistroIncidenciaBinding
import com.ubix.app.model.ClasificadorPrioridad
import com.ubix.app.model.IncidenciaFactory
import com.ubix.app.remote.ApiCliente
import com.ubix.app.util.InsetsUtil
import java.io.File

class RegistroIncidenciaFragment : Fragment() {

    private var _binding: FragmentRegistroIncidenciaBinding? = null
    private val binding get() = _binding!!

    private lateinit var incidenciaStore: IncidenciaStore
    private lateinit var imagenStorage: ImagenStorage
    private val clasificador = ClasificadorPrioridad()

    private var idEdicion: Long = 0L
    private var uriFoto: Uri? = null
    private var latitudActual: Double? = null
    private var longitudActual: Double? = null

    private val selectorFoto = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            uriFoto = uri
            _binding?.imageFoto?.apply {
                setPadding(0, 0, 0, 0)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
            }
        }
    }

    private val permisoUbicacion =
        registerForActivityResult(RequestMultiplePermissions()) { permisos ->
            if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                obtenerUbicacion()
            } else {
                _binding?.tvGpsInfo?.text = getString(R.string.gps_error)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incidenciaStore = IncidenciaStore(requireContext().applicationContext)
        imagenStorage = ImagenStorage(requireContext().applicationContext)
        idEdicion = arguments?.getLong(ARG_ID_EDICION, 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroIncidenciaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        InsetsUtil.aplicar(binding.tvHeaderRegistro, arriba = true, abajo = false)
        InsetsUtil.aplicar(binding.contenidoRegistro, arriba = false, abajo = true)

        cargarCatalogos()
        configurarEventos()
        if (idEdicion != 0L) {
            cargarParaEdicion()
        }
    }

    private fun cargarCatalogos() {
        configurarSpinners(
            resources.getStringArray(R.array.categorias).toList(),
            resources.getStringArray(R.array.zonas).toList()
        )

        ApiCliente.obtenerCatalogos { exito, categorias, zonas ->
            val bindingActual = _binding ?: return@obtenerCatalogos
            if (exito && categorias.isNotEmpty() && zonas.isNotEmpty()) {
                val categoriaActual = bindingActual.spnCategoria.selectedItem?.toString()
                val zonaActual = bindingActual.spnZona.selectedItem?.toString()
                configurarSpinners(
                    incluirValor(categorias, categoriaActual),
                    incluirValor(zonas, zonaActual)
                )
                seleccionarValor(bindingActual.spnCategoria, categoriaActual)
                seleccionarValor(bindingActual.spnZona, zonaActual)
                Toast.makeText(requireContext(), R.string.msg_catalogos_ok, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarSpinners(categorias: List<String>, zonas: List<String>) {
        val adaptadorCategorias = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, categorias
        )
        adaptadorCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnCategoria.adapter = adaptadorCategorias

        val adaptadorZonas = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, zonas
        )
        adaptadorZonas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnZona.adapter = adaptadorZonas
    }

    private fun incluirValor(lista: List<String>, valor: String?): List<String> {
        return if (valor != null && valor !in lista) listOf(valor) + lista else lista
    }

    private fun seleccionarValor(spinner: Spinner, valor: String?) {
        if (valor == null) return
        for (posicion in 0 until spinner.count) {
            if (spinner.getItemAtPosition(posicion).toString() == valor) {
                spinner.setSelection(posicion)
                return
            }
        }
    }

    private fun configurarEventos() {
        binding.edtDescripcion.doAfterTextChanged {
            binding.tilDescripcion.error = null
        }
        binding.btnFoto.setOnClickListener {
            selectorFoto.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
        binding.btnGps.setOnClickListener {
            pedirUbicacion()
        }
        binding.btnGuardarBorrador.setOnClickListener {
            guardarBorrador()
        }
    }

    private fun tienePermisoUbicacion(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun pedirUbicacion() {
        if (tienePermisoUbicacion()) {
            obtenerUbicacion()
        } else {
            permisoUbicacion.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun obtenerUbicacion() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _binding?.tvGpsInfo?.text = getString(R.string.gps_error)
            return
        }

        LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
            .addOnSuccessListener { ubicacion ->
                val bindingActual = _binding ?: return@addOnSuccessListener
                if (ubicacion == null) {
                    bindingActual.tvGpsInfo.text = getString(R.string.gps_error)
                } else {
                    latitudActual = ubicacion.latitude
                    longitudActual = ubicacion.longitude
                    bindingActual.tvGpsInfo.text = getString(
                        R.string.gps_formato,
                        ubicacion.latitude,
                        ubicacion.longitude
                    )
                }
            }
            .addOnFailureListener {
                _binding?.tvGpsInfo?.text = getString(R.string.gps_error)
            }
    }

    private fun cargarParaEdicion() {
        val incidencia = incidenciaStore.obtenerPorId(idEdicion) ?: return
        binding.tvHeaderRegistro.setText(R.string.title_editar)
        binding.edtDescripcion.setText(incidencia.descripcion)
        binding.btnGuardarBorrador.setText(R.string.btn_actualizar)
        seleccionarValor(binding.spnCategoria, incidencia.categoria)
        seleccionarValor(binding.spnZona, incidencia.zona)
        latitudActual = incidencia.latitud
        longitudActual = incidencia.longitud
        if (latitudActual != null && longitudActual != null) {
            binding.tvGpsInfo.text =
                getString(R.string.gps_formato, latitudActual!!, longitudActual!!)
        }
        incidencia.fotoPath?.let(::File)?.takeIf { it.exists() }?.let { archivo ->
            binding.imageFoto.apply {
                setPadding(0, 0, 0, 0)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(Uri.fromFile(archivo))
            }
        }
    }

    private fun guardarBorrador() {
        val categoria = binding.spnCategoria.selectedItem.toString()
        val zona = binding.spnZona.selectedItem.toString()
        val descripcion = binding.edtDescripcion.text?.toString()?.trim().orEmpty()

        if (descripcion.length < DESCRIPCION_MINIMA) {
            binding.tilDescripcion.error = getString(R.string.error_descripcion)
            return
        }

        val prioridad = clasificador.determinarPrioridad(
            descripcion = descripcion,
            prioridadAlta = getString(R.string.prioridad_alta),
            prioridadMedia = getString(R.string.prioridad_media),
            prioridadBaja = getString(R.string.prioridad_baja)
        )

        if (idEdicion == 0L) {
            val rutaFoto = uriFoto?.let { imagenStorage.guardarImagen(it) }
            val incidencia = IncidenciaFactory.crearIncidencia(
                categoria = categoria,
                zona = zona,
                descripcion = descripcion,
                prioridad = prioridad,
                estado = getString(R.string.estado_reportada),
                latitud = latitudActual,
                longitud = longitudActual,
                fotoPath = rutaFoto
            )
            val nuevoId = incidenciaStore.insertar(incidencia)
            if (nuevoId > 0) {
                Toast.makeText(requireContext(), R.string.msg_guardado, Toast.LENGTH_SHORT).show()
                (requireActivity() as MainActivity).volver()
            } else {
                Toast.makeText(requireContext(), R.string.msg_error_guardar, Toast.LENGTH_SHORT).show()
            }
        } else {
            val existente = incidenciaStore.obtenerPorId(idEdicion) ?: return
            val actualizada = existente.copy(
                categoria = categoria,
                zona = zona,
                descripcion = descripcion,
                prioridad = prioridad,
                sincronizada = false
            )
            incidenciaStore.actualizar(actualizada)
            Toast.makeText(requireContext(), R.string.msg_actualizado, Toast.LENGTH_SHORT).show()
            (requireActivity() as MainActivity).volver()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID_EDICION = "arg_id_edicion"
        private const val DESCRIPCION_MINIMA = 8

        fun nueva(idEdicion: Long): RegistroIncidenciaFragment {
            return RegistroIncidenciaFragment().apply {
                arguments = Bundle().apply { putLong(ARG_ID_EDICION, idEdicion) }
            }
        }
    }
}