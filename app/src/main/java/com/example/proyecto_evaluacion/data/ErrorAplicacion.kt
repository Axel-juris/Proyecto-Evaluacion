package com.example.proyecto_evaluacion.data

/** Error ya traducido a un mensaje comprensible para el usuario. */
class ErrorAplicacion(
    val codigoHttp: Int?,
    override val message: String
) : Exception(message)