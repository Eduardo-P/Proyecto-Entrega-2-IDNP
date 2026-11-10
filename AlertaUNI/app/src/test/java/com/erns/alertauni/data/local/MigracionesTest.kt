package com.erns.alertauni.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Comprueba que cada migración de Room conserva los datos existentes
 * (en especial las notificaciones guardadas por la versión original de la app).
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class MigracionesTest {

    private val nombreDb = "migracion-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migracion2a3_conservaNotificacionesYCreaPostExtra() {
        helper.createDatabase(nombreDb, 2).apply {
            execSQL("INSERT INTO notificaciones (cursoId, mensaje, fecha) VALUES ('INF-01', 'Hola', '2026-05-31 10:00:00')")
            close()
        }

        val db = helper.runMigrationsAndValidate(nombreDb, 3, true, AppDatabase.MIGRATION_2_3)

        db.query("SELECT mensaje FROM notificaciones").use { c ->
            assertEquals(1, c.count)
            c.moveToFirst()
            assertEquals("Hola", c.getString(0))
        }
        db.execSQL("INSERT INTO post_extra (postId, priority) VALUES (7, 1)")
        db.query("SELECT priority FROM post_extra WHERE postId = 7").use { c ->
            c.moveToFirst()
            assertEquals(1, c.getInt(0))
        }
    }

    @Test
    fun migracion3a4_creaTablasDemoSinTocarDatosPrevios() {
        helper.createDatabase(nombreDb, 3).apply {
            execSQL("INSERT INTO notificaciones (cursoId, mensaje, fecha) VALUES ('INF-01', 'Hola', '2026-05-31 10:00:00')")
            execSQL("INSERT INTO post_extra (postId, priority) VALUES (7, 1)")
            close()
        }

        val db = helper.runMigrationsAndValidate(nombreDb, 4, true, AppDatabase.MIGRATION_3_4)

        db.query("SELECT COUNT(*) FROM notificaciones").use { c -> c.moveToFirst(); assertEquals(1, c.getInt(0)) }
        db.query("SELECT COUNT(*) FROM post_extra").use { c -> c.moveToFirst(); assertEquals(1, c.getInt(0)) }
        db.query("SELECT COUNT(*) FROM demo_post").use { c -> c.moveToFirst(); assertEquals(0, c.getInt(0)) }
    }

    @Test
    fun migracion4a5_creaTablaDeBorradores() {
        helper.createDatabase(nombreDb, 4).apply {
            execSQL("INSERT INTO notificaciones (cursoId, mensaje, fecha) VALUES ('INF-01', 'Hola', '2026-05-31 10:00:00')")
            close()
        }
        val db = helper.runMigrationsAndValidate(nombreDb, 5, true, AppDatabase.MIGRATION_4_5)
        db.query("SELECT COUNT(*) FROM notificaciones").use { c -> c.moveToFirst(); assertEquals(1, c.getInt(0)) }
        db.query("SELECT COUNT(*) FROM drafts").use { c -> c.moveToFirst(); assertEquals(0, c.getInt(0)) }
    }

    /** Un usuario con la app original (v2) actualiza directamente a la versión final. */
    @Test
    fun migracionCompleta2a5_conservaNotificaciones() {
        helper.createDatabase(nombreDb, 2).apply {
            execSQL("INSERT INTO notificaciones (cursoId, mensaje, fecha) VALUES ('INF-01', 'Hola', '2026-05-31 10:00:00')")
            close()
        }
        val db = helper.runMigrationsAndValidate(nombreDb, 5, true, *AppDatabase.MIGRACIONES)
        db.query("SELECT mensaje FROM notificaciones").use { c -> c.moveToFirst(); assertEquals("Hola", c.getString(0)) }
    }
}
