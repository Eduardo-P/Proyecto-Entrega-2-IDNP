package com.erns.alertauni.data.demo

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

/*
 * SIMULADO: "backend" local del modo demo (solo debug, BuildConfig.DEMO_MODE).
 * Reemplaza a las Edge Functions de Supabase para poder usar y probar la app sin credenciales.
 * Los datos viven en Room, así que lo publicado en demo se conserva al reabrir la app.
 */

@Entity(tableName = "demo_post")
data class DemoPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseCatalogId: String,
    val courseCode: String,
    val courseName: String,
    val title: String,
    val content: String,
    val isPrivate: Boolean,
    val receiverId: String?,
    val receiverName: String?,
    val priority: Int,
    val allowComments: Boolean,
    val archivedAt: String?,
    val published: String,
    val authorName: String
)

@Entity(tableName = "demo_comment")
data class DemoCommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val authorName: String,
    val email: String,
    val content: String,
    val published: String
)

data class ConteoComentarios(val postId: Int, val total: Int)

@Dao
interface DemoDao {
    @Query("SELECT COUNT(*) FROM demo_post")
    suspend fun contarPosts(): Int

    @Query("SELECT * FROM demo_post ORDER BY published DESC")
    suspend fun getPosts(): List<DemoPostEntity>

    @Query("SELECT * FROM demo_post WHERE id = :id")
    suspend fun getPost(id: Int): DemoPostEntity?

    @Insert
    suspend fun insertPost(post: DemoPostEntity): Long

    @Insert
    suspend fun insertPosts(posts: List<DemoPostEntity>): List<Long>

    @Query("UPDATE demo_post SET archivedAt = :archivedAt WHERE id = :id")
    suspend fun setArchivedAt(id: Int, archivedAt: String?)

    @Query("UPDATE demo_post SET allowComments = :allow WHERE id = :id")
    suspend fun setAllowComments(id: Int, allow: Boolean)

    @Query("SELECT postId, COUNT(*) AS total FROM demo_comment GROUP BY postId")
    suspend fun conteoComentarios(): List<ConteoComentarios>

    @Query("SELECT * FROM demo_comment WHERE postId = :postId ORDER BY published ASC")
    suspend fun getComments(postId: Int): List<DemoCommentEntity>

    @Insert
    suspend fun insertComment(comment: DemoCommentEntity)

    @Insert
    suspend fun insertComments(comments: List<DemoCommentEntity>)
}
