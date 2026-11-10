package com.erns.alertauni.common

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Fechas tal como las maneja el backend: texto `yyyy-MM-dd HH:mm:ss` sin zona horaria.
 *
 * Supuesto documentado: las Edge Functions generan la fecha con `toISOString()`, es decir,
 * en **UTC**. Para mostrarla se convierte a la hora de Perú.
 */
object FechaBackend {
    val ZONA_SERVIDOR: ZoneId = ZoneOffset.UTC
    val ZONA_LOCAL: ZoneId = ZoneId.of("America/Lima")
    private val FORMATO: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /** Convierte el texto del backend a fecha local. Devuelve null si el formato no es válido. */
    fun parse(texto: String?): ZonedDateTime? = try {
        if (texto.isNullOrBlank()) null
        else LocalDateTime.parse(texto.trim().take(19).replace('T', ' '), FORMATO)
            .atZone(ZONA_SERVIDOR)
            .withZoneSameInstant(ZONA_LOCAL)
    } catch (e: Exception) {
        null
    }

    /** Fecha y hora actual en el formato del backend (UTC). */
    fun ahora(): String = LocalDateTime.now(ZONA_SERVIDOR).format(FORMATO)

    fun format(fecha: ZonedDateTime): String =
        fecha.withZoneSameInstant(ZONA_SERVIDOR).toLocalDateTime().format(FORMATO)
}
