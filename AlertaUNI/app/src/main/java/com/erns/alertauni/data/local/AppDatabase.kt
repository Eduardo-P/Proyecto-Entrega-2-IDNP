package com.erns.alertauni.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.erns.alertauni.data.model.NotificacionEntity

/**
 * Base local de la app.
 *
 * - v2: `notificaciones` (original).
 * - v3: `post_extra` (SIMULADO: prioridad, archivado, comentarios y destinatario).
 *
 * Cada cambio de versión tiene una migración real: nunca se usa
 * `fallbackToDestructiveMigration`, para no borrar las notificaciones guardadas.
 */
@Database(
    entities = [NotificacionEntity::class, PostExtraEntity::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificacionDao(): NotificacionDao
    abstract fun postExtraDao(): PostExtraDao

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

        val MIGRACIONES = arrayOf(MIGRATION_2_3)

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
