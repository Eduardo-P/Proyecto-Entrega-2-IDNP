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
}
