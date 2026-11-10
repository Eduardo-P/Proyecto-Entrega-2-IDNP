package com.erns.alertauni.domain.muro

import com.erns.alertauni.data.model.PostEntity

/**
 * Estado de búsqueda y filtros del muro (Problema 2, alternativa A del examen).
 * Los criterios se combinan con AND.
 */
data class FiltroMuro(
    val texto: String = "",
    val curso: String? = null,        // código del curso (p. ej. "INF-01")
    val privado: Boolean? = null,     // null = todos; true = privados; false = públicos
    val prioridad: Int? = null,
    val archivados: Boolean = false   // false = muro principal; true = solo archivados
) {
    /** Hay algún criterio aparte de la vista de archivados. */
    val tieneCriterios: Boolean
        get() = texto.isNotBlank() || curso != null || privado != null || prioridad != null

    fun limpio(): FiltroMuro = FiltroMuro(archivados = archivados)
}

/** Filtra en el cliente: el muro de un docente tiene decenas o cientos de anuncios. */
fun List<PostEntity>.filtrar(f: FiltroMuro): List<PostEntity> = filter { a ->
    a.isArchived == f.archivados &&
        (f.curso == null || a.courseCode == f.curso) &&
        (f.privado == null || a.isPrivate == f.privado) &&
        (f.prioridad == null || a.priority == f.prioridad) &&
        (TextoBusqueda.contiene(a.title, f.texto) || TextoBusqueda.contiene(a.content, f.texto))
}
