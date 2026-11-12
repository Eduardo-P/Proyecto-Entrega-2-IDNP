package com.erns.alertauni.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import com.erns.alertauni.data.model.PostEntity

/**
 * SIMULADO: datos del anuncio que el backend actual no guarda.
 *
 * La tabla `post` de Supabase no tiene `priority` ni `archived_at`, no hay endpoint para
 * cambiar `allow_comments` y `get_posts_fun` no devuelve el nombre del destinatario.
 * Mientras eso no exista en el servidor, se guardan aquí, en el dispositivo del docente,
 * asociados al `post_id` real. Solo los ve este dispositivo.
 */
@Entity(tableName = "post_extra")
data class PostExtraEntity(
    @PrimaryKey val postId: Int,
    val priority: Int? = null,
    val archivedAt: String? = null,
    val allowComments: Boolean? = null,
    val receiverName: String? = null
)

@Dao
interface PostExtraDao {
    @Query("SELECT * FROM post_extra")
    suspend fun getAll(): List<PostExtraEntity>

    @Query("SELECT * FROM post_extra WHERE postId = :postId")
    suspend fun get(postId: Int): PostExtraEntity?

    @Upsert
    suspend fun upsert(extra: PostExtraEntity)
}

/** Aplica sobre el anuncio del backend los datos locales que existan. */
fun PostEntity.conExtra(extra: PostExtraEntity?): PostEntity {
    if (extra == null) return this
    return copy(
        priority = extra.priority ?: priority,
        archivedAt = extra.archivedAt ?: archivedAt,
        allowComments = extra.allowComments ?: allowComments,
        receiverName = extra.receiverName ?: receiverName
    )
}
