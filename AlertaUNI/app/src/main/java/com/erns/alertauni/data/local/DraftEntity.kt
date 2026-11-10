package com.erns.alertauni.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Borrador de anuncio guardado en el dispositivo (Tarea 3). Persistente en Room: sobrevive
 * a cerrar y reabrir la app. Cada fila pertenece a una cuenta (`cuenta` = correo de la sesión)
 * y TODAS las consultas filtran por ella, así otra cuenta nunca ve estos borradores.
 * Al cerrar sesión los borradores se conservan, asociados a su cuenta.
 */
@Entity(tableName = "drafts", indices = [Index("cuenta")])
data class DraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cuenta: String,
    val privado: Boolean,
    val cursoId: String?,
    val cursoCodigo: String?,
    val estudianteId: String?,
    val estudianteNombre: String?,
    val prioridad: Int,
    val titulo: String,
    val contenido: String,
    val permitirComentarios: Boolean,
    val creadoEn: Long,
    val editadoEn: Long
)

@Dao
interface DraftDao {
    @Query("SELECT * FROM drafts WHERE cuenta = :cuenta ORDER BY editadoEn DESC")
    fun observar(cuenta: String): Flow<List<DraftEntity>>

    @Query("SELECT COUNT(*) FROM drafts WHERE cuenta = :cuenta")
    fun contar(cuenta: String): Flow<Int>

    @Query("SELECT * FROM drafts WHERE id = :id AND cuenta = :cuenta")
    suspend fun obtener(id: Long, cuenta: String): DraftEntity?

    @Insert
    suspend fun insertar(draft: DraftEntity): Long

    @Update
    suspend fun actualizar(draft: DraftEntity): Int

    @Query("DELETE FROM drafts WHERE id = :id AND cuenta = :cuenta")
    suspend fun eliminar(id: Long, cuenta: String): Int
}
