package com.ubix.app.remote

import com.ubix.app.model.Incidencia
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ApiCliente {

    fun login(correo: String, clave: String, callback: (Boolean, String) -> Unit) {
        RetrofitClient.api.login(LoginRequest(correo, clave)).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                val codigo = response.code()
                if (codigo == 200 || codigo == 201) {
                    callback(true, "ok")
                } else {
                    callback(false, "credenciales")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {

                callback(false, "sin_conexion")
            }
        })
    }
    fun obtenerCatalogos(callback: (Boolean, List<String>, List<String>) -> Unit) {
        RetrofitClient.api.obtenerCatalogos().enqueue(object : Callback<CatalogosResponse> {
            override fun onResponse(
                call: Call<CatalogosResponse>,
                response: Response<CatalogosResponse>
            ) {
                val cuerpo = response.body()
                if (response.code() == 200 && cuerpo != null) {
                    callback(true, limpiarLista(cuerpo.categorias), limpiarLista(cuerpo.zonas))
                } else {
                    callback(false, emptyList(), emptyList())
                }
            }

            override fun onFailure(call: Call<CatalogosResponse>, t: Throwable) {
                callback(false, emptyList(), emptyList())
            }
        })
    }
    fun obtenerIncidencias(callback: (Boolean, String) -> Unit) {
        ejecutar(RetrofitClient.api.obtenerIncidencias(), setOf(200), callback)
    }
    fun enviarIncidencia(incidencia: Incidencia, callback: (Boolean, String) -> Unit) {
        val solicitud = IncidenciaRequest(
            client_uuid = incidencia.uuid,
            categoria = incidencia.categoria,
            zona = incidencia.zona,
            descripcion = incidencia.descripcion,
            prioridad_sugerida = incidencia.prioridad.lowercase(),
            estado = incidencia.estado,
            latitud = incidencia.latitud,
            longitud = incidencia.longitud,
            foto = incidencia.fotoPath?.let { java.io.File(it).name }
        )

        ejecutar(RetrofitClient.api.crearIncidencia(solicitud), setOf(200, 201, 409), callback)
    }

    fun actualizarEstado(clientUuid: String, nuevoEstado: String, callback: (Boolean, String) -> Unit) {
        val llamada = RetrofitClient.api.actualizarEstado(clientUuid, EstadoRequest(nuevoEstado))
        ejecutar(llamada, setOf(200), callback)
    }
    private fun ejecutar(
        llamada: Call<Void>,
        codigosValidos: Set<Int>,
        callback: (Boolean, String) -> Unit
    ) {
        llamada.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                val codigo = response.code()
                callback(codigo in codigosValidos, "http_$codigo")
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                callback(false, "sin_conexion")
            }
        })
    }
    private fun limpiarLista(lista: List<String?>?): List<String> {
        return lista.orEmpty().filterNotNull().filter { it.isNotBlank() }
    }
}