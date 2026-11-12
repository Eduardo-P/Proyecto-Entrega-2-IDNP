package com.erns.alertauni.data.repository

import com.erns.alertauni.data.local.DraftDao
import com.erns.alertauni.data.local.DraftEntity
import com.erns.alertauni.domain.manager.DataStoreHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/** Borrador listo para guardar (sin cuenta ni fechas: las pone el repositorio). */
data class BorradorNuevo(
    val privado: Boolean,
    val cursoId: String?,
    val cursoCodigo: String?,
    val estudianteId: String?,
    val estudianteNombre: String?,
    val prioridad: Int,
    val titulo: String,
    val contenido: String,
    val permitirComentarios: Boolean
)

interface DraftRepository {
    /** Borradores de la cuenta en sesión; cambia solo si cambia la cuenta. */
    fun observarBorradores(): Flow<List<DraftEntity>>
    fun contarBorradores(): Flow<Int>
    suspend fun obtener(id: Long): DraftEntity?
    /** Crea (id = null) o actualiza un borrador de la cuenta actual. Devuelve su id. */
    suspend fun guardar(id: Long?, borrador: BorradorNuevo): Long
    suspend fun eliminar(id: Long)
    /** Vuelve a insertar un borrador eliminado (acción «Deshacer»). */
    suspend fun restaurar(draft: DraftEntity)
}

/**
 * Almacenamiento LOCAL (Room) de borradores, aislado por cuenta.
 *
 * Identificador de cuenta: el correo que la app guarda en DataStore al iniciar sesión
 * (LoginViewModel -> saveEmail). Es estable para una cuenta de Google, está disponible sin
 * red y también en el modo demo; el id de usuario de Supabase no se guarda en el dispositivo.
 */
@Singleton
class DraftRepositoryImpl @Inject constructor(
    private val dao: DraftDao,
    private val dataStoreHelper: DataStoreHelper
) : DraftRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observarBorradores(): Flow<List<DraftEntity>> =
        dataStoreHelper.emailFlow.flatMapLatest { cuenta ->
            if (cuenta.isBlank()) flowOf(emptyList()) else dao.observar(cuenta)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun contarBorradores(): Flow<Int> =
        dataStoreHelper.emailFlow.flatMapLatest { cuenta ->
            if (cuenta.isBlank()) flowOf(0) else dao.contar(cuenta)
        }

    override suspend fun obtener(id: Long): DraftEntity? = dao.obtener(id, cuentaActual())

    override suspend fun guardar(id: Long?, borrador: BorradorNuevo): Long {
        val cuenta = cuentaActual()
        val ahora = System.currentTimeMillis()
        val existente = id?.let { dao.obtener(it, cuenta) }
        val entidad = DraftEntity(
            id = existente?.id ?: 0,
            cuenta = cuenta,
            privado = borrador.privado,
            cursoId = borrador.cursoId,
            cursoCodigo = borrador.cursoCodigo,
            estudianteId = borrador.estudianteId,
            estudianteNombre = borrador.estudianteNombre,
            prioridad = borrador.prioridad,
            titulo = borrador.titulo,
            contenido = borrador.contenido,
            permitirComentarios = borrador.permitirComentarios,
            creadoEn = existente?.creadoEn ?: ahora,
            editadoEn = ahora
        )
        return if (existente != null) {
            dao.actualizar(entidad)
            existente.id
        } else {
            dao.insertar(entidad)
        }
    }

    override suspend fun eliminar(id: Long) {
        dao.eliminar(id, cuentaActual())
    }

    override suspend fun restaurar(draft: DraftEntity) {
        if (draft.cuenta == cuentaActual()) dao.insertar(draft)
    }

    private suspend fun cuentaActual(): String {
        val cuenta = dataStoreHelper.getEmail()
        check(cuenta.isNotBlank()) { "No hay una sesión iniciada" }
        return cuenta
    }
}
