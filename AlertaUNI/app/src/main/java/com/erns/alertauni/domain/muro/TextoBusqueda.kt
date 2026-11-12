package com.erns.alertauni.domain.muro

import java.text.Normalizer

/**
 * Búsqueda de texto sin distinguir mayúsculas ni tildes, con resaltado en el texto original.
 *
 * Problema (riesgo identificado en el examen): al quitar tildes con Normalizer, el texto
 * normalizado puede tener otra longitud que el original, así que las posiciones de una
 * coincidencia no sirven directamente para resaltar. Por eso cada carácter normalizado
 * guarda el índice del carácter original del que proviene.
 */
object TextoBusqueda {

    class TextoNormalizado(val texto: String, private val origen: IntArray) {
        /** Convierte un rango del texto normalizado a un rango del texto original. */
        fun rangoOriginal(inicio: Int, finExclusivo: Int): IntRange =
            origen[inicio]..origen[finExclusivo - 1]
    }

    private val MARCAS = Regex("\\p{Mn}+")

    fun normalizar(texto: String): String = normalizarConMapa(texto).texto

    fun normalizarConMapa(texto: String): TextoNormalizado {
        val sb = StringBuilder(texto.length)
        val origen = ArrayList<Int>(texto.length)
        texto.forEachIndexed { i, c ->
            val base = MARCAS.replace(Normalizer.normalize(c.toString(), Normalizer.Form.NFD), "")
                .lowercase()
            base.forEach { b ->
                sb.append(b)
                origen.add(i)
            }
        }
        return TextoNormalizado(sb.toString(), origen.toIntArray())
    }

    /** Rangos (en el texto original) donde aparece la consulta. Vacío si no hay consulta. */
    fun coincidencias(texto: String, consulta: String): List<IntRange> {
        val q = normalizar(consulta.trim())
        if (q.isEmpty()) return emptyList()
        val norm = normalizarConMapa(texto)
        val rangos = mutableListOf<IntRange>()
        var desde = 0
        while (true) {
            val i = norm.texto.indexOf(q, desde)
            if (i < 0) break
            rangos.add(norm.rangoOriginal(i, i + q.length))
            desde = i + q.length
        }
        return rangos
    }

    fun contiene(texto: String, consulta: String): Boolean {
        val q = normalizar(consulta.trim())
        return q.isEmpty() || normalizar(texto).contains(q)
    }
}
