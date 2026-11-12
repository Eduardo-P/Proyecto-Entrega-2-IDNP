package com.erns.alertauni.data.demo

import com.erns.alertauni.common.FechaBackend
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.ClassCodeRequest
import com.erns.alertauni.data.model.CommentDto
import com.erns.alertauni.data.model.CommentEntity
import com.erns.alertauni.data.model.CommentRequest
import com.erns.alertauni.data.model.CourseCatalogEntity
import com.erns.alertauni.data.model.CourseCatalogRequest
import com.erns.alertauni.data.model.CourseEnrollRequest
import com.erns.alertauni.data.model.CourseEnrollResponse
import com.erns.alertauni.data.model.NuevoAnuncioRequest
import com.erns.alertauni.data.model.PostAddResponse
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.data.model.PostPrivateRequest
import com.erns.alertauni.data.model.PostPrivateResponse
import com.erns.alertauni.data.model.PostRequest
import com.erns.alertauni.data.model.StudentEnrollment
import com.erns.alertauni.data.model.StudentEntity
import com.erns.alertauni.data.repository.AnnounceRepository
import com.erns.alertauni.data.repository.CommentRepository
import com.erns.alertauni.data.repository.ContactRepository
import com.erns.alertauni.data.repository.CourseRepository
import com.erns.alertauni.data.repository.PostRepository
import com.erns.alertauni.data.repository.StudentRepository
import com.erns.alertauni.domain.manager.DataStoreHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/*
 * SIMULADO: implementaciones del modo demo (BuildConfig.DEMO_MODE, solo debug).
 * Cumplen las mismas interfaces que los repositorios reales, de modo que la interfaz y los
 * ViewModels no saben si hablan con Supabase o con esta base local.
 * Se añade una pequeña latencia para que se vean los estados de carga.
 */

private const val LATENCIA_MS = 350L

@Singleton
class DemoPostRepository @Inject constructor(
    private val dao: DemoDao,
    private val dataStoreHelper: DataStoreHelper
) : PostRepository {

    private val mutex = Mutex()

    private suspend fun asegurarDatos() = mutex.withLock {
        DemoSeeder.sembrarSiVacio(dao, autor = nombreUsuario())
    }

    private suspend fun nombreUsuario(): String =
        "${dataStoreHelper.getFirstname()} ${dataStoreHelper.getSurname()}".trim().ifBlank { "Docente demo" }

    override suspend fun getPosts(): Result<List<PostEntity>> = runCatching {
        asegurarDatos()
        delay(LATENCIA_MS)
        val conteos = dao.conteoComentarios().associate { it.postId to it.total }
        dao.getPosts().map { it.aPostEntity(conteos[it.id] ?: 0) }
    }

    override suspend fun getPost(postId: Int): Result<PostEntity> = runCatching {
        asegurarDatos()
        val post = dao.getPost(postId) ?: throw NoSuchElementException("Anuncio no encontrado")
        post.aPostEntity(dao.getComments(postId).size)
    }

    override suspend fun getCourseCatalog(): Result<List<CatalogCourseEntity>> =
        Result.success(DemoData.catalogo())

    override suspend fun publish(request: NuevoAnuncioRequest): Result<PostAddResponse> = runCatching {
        asegurarDatos()
        delay(LATENCIA_MS)
        val curso = DemoData.curso(request.courseCatalogId)
        val ahora = FechaBackend.ahora()
        val id = dao.insertPost(
            DemoPostEntity(
                courseCatalogId = request.courseCatalogId,
                courseCode = curso?.codigo ?: request.courseCode,
                courseName = curso?.nombre ?: "",
                title = request.title,
                content = request.content,
                isPrivate = request.isPrivate,
                receiverId = request.receiverId,
                receiverName = request.receiverName,
                priority = request.priority,
                allowComments = request.allowComments,
                archivedAt = null,
                published = ahora,
                authorName = nombreUsuario()
            )
        ).toInt()
        PostAddResponse(post_id = id, created_at = ahora)
    }

    override suspend fun setArchived(postId: Int, archived: Boolean): Result<Unit> = runCatching {
        delay(LATENCIA_MS)
        dao.setArchivedAt(postId, if (archived) FechaBackend.ahora() else null)
    }

    override suspend fun setAllowComments(postId: Int, allow: Boolean): Result<Unit> = runCatching {
        delay(LATENCIA_MS)
        dao.setAllowComments(postId, allow)
    }

    private fun DemoPostEntity.aPostEntity(comentarios: Int) = PostEntity(
        id = id,
        authorName = authorName,
        authorType = 1,
        courseCode = courseCode,
        courseName = courseName,
        title = title,
        content = content,
        allowComments = allowComments,
        countComments = comentarios,
        isPrivate = isPrivate,
        date = published,
        receiverPrivate = receiverId,
        priority = priority,
        archivedAt = archivedAt,
        receiverName = receiverName
    )
}

