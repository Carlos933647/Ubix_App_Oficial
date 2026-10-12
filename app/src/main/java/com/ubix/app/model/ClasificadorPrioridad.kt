package com.ubix.app.model

class ClasificadorPrioridad {

    fun determinarPrioridad(
        descripcion: String,
        prioridadAlta: String,
        prioridadMedia: String,
        prioridadBaja: String
    ): String {
        val texto = descripcion.lowercase()

        val esUrgente =
            "urgente" in texto ||
                    "caído" in texto ||
                    "caido" in texto ||
                    "sin alumbrado" in texto ||
                    "fuga" in texto ||
                    "hueco grande" in texto

        val atencionPronta =
            "bache" in texto ||
                    "rajadura" in texto ||
                    "apagada" in texto ||
                    "falla" in texto ||
                    "obstáculo" in texto ||
                    "obstaculo" in texto

        return when {
            esUrgente -> prioridadAlta
            atencionPronta -> prioridadMedia
            else -> prioridadBaja
        }
    }

}