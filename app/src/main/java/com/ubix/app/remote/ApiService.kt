package com.ubix.app.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("auth/login")
    fun login(@Body solicitud: LoginRequest): Call<Void>

    @GET("catalogos")
    fun obtenerCatalogos(): Call<CatalogosResponse>

    @GET("incidencias")
    fun obtenerIncidencias(): Call<Void>

    @POST("incidencias")
    fun crearIncidencia(@Body solicitud: IncidenciaRequest): Call<Void>

    @Headers("X-HTTP-Method-Override: PATCH")
    @POST("incidencias/{uuid}/estado")
    fun actualizarEstado(
        @Path("uuid") clientUuid: String,
        @Body solicitud: EstadoRequest
    ): Call<Void>
}