package com.erns.alertauni.screen.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.common.FechaBackend
import com.erns.alertauni.data.model.BackendException
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.data.repository.PostRepository
import com.erns.alertauni.domain.manager.DataStoreHelper
import com.erns.alertauni.domain.muro.FechaAmigable
import com.erns.alertauni.domain.muro.FiltroMuro
import com.erns.alertauni.domain.muro.GrupoFecha
import com.erns.alertauni.domain.muro.TextoBusqueda
import com.erns.alertauni.domain.muro.filtrar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

/** Anuncio listo para mostrar: textos calculados aquí, no en la interfaz. */
data class AnuncioUi(
    val post: PostEntity,
    val fecha: String,
    val fechaArchivado: String?,
    val coincidenciasTitulo: List<IntRange>,
    val coincidenciasContenido: List<IntRange>
)

data class GrupoAnuncios(val grupo: GrupoFecha?, val anuncios: List<AnuncioUi>)

sealed interface MuroUiState {
    data object Cargando : MuroUiState
    data class Error(val mensaje: String) : MuroUiState
    /** El docente todavía no tiene anuncios publicados. */
    data object MuroVacio : MuroUiState
    data class SinResultados(
        val titulo: String,
        val detalle: String,
        val puedeQuitarFiltros: Boolean,
        val puedeBuscarEnArchivados: Boolean
    ) : MuroUiState
    data class Contenido(
        val grupos: List<GrupoAnuncios>,
        /** «2 resultados para «práctica» en INF-01», o null si no hay búsqueda ni filtros. */
        val resumen: String?,
        /** Aviso de la vista de archivados. */
        val avisoArchivados: String?
    ) : MuroUiState
}

sealed interface MuroEvento {
    data class Archivado(val post: PostEntity) : MuroEvento
    data class Mensaje(val texto: String) : MuroEvento
}

