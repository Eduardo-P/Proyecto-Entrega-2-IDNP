package com.erns.alertauni.data.demo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.erns.alertauni.common.FechaBackend
import com.erns.alertauni.data.local.AppDatabase
import com.erns.alertauni.data.model.Prioridad
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.ZonedDateTime

/** Verifica que los datos de prueba cumplen lo pedido en la Tarea 4 del enunciado. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class DemoSeederTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: DemoDao

    @Before
    fun abrir() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries().build()
        dao = db.demoDao()
    }

    @After
    fun cerrar() = db.close()

    @Test
    fun siembra40AnunciosVariados() = runTest {
        DemoSeeder.sembrarSiVacio(dao, "Docente")
        val posts = dao.getPosts()

        assertTrue("al menos 40 anuncios", posts.size >= 40)
        assertTrue("hay públicos", posts.any { !it.isPrivate })
        assertTrue("hay privados con destinatario", posts.any { it.isPrivate && it.receiverName != null })
        assertTrue("todo privado tiene destinatario", posts.filter { it.isPrivate }.all { it.receiverId != null })
        assertEquals("las tres prioridades", Prioridad.todas.toSet(), posts.map { it.priority }.toSet())
        assertTrue("incluye la búsqueda del informe", posts.any { it.title == "Práctica calificada 2" })
        assertTrue("hay archivados", posts.any { it.archivedAt != null })
        assertTrue("hay comentarios deshabilitados", posts.any { !it.allowComments })
        assertTrue("hay contenido largo (> 250)", posts.any { it.content.length > 250 })

        val hoy = ZonedDateTime.now(FechaBackend.ZONA_LOCAL).toLocalDate()
        val dias = posts.mapNotNull { FechaBackend.parse(it.published)?.toLocalDate() }
        assertEquals(posts.size, dias.size)
        assertTrue("hay de hoy", dias.any { it == hoy })
        assertTrue("hay de esta semana", dias.any { it.isBefore(hoy) && it.isAfter(hoy.minusDays(7)) })
        assertTrue("hay anteriores", dias.any { it.isBefore(hoy.minusDays(7)) })
    }

    @Test
    fun noDuplicaSiYaHayDatos() = runTest {
        DemoSeeder.sembrarSiVacio(dao, "Docente")
        val n = dao.contarPosts()
        DemoSeeder.sembrarSiVacio(dao, "Docente")
        assertEquals(n, dao.contarPosts())
    }
}
