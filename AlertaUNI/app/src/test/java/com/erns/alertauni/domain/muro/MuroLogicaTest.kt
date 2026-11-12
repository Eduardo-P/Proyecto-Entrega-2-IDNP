package com.erns.alertauni.domain.muro

import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.data.model.Prioridad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class MuroLogicaTest {

    // ---------- Búsqueda ----------

    @Test
    fun busqueda_ignoraMayusculasYTildes() {
        assertTrue(TextoBusqueda.contiene("Práctica calificada 2", "practica"))
        assertTrue(TextoBusqueda.contiene("PRACTICA CALIFICADA", "Práctica"))
        assertTrue(TextoBusqueda.contiene("Examen de Ingeniería", "ingenieria"))
        assertTrue(TextoBusqueda.contiene("cualquier texto", "   "))
        assertTrue(!TextoBusqueda.contiene("Material de clase", "práctica"))
    }

    @Test
    fun resaltado_devuelveRangosDelTextoOriginal() {
        val texto = "La práctica y la PRÁCTICA"
        val rangos = TextoBusqueda.coincidencias(texto, "practica")
        assertEquals(2, rangos.size)
        assertEquals("práctica", texto.substring(rangos[0]))
        assertEquals("PRÁCTICA", texto.substring(rangos[1]))
    }

    @Test
    fun resaltado_conTildeDescompuesta_mapeaBienLosIndices() {
        // "á" escrita como 'a' + tilde combinable (2 caracteres en el original)
        val texto = "Práctica 2"
        val rangos = TextoBusqueda.coincidencias(texto, "práctica")
        assertEquals(1, rangos.size)
        assertEquals("Práctica", texto.substring(rangos[0]))
    }

    // ---------- Filtros ----------

    private fun post(
        id: Int, titulo: String, contenido: String = "", curso: String = "INF-01",
        privado: Boolean = false, prioridad: Int = Prioridad.MEDIA, archivado: Boolean = false
    ) = PostEntity(
        id = id, authorType = 1, courseCode = curso, title = titulo, content = contenido,
        allowComments = true, countComments = 0, isPrivate = privado, date = "2026-06-01 10:00:00",
        priority = prioridad, archivedAt = if (archivado) "2026-06-02 10:00:00" else null
    )

    private val lista = listOf(
        post(1, "Práctica calificada 2", "Traer calculadora", prioridad = Prioridad.ALTA),
        post(2, "Inasistencia a la práctica", privado = true),
        post(3, "Material de la semana 6", curso = "INF-02", prioridad = Prioridad.BAJA),
        post(4, "Horario de consultas", archivado = true)
    )

    @Test
    fun filtro_porTextoEnTituloOContenido() {
        assertEquals(listOf(1, 2), lista.filtrar(FiltroMuro(texto = "PRACTICA")).map { it.id })
        assertEquals(listOf(1), lista.filtrar(FiltroMuro(texto = "calculadora")).map { it.id })
    }

    @Test
    fun filtro_combinaCriterios() {
        assertEquals(listOf(2), lista.filtrar(FiltroMuro(texto = "práctica", privado = true)).map { it.id })
        assertEquals(listOf(3), lista.filtrar(FiltroMuro(curso = "INF-02")).map { it.id })
        assertEquals(listOf(1), lista.filtrar(FiltroMuro(prioridad = Prioridad.ALTA)).map { it.id })
        assertTrue(lista.filtrar(FiltroMuro(texto = "examen sustitutorio")).isEmpty())
    }

    @Test
    fun filtro_separaArchivadosDelMuroPrincipal() {
        assertEquals(listOf(1, 2, 3), lista.filtrar(FiltroMuro()).map { it.id })
        assertEquals(listOf(4), lista.filtrar(FiltroMuro(archivados = true)).map { it.id })
    }

    @Test
    fun filtro_limpioConservaSoloLaVista() {
        val f = FiltroMuro(texto = "x", curso = "INF-01", archivados = true)
        assertEquals(FiltroMuro(archivados = true), f.limpio())
        assertTrue(!FiltroMuro(archivados = true).tieneCriterios)
    }

    // ---------- Fechas ----------

    private val lima = ZoneId.of("America/Lima")
    private val ahora = ZonedDateTime.of(2026, 6, 3, 10, 0, 0, 0, lima) // miércoles

    @Test
    fun fecha_amigable() {
        assertEquals("Hoy, 09:15", FechaAmigable.formatear(ahora.withHour(9).withMinute(15), ahora))
        assertEquals("Ayer, 18:20", FechaAmigable.formatear(ahora.minusDays(1).withHour(18).withMinute(20), ahora))
        val lunes = FechaAmigable.formatear(ahora.minusDays(2).withHour(18).withMinute(40), ahora)
        assertTrue(lunes, lunes.startsWith("Lun 1 jun") && lunes.endsWith("18:40"))
        assertEquals("Ahora", FechaAmigable.formatear(ahora, ahora))
    }

    @Test
    fun fecha_grupos() {
        assertEquals(GrupoFecha.HOY, FechaAmigable.grupo(ahora.withHour(0), ahora))
        assertEquals(GrupoFecha.ESTA_SEMANA, FechaAmigable.grupo(ahora.minusDays(1), ahora))
        assertEquals(GrupoFecha.ESTA_SEMANA, FechaAmigable.grupo(ahora.minusDays(6), ahora))
        assertEquals(GrupoFecha.ANTERIORES, FechaAmigable.grupo(ahora.minusDays(7), ahora))
    }
}
