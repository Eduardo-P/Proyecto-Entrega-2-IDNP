package com.erns.alertauni.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.erns.alertauni.data.local.AppDatabase
import com.erns.alertauni.data.model.Prioridad
import com.erns.alertauni.domain.manager.DataStoreHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Tarea 3: guardar, recuperar, editar, eliminar y AISLAR borradores por cuenta.
 * Usa Room en memoria y el DataStore real de la app (cuenta = correo de la sesión).
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class DraftRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var sesion: DataStoreHelper
    private lateinit var repo: DraftRepositoryImpl

    private val cuentaA = "docente.a@demo.alertauni.pe"
    private val cuentaB = "docente.b@demo.alertauni.pe"

    private fun borrador(titulo: String, privado: Boolean = false) = BorradorNuevo(
        privado = privado, cursoId = "demo-cc-inf01", cursoCodigo = "INF-01",
        estudianteId = if (privado) "e1" else null, estudianteNombre = if (privado) "Juan Pérez" else null,
        prioridad = Prioridad.ALTA, titulo = titulo, contenido = "Contenido de $titulo",
        permitirComentarios = true
    )

    @Before
    fun abrir() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        sesion = DataStoreHelper(context)
        repo = DraftRepositoryImpl(db.draftDao(), sesion)
    }

    @After
    fun cerrar() = db.close()

    @Test
    fun guardarYRecuperar_conservaTodosLosCampos() = runTest {
        sesion.saveEmail(cuentaA)
        val id = repo.guardar(null, borrador("Práctica 3", privado = true))
        val leido = repo.obtener(id)!!
        assertEquals("Práctica 3", leido.titulo)
        assertTrue(leido.privado)
        assertEquals("Juan Pérez", leido.estudianteNombre)
        assertEquals(Prioridad.ALTA, leido.prioridad)
        assertEquals(cuentaA, leido.cuenta)
    }

    @Test
    fun guardarDeNuevo_actualizaElMismoBorrador() = runTest {
        sesion.saveEmail(cuentaA)
        val id = repo.guardar(null, borrador("Versión 1"))
        val creado = repo.obtener(id)!!.creadoEn
        val id2 = repo.guardar(id, borrador("Versión 2"))
        assertEquals(id, id2)
        assertEquals(1, repo.observarBorradores().first().size)
        val leido = repo.obtener(id)!!
        assertEquals("Versión 2", leido.titulo)
        assertEquals(creado, leido.creadoEn)
        assertTrue(leido.editadoEn >= creado)
    }

    @Test
    fun borradoresAisladosPorCuenta() = runTest {
        sesion.saveEmail(cuentaA)
        val idA = repo.guardar(null, borrador("Borrador de A"))

        // Otra cuenta inicia sesión en el mismo dispositivo.
        sesion.saveEmail(cuentaB)
        assertTrue("B no ve los borradores de A", repo.observarBorradores().first().isEmpty())
        assertEquals(0, repo.contarBorradores().first())
        assertNull("B no puede abrir un borrador de A por id", repo.obtener(idA))
        repo.eliminar(idA) // B no puede borrarlo
        repo.guardar(null, borrador("Borrador de B"))
        assertEquals(listOf("Borrador de B"), repo.observarBorradores().first().map { it.titulo })

        // A vuelve: sus borradores siguen ahí (se conservan al cambiar de cuenta).
        sesion.saveEmail(cuentaA)
        assertEquals(listOf("Borrador de A"), repo.observarBorradores().first().map { it.titulo })
    }

    @Test
    fun eliminarYDeshacer() = runTest {
        sesion.saveEmail(cuentaA)
        val id = repo.guardar(null, borrador("Para borrar"))
        val copia = repo.obtener(id)!!
        repo.eliminar(id)
        assertNull(repo.obtener(id))
        repo.restaurar(copia)
        assertEquals("Para borrar", repo.obtener(id)?.titulo)
    }

    @Test
    fun persisteEnLaBaseAlReabrir() = runTest {
        // Con una base en archivo: se cierra y se vuelve a abrir (como al reiniciar la app).
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("borradores-test")
        val db1 = Room.databaseBuilder(context, AppDatabase::class.java, "borradores-test")
            .allowMainThreadQueries().build()
        sesion.saveEmail(cuentaA)
        DraftRepositoryImpl(db1.draftDao(), sesion).guardar(null, borrador("Persistente"))
        db1.close()

        val db2 = Room.databaseBuilder(context, AppDatabase::class.java, "borradores-test")
            .allowMainThreadQueries().build()
        val titulos = DraftRepositoryImpl(db2.draftDao(), sesion).observarBorradores().first().map { it.titulo }
        db2.close()
        assertEquals(listOf("Persistente"), titulos)
    }
}
