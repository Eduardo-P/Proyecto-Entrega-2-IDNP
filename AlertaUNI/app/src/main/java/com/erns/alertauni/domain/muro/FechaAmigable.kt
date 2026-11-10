package com.erns.alertauni.domain.muro

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Grupos de la lista del muro (encabezados fijos). */
enum class GrupoFecha(val titulo: String) {
    HOY("HOY"),
    ESTA_SEMANA("ESTA SEMANA"),
    ANTERIORES("ANTERIORES")
}

/**
 * Fecha amigable para el docente («Hoy, 09:15», «Ayer, 18:20», «Lun 1 jun, 18:40»).
 * Siempre se calcula respecto a `ahora`, que el ViewModel recalcula al recargar,
 * así «Hoy» y «Ayer» no quedan desactualizados al cambiar el día.
 */
object FechaAmigable {
    private val ES = Locale("es", "PE")
    private val HORA = DateTimeFormatter.ofPattern("HH:mm", ES)
    private val DIA_SEMANA = DateTimeFormatter.ofPattern("EEE d MMM", ES)
    private val DIA_ANIO = DateTimeFormatter.ofPattern("d MMM yyyy", ES)

    fun formatear(fecha: ZonedDateTime?, ahora: ZonedDateTime): String {
        if (fecha == null) return ""
        val dias = ChronoUnit.DAYS.between(fecha.toLocalDate(), ahora.toLocalDate())
        val hora = fecha.format(HORA)
        return when {
            dias <= 0L && ChronoUnit.MINUTES.between(fecha, ahora) in 0..1 -> "Ahora"
            dias <= 0L -> "Hoy, $hora"
            dias == 1L -> "Ayer, $hora"
            fecha.year == ahora.year -> "${limpiar(fecha.format(DIA_SEMANA))}, $hora"
            else -> limpiar(fecha.format(DIA_ANIO))
        }
    }

    /** Solo día y mes, para textos como «Archivado el 2 jun». */
    fun corta(fecha: ZonedDateTime?, ahora: ZonedDateTime): String {
        if (fecha == null) return ""
        val patron = if (fecha.year == ahora.year) "d MMM" else "d MMM yyyy"
        return limpiar(fecha.format(DateTimeFormatter.ofPattern(patron, ES)))
    }

    fun grupo(fecha: ZonedDateTime?, ahora: ZonedDateTime): GrupoFecha {
        if (fecha == null) return GrupoFecha.ANTERIORES
        val dias = ChronoUnit.DAYS.between(fecha.toLocalDate(), ahora.toLocalDate())
        return when {
            dias <= 0L -> GrupoFecha.HOY
            dias < 7L -> GrupoFecha.ESTA_SEMANA
            else -> GrupoFecha.ANTERIORES
        }
    }

    // "lun. 1 jun." -> "Lun 1 jun" (según la versión de Android, el mes/día abreviado lleva punto)
    private fun limpiar(s: String): String =
        s.replace(".", "").replaceFirstChar { it.titlecase(ES) }
}
