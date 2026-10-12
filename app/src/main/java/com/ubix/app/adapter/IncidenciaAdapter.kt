package com.ubix.app.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.ubix.app.databinding.ItemIncidenciaBinding
import com.ubix.app.model.Incidencia
import java.io.File
import com.ubix.app.R

class IncidenciaAdapter(
    private val onIncidenciaClick: (Incidencia) -> Unit
) : RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder>() {

    private val incidencias = mutableListOf<Incidencia>()

    fun actualizar(nuevasIncidencias: List<Incidencia>) {
        val cantidadAnterior = incidencias.size
        incidencias.clear()
        if (cantidadAnterior > 0) {
            notifyItemRangeRemoved(0, cantidadAnterior)
        }

        incidencias.addAll(nuevasIncidencias)
        if (incidencias.isNotEmpty()) {
            notifyItemRangeInserted(0, incidencias.size)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IncidenciaAdapter.IncidenciaViewHolder {
        val binding = ItemIncidenciaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IncidenciaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncidenciaAdapter.IncidenciaViewHolder, position: Int) {
        holder.vincular(incidencias[position])
    }

    override fun getItemCount(): Int {
        return incidencias.size
    }

    inner class IncidenciaViewHolder(
        private val binding: ItemIncidenciaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun vincular(incidencia: Incidencia) {
            val context = binding.root.context

            binding.tvItemIcono.text = incidencia.categoria.take(1).uppercase()
            binding.tvItemCategoria.text = incidencia.categoria
            binding.tvItemZona.text = context.getString(
                R.string.item_zona_formato,
                incidencia.zona,
                incidencia.prioridad
            )
            binding.tvItemDescripcion.text = incidencia.descripcion

            val archivo = incidencia.fotoPath?.let(::File)?.takeIf { it.exists() }
            if (archivo == null) {
                binding.imageMini.visibility = View.GONE
                binding.imageMini.setImageDrawable(null)
            } else {
                binding.imageMini.visibility = View.VISIBLE
                binding.imageMini.setImageURI(Uri.fromFile(archivo))
            }

            val colorPrioridad = when (incidencia.prioridad) {
                context.getString(R.string.prioridad_alta) -> R.color.prio_alta
                context.getString(R.string.prioridad_media) -> R.color.prio_media
                else -> R.color.prio_baja
            }
            binding.viewPrioridad.setBackgroundColor(context.getColor(colorPrioridad))

            val colorEstado = context.getColor(
                when (incidencia.estado) {
                    context.getString(R.string.estado_solucionada) -> R.color.status_solucionada
                    context.getString(R.string.estado_en_proceso) -> R.color.status_en_proceso
                    else -> R.color.status_reportada
                }
            )
            binding.tvItemEstado.text = incidencia.estado
            binding.tvItemEstado.setTextColor(colorEstado)
            binding.tvItemEstado.background.mutate()
                .setTint(ColorUtils.setAlphaComponent(colorEstado, 38))

            if (incidencia.sincronizada) {
                binding.tvItemSync.text = context.getString(R.string.item_sync_ok)
                binding.tvItemSync.setTextColor(context.getColor(R.color.status_solucionada))
            } else {
                binding.tvItemSync.text = context.getString(R.string.item_sync_pend)
                binding.tvItemSync.setTextColor(context.getColor(R.color.status_en_proceso))
            }

            binding.root.setOnClickListener {
                onIncidenciaClick(incidencia)
            }
        }
    }

}