@OptIn(FlowPreview::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {
    private val TAG = "PostViewModel"

    private sealed interface Carga {
        data object Cargando : Carga
        data class Error(val mensaje: String) : Carga
        data class Datos(val anuncios: List<PostEntity>) : Carga
    }

    private val _carga = MutableStateFlow<Carga>(Carga.Cargando)

    /** Filtro inmediato (lo que ve el campo de texto y los chips). */
    private val _filtro = MutableStateFlow(FiltroMuro())
    val filtro: StateFlow<FiltroMuro> = _filtro.asStateFlow()

    /** El texto se aplica con debounce para no filtrar en cada pulsación. */
    private val textoAplicado: Flow<String> =
        _filtro.map { it.texto }.distinctUntilChanged().debounce(250)

    private val _resaltado = MutableStateFlow<Int?>(null)
    val resaltado: StateFlow<Int?> = _resaltado.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    /** Cursos presentes en el muro, para el chip de filtro «Curso». */
    val cursos: StateFlow<List<String>> = _carga
        .map { c -> (c as? Carga.Datos)?.anuncios?.map { it.courseCode }?.distinct()?.sorted().orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<MuroUiState> =
        combine(_carga, _filtro, textoAplicado) { carga, filtro, texto ->
            construirEstado(carga, filtro.copy(texto = texto))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MuroUiState.Cargando)

    private val _eventos = Channel<MuroEvento>(Channel.BUFFERED)
    val eventos: Flow<MuroEvento> = _eventos.receiveAsFlow()

    init {
        viewModelScope.launch {
            _username.value = dataStoreHelper.getFirstname() + " " + dataStoreHelper.getSurname()
        }
        recargar()
    }

    // ---------------- Carga ----------------

    fun recargar() {
        viewModelScope.launch {
            // Si ya hay datos se mantienen en pantalla mientras se recarga (sin parpadeo).
            if (_carga.value !is Carga.Datos) _carga.value = Carga.Cargando
            repository.getPosts()
                .onSuccess { _carga.value = Carga.Datos(it) }
                .onFailure { e ->
                    val detalle = if (e is BackendException) "${e.errorData.code} -> ${e.errorData.message}" else e.message
                    Log.d(TAG, "Error al cargar anuncios: $detalle")
                    if (_carga.value !is Carga.Datos) {
                        _carga.value = Carga.Error("No se pudieron cargar los anuncios. Revisa tu conexión e inténtalo de nuevo.")
                    } else {
                        _eventos.send(MuroEvento.Mensaje("No se pudo actualizar el muro"))
                    }
                }
        }
    }

    /** Recarga solo si ya se habían mostrado datos (al volver a la pantalla). */
    fun recargarSiHayDatos() {
        if (_carga.value is Carga.Datos) recargar()
    }

    /** Se llama al volver del formulario: recarga, resalta el anuncio nuevo y confirma. */
    fun onPublicado(postId: Int, mensaje: String) {
        _filtro.value = FiltroMuro()
        _resaltado.value = postId
        recargar()
        viewModelScope.launch { _eventos.send(MuroEvento.Mensaje(mensaje)) }
    }

    fun quitarResaltado() {
        _resaltado.value = null
    }

    // ---------------- Búsqueda y filtros ----------------

    fun onTexto(texto: String) = _filtro.update { it.copy(texto = texto) }
    fun onCurso(curso: String?) = _filtro.update { it.copy(curso = curso) }
    fun onTipo(privado: Boolean?) = _filtro.update { it.copy(privado = privado) }
    fun onPrioridad(prioridad: Int?) = _filtro.update { it.copy(prioridad = prioridad) }
    fun onArchivados(ver: Boolean) = _filtro.update { it.copy(archivados = ver) }
    fun quitarFiltros() = _filtro.update { it.limpio() }
    fun buscarEnArchivados() = _filtro.update { FiltroMuro(texto = it.texto, archivados = true) }

    // ---------------- Gestión ----------------

    /** Archiva sin diálogo: actualización optimista; si falla, se revierte y se avisa. */
    fun archivar(post: PostEntity) = cambiarArchivado(post, archivar = true)

    fun restaurar(post: PostEntity) = cambiarArchivado(post, archivar = false)

    private fun cambiarArchivado(post: PostEntity, archivar: Boolean) {
        val nuevo = post.copy(archivedAt = if (archivar) FechaBackend.ahora() else null)
        reemplazarLocal(nuevo)
        viewModelScope.launch {
            repository.setArchived(post.id, archivar)
                .onSuccess {
                    if (archivar) _eventos.send(MuroEvento.Archivado(post))
                    else _eventos.send(MuroEvento.Mensaje("«${post.title}» volvió al muro"))
                }
                .onFailure {
                    reemplazarLocal(post)
                    _eventos.send(MuroEvento.Mensaje("No se pudo ${if (archivar) "archivar" else "restaurar"} el anuncio"))
                }
        }
    }

    fun alternarComentarios(post: PostEntity) {
        val habilitar = !post.allowComments
        reemplazarLocal(post.copy(allowComments = habilitar))
        viewModelScope.launch {
            repository.setAllowComments(post.id, habilitar)
                .onSuccess {
                    _eventos.send(MuroEvento.Mensaje(if (habilitar) "Comentarios habilitados" else "Comentarios deshabilitados"))
                }
                .onFailure {
                    reemplazarLocal(post)
                    _eventos.send(MuroEvento.Mensaje("No se pudo cambiar el estado de los comentarios"))
                }
        }
    }

    private fun reemplazarLocal(post: PostEntity) {
        _carga.update { c ->
            if (c is Carga.Datos) Carga.Datos(c.anuncios.map { if (it.id == post.id) post else it }) else c
        }
    }

    // ---------------- Construcción del estado ----------------

    private fun construirEstado(carga: Carga, filtro: FiltroMuro): MuroUiState {
        val datos = when (carga) {
            Carga.Cargando -> return MuroUiState.Cargando
            is Carga.Error -> return MuroUiState.Error(carga.mensaje)
            is Carga.Datos -> carga.anuncios
        }
        val ahora = ZonedDateTime.now(FechaBackend.ZONA_LOCAL)
        val activos = datos.count { !it.isArchived }
        val archivados = datos.count { it.isArchived }

        if (!filtro.archivados && activos == 0 && !filtro.tieneCriterios) return MuroUiState.MuroVacio

        val visibles = datos.filtrar(filtro)
            .map { it to FechaBackend.parse(it.date) }
            .sortedByDescending { it.second }

        if (visibles.isEmpty()) return sinResultados(filtro, archivados)

        val anuncios = visibles.map { (p, fecha) ->
            AnuncioUi(
                post = p,
                fecha = FechaAmigable.formatear(fecha, ahora),
                fechaArchivado = p.archivedAt?.let { "Archivado el " + FechaAmigable.corta(FechaBackend.parse(it), ahora) },
                coincidenciasTitulo = TextoBusqueda.coincidencias(p.title, filtro.texto),
                coincidenciasContenido = TextoBusqueda.coincidencias(p.content, filtro.texto)
            ) to fecha
        }
        val grupos = if (filtro.archivados) {
            listOf(GrupoAnuncios(null, anuncios.map { it.first }))
        } else {
            anuncios.groupBy { FechaAmigable.grupo(it.second, ahora) }
                .toSortedMap()
                .map { (g, lista) -> GrupoAnuncios(g, lista.map { it.first }) }
        }
        return MuroUiState.Contenido(
            grupos = grupos,
            resumen = if (filtro.tieneCriterios) resumen(visibles.size, filtro) else null,
            avisoArchivados = if (filtro.archivados) {
                "$archivados ${if (archivados == 1) "anuncio archivado" else "anuncios archivados"} · no aparecen en el muro principal"
            } else null
        )
    }

    private fun sinResultados(f: FiltroMuro, archivados: Int): MuroUiState {
        val texto = f.texto.trim()
        return when {
            f.archivados && !f.tieneCriterios -> MuroUiState.SinResultados(
                titulo = "No hay anuncios archivados",
                detalle = "Los anuncios que archives desde el menú ⋮ aparecerán aquí.",
                puedeQuitarFiltros = false, puedeBuscarEnArchivados = false
            )
            else -> MuroUiState.SinResultados(
                titulo = if (texto.isNotEmpty()) "Sin resultados para «$texto»" else "Ningún anuncio coincide con los filtros",
                detalle = buildString {
                    append("No hay anuncios")
                    descripcionFiltros(f)?.let { append(" $it") }
                    if (texto.isNotEmpty()) append(" que contengan ese texto")
                    append(". Prueba otra palabra o quita filtros.")
                },
                puedeQuitarFiltros = true,
                puedeBuscarEnArchivados = !f.archivados && archivados > 0
            )
        }
    }

    private fun resumen(n: Int, f: FiltroMuro): String = buildString {
        append(if (n == 1) "1 resultado" else "$n resultados")
        if (f.texto.isNotBlank()) append(" para «${f.texto.trim()}»")
        descripcionFiltros(f)?.let { append(" · $it") }
    }

    private fun descripcionFiltros(f: FiltroMuro): String? = listOfNotNull(
        f.curso?.let { "en $it" },
        f.privado?.let { if (it) "privados" else "públicos" },
        f.prioridad?.let { "de prioridad ${com.erns.alertauni.data.model.Prioridad.etiqueta(it)}" }
    ).takeIf { it.isNotEmpty() }?.joinToString(" ")
}
