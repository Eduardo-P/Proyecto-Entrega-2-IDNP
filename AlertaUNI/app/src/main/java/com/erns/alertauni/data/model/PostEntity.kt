package com.erns.alertauni.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PostEntity(
    @SerialName("post_id") val id: Int,
    @SerialName("author_name") val authorName: String? = "",
    @SerialName("author_type") val authorType: Int,
    @SerialName("course_code") val courseCode: String,
    @SerialName("course_name") val courseName: String? = "",
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("allow_comments") val allowComments: Boolean,
    @SerialName("count_comments") val countComments: Int,
    @SerialName("is_private") val isPrivate: Boolean,
    @SerialName("published") val date: String,
    @SerialName("receiver_private") val receiverPrivate: String? = "",
    // Campos nuevos del Muro rediseñado. El backend actual NO los devuelve: llegan con su
    // valor por defecto y se completan con la capa local SIMULADA (PostExtraEntity).
    // Si el backend los agrega en el futuro, se leerán directamente de la respuesta.
    @SerialName("priority") val priority: Int = Prioridad.MEDIA,
    @SerialName("archived_at") val archivedAt: String? = null,
    @SerialName("receiver_name") val receiverName: String? = null
) {
    val isArchived: Boolean get() = archivedAt != null
}

/** Niveles de prioridad, con los mismos valores propuestos para la columna `priority` del backend. */
object Prioridad {
    const val ALTA = 1
    const val MEDIA = 2
    const val BAJA = 3
    val todas = listOf(ALTA, MEDIA, BAJA)

    fun etiqueta(p: Int): String = when (p) {
        ALTA -> "Alta"
        BAJA -> "Baja"
        else -> "Media"
    }
}

/** Datos para publicar un anuncio desde el formulario único (público o privado). */
data class NuevoAnuncioRequest(
    val courseCatalogId: String,
    val courseCode: String,
    val title: String,
    val content: String,
    val isPrivate: Boolean,
    val receiverId: String?,
    val receiverName: String?,
    val priority: Int,
    val allowComments: Boolean
)
