package com.erns.alertauni.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.erns.alertauni.data.demo.DemoCommentEntity
import com.erns.alertauni.data.demo.DemoDao
import com.erns.alertauni.data.demo.DemoPostEntity
import com.erns.alertauni.data.model.NotificacionEntity

/**
 * Base local de la app.
 *
 * - v2: `notificaciones` (original).
 * - v3: `post_extra` (SIMULADO: prioridad, archivado, comentarios y destinatario).
 * - v4: `demo_post` y `demo_comment` (SIMULADO: backend local del modo demo).
 * - v5: `drafts` (borradores locales por cuenta, Tarea 3).
 *
 * Cada cambio de versión tiene una migración real: nunca se usa
 * `fallbackToDestructiveMigration`, para no borrar las notificaciones guardadas.
 */
@Database(
    entities = [
        NotificacionEntity::class, PostExtraEntity::class,
        DemoPostEntity::class, DemoCommentEntity::class,
        DraftEntity::class
    ],
    version = 5
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificacionDao(): NotificacionDao
    abstract fun postExtraDao(): PostExtraDao
    abstract fun demoDao(): DemoDao
    abstract fun draftDao(): DraftDao

    companion object {
        const val NOMBRE = "notificaciones-db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `post_extra` (`postId` INTEGER NOT NULL, " +
                        "`priority` INTEGER, `archivedAt` TEXT, `allowComments` INTEGER, " +
                        "`receiverName` TEXT, PRIMARY KEY(`postId`))"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `demo_post` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`courseCatalogId` TEXT NOT NULL, `courseCode` TEXT NOT NULL, `courseName` TEXT NOT NULL, " +
                        "`title` TEXT NOT NULL, `content` TEXT NOT NULL, `isPrivate` INTEGER NOT NULL, " +
                        "`receiverId` TEXT, `receiverName` TEXT, `priority` INTEGER NOT NULL, " +
                        "`allowComments` INTEGER NOT NULL, `archivedAt` TEXT, `published` TEXT NOT NULL, " +
                        "`authorName` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `demo_comment` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`postId` INTEGER NOT NULL, `authorName` TEXT NOT NULL, `email` TEXT NOT NULL, " +
                        "`content` TEXT NOT NULL, `published` TEXT NOT NULL)"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `drafts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`cuenta` TEXT NOT NULL, `privado` INTEGER NOT NULL, `cursoId` TEXT, `cursoCodigo` TEXT, " +
                        "`estudianteId` TEXT, `estudianteNombre` TEXT, `prioridad` INTEGER NOT NULL, " +
                        "`titulo` TEXT NOT NULL, `contenido` TEXT NOT NULL, `permitirComentarios` INTEGER NOT NULL, " +
                        "`creadoEn` INTEGER NOT NULL, `editadoEn` INTEGER NOT NULL)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_drafts_cuenta` ON `drafts` (`cuenta`)")
            }
        }

        val MIGRACIONES = arrayOf(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)

        @Volatile
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NOMBRE
                ).addMigrations(*MIGRACIONES).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