@Singleton
class DemoCommentRepository @Inject constructor(
    private val dao: DemoDao,
    private val dataStoreHelper: DataStoreHelper
) : CommentRepository {

    override suspend fun sendComment(commentRequest: CommentRequest): Result<Boolean> = runCatching {
        delay(LATENCIA_MS)
        val postId = commentRequest.post_id.toInt()
        val post = dao.getPost(postId) ?: throw NoSuchElementException("Anuncio no encontrado")
        // Misma regla que se propone validar en service-comment-add.
        check(post.allowComments) { "Los comentarios de este anuncio están deshabilitados" }
        dao.insertComment(
            DemoCommentEntity(
                postId = postId,
                authorName = "${dataStoreHelper.getFirstname()} ${dataStoreHelper.getSurname()}".trim(),
                email = dataStoreHelper.getEmail(),
                content = commentRequest.content,
                published = FechaBackend.ahora()
            )
        )
        true
    }

    override suspend fun getComments(commentDto: CommentDto): Result<List<CommentEntity>> = runCatching {
        delay(LATENCIA_MS)
        val email = dataStoreHelper.getEmail()
        dao.getComments(commentDto.post_id).map {
            CommentEntity(
                id = it.id,
                postId = it.postId,
                authorName = it.authorName,
                content = it.content,
                email = it.email,
                isMine = it.email == email,
                publishedDate = it.published
            )
        }
    }
}

class DemoContactRepository @Inject constructor() : ContactRepository {
    override suspend fun getStudentsByCourse(courseCatalogRequest: CourseCatalogRequest): Result<List<StudentEntity>> {
        delay(LATENCIA_MS)
        return Result.success(DemoData.estudiantes(courseCatalogRequest.courseCatalogId ?: ""))
    }
}

class DemoStudentRepository @Inject constructor(
    private val dataStoreHelper: DataStoreHelper
) : StudentRepository {
    override suspend fun getStudentEnrollment(): Result<List<StudentEnrollment>> {
        val email = dataStoreHelper.getEmail()
        val cuenta = listOf(DemoData.CUENTA_A, DemoData.CUENTA_B).firstOrNull { it.email == email }
        return Result.success(DemoData.matriculas(cuenta))
    }

    override suspend fun findCourse(classCode: ClassCodeRequest): Result<StudentEnrollment> =
        Result.failure(UnsupportedOperationException("No disponible en el modo demo"))

    override suspend fun courseEnroll(courseEnrollRequest: CourseEnrollRequest): Result<CourseEnrollResponse> =
        Result.failure(UnsupportedOperationException("No disponible en el modo demo"))

    override suspend fun courseStudent(courseCatalogRequest: CourseCatalogRequest): Result<List<StudentEntity>> =
        Result.success(DemoData.estudiantes(courseCatalogRequest.courseCatalogId ?: ""))
}

class DemoCourseRepository @Inject constructor() : CourseRepository {
    override suspend fun getCourseCatalogList(): Result<List<CourseCatalogEntity>> =
        Result.success(DemoData.cursos.map { CourseCatalogEntity(id = it.id, code = it.codigo, name = it.nombre) })
}

class DemoAnnounceRepository @Inject constructor(
    private val postRepository: DemoPostRepository
) : AnnounceRepository {
    override suspend fun addPostPrivate(postPrivateRequest: PostPrivateRequest): Result<PostPrivateResponse> =
        postRepository.publish(
            NuevoAnuncioRequest(
                courseCatalogId = postPrivateRequest.courseCatalogId,
                courseCode = "",
                title = postPrivateRequest.title,
                content = postPrivateRequest.content,
                isPrivate = true,
                receiverId = postPrivateRequest.receiverPrivate,
                receiverName = null,
                priority = com.erns.alertauni.data.model.Prioridad.MEDIA,
                allowComments = postPrivateRequest.allowComments
            )
        ).map { PostPrivateResponse(published = it.created_at) }

    override suspend fun addPostPublic(postRequest: PostRequest): Result<PostPrivateResponse> =
        Result.failure(UnsupportedOperationException("Usa PostRepository.publish"))
}
