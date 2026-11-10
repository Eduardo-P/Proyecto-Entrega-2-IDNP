package com.erns.alertauni.data.repository

import com.erns.alertauni.common.AppEvent
import com.erns.alertauni.common.AppEventManager
import com.erns.alertauni.common.FechaBackend
import com.erns.alertauni.data.local.PostExtraDao
import com.erns.alertauni.data.local.PostExtraEntity
import com.erns.alertauni.data.local.conExtra
import com.erns.alertauni.data.model.BackendException
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.NuevoAnuncioRequest
import com.erns.alertauni.data.model.PostAddResponse
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.data.model.PostPrivateRequest
import com.erns.alertauni.data.model.PostRequest
import com.erns.alertauni.data.remote.PostDataSource
import javax.inject.Inject

/**
 * Repositorio de anuncios contra el backend real (Supabase Edge Functions).
 *
 * - REAL: listar (`service-posts`), publicar público (`service-post-add`) y privado
 *   (`service-post-private-add`), catálogo de cursos.
 * - SIMULADO (PostExtraDao, local): prioridad, archivado, habilitar/deshabilitar comentarios
 *   y nombre del destinatario, porque el backend aún no los soporta.
 */
class PostRepositoryImpl @Inject constructor(
    private val postDataSource: PostDataSource,
    private val postExtraDao: PostExtraDao
) : PostRepository {

    override suspend fun getPosts(): Result<List<PostEntity>> {
        return try {
            val posts = postDataSource.callPostsEndpoint().data
            val extras = postExtraDao.getAll().associateBy { it.postId }
            // Antes una lista vacía se devolvía como error ("No posts found").
            Result.success(posts.map { it.conExtra(extras[it.id]) })
        } catch (e: BackendException) {
            if (e.errorData.code == "PROFILE_NOT_FOUND") {
                // Disparamos el evento global de navegación asíncronamente
                AppEventManager.emit(AppEvent.NavigateToCompleteProfile)
            }
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPost(postId: Int): Result<PostEntity> =
        getPosts().mapCatching { lista ->
            lista.firstOrNull { it.id == postId } ?: throw NoSuchElementException("Anuncio no encontrado")
        }

    override suspend fun getCourseCatalog(): Result<List<CatalogCourseEntity>> {
        return try {
            Result.success(postDataSource.callCourseCatalogEndpoint().data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun publish(request: NuevoAnuncioRequest): Result<PostAddResponse> {
        return try {
            val respuesta = if (request.isPrivate) {
                postDataSource.callInsertPrivateEndpoint(
                    PostPrivateRequest(
                        courseCatalogId = request.courseCatalogId,
                        title = request.title,
                        content = request.content,
                        receiverPrivate = requireNotNull(request.receiverId) { "Falta el estudiante" },
                        allowComments = request.allowComments
                    )
                ).data
            } else {
                postDataSource.callInsertEndpoint(
                    PostRequest(
                        course_catalog_id = request.courseCatalogId,
                        title = request.title,
                        content = request.content,
                        is_private = false,
                        allow_comments = request.allowComments
                    )
                ).data
            }
            // SIMULADO: la prioridad y el nombre del destinatario se guardan localmente.
            postExtraDao.upsert(
                PostExtraEntity(
                    postId = respuesta.post_id,
                    priority = request.priority,
                    receiverName = request.receiverName
                )
            )
            Result.success(respuesta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // SIMULADO: el backend no tiene archived_at; se marca solo en este dispositivo.
    override suspend fun setArchived(postId: Int, archived: Boolean): Result<Unit> = runCatching {
        val actual = postExtraDao.get(postId) ?: PostExtraEntity(postId)
        postExtraDao.upsert(actual.copy(archivedAt = if (archived) FechaBackend.ahora() else null))
    }

    // SIMULADO: no hay endpoint para cambiar allow_comments; se guarda solo en este dispositivo.
    override suspend fun setAllowComments(postId: Int, allow: Boolean): Result<Unit> = runCatching {
        val actual = postExtraDao.get(postId) ?: PostExtraEntity(postId)
        postExtraDao.upsert(actual.copy(allowComments = allow))
    }
}
