package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.NuevoAnuncioRequest
import com.erns.alertauni.data.model.PostAddResponse
import com.erns.alertauni.data.model.PostEntity

interface PostRepository {
    /** Anuncios visibles para el usuario. Una lista vacía es un resultado válido, no un error. */
    suspend fun getPosts(): Result<List<PostEntity>>

    suspend fun getPost(postId: Int): Result<PostEntity>

    suspend fun getCourseCatalog(): Result<List<CatalogCourseEntity>>

    /** Publica un anuncio público (todo el curso) o privado (un estudiante). */
    suspend fun publish(request: NuevoAnuncioRequest): Result<PostAddResponse>

    suspend fun setArchived(postId: Int, archived: Boolean): Result<Unit>

    suspend fun setAllowComments(postId: Int, allow: Boolean): Result<Unit>
}
