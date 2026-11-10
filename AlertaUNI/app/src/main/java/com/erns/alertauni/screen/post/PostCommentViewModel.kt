package com.erns.alertauni.screen.post

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.common.FechaBackend
import com.erns.alertauni.data.model.CommentDto
import com.erns.alertauni.data.model.CommentEntity
import com.erns.alertauni.data.model.CommentRequest
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.data.repository.CommentRepository
import com.erns.alertauni.data.repository.PostRepository
import com.erns.alertauni.domain.manager.DataStoreHelper
import com.erns.alertauni.domain.muro.FechaAmigable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

/** Burbuja de chat ya formateada. `separador` = «Hoy», «Ayer»… cuando cambia el día. */
data class ComentarioUi(val comentario: CommentEntity, val hora: String, val separador: String?)

data class ComentariosUiState(
    val cargando: Boolean = true,
    val post: PostEntity? = null,
    val fechaPost: String = "",
    val comentarios: List<ComentarioUi> = emptyList(),
    val esDocente: Boolean = false,
    val enviando: Boolean = false,
    val error: String? = null
) {
    /** Si el anuncio tiene los comentarios cerrados, la entrada queda bloqueada. */
    val escrituraBloqueada: Boolean get() = post?.allowComments == false
}

@HiltViewModel
class PostCommentViewModel @Inject constructor(
    private val repository: CommentRepository,
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle,
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {
    private val TAG = "CommentViewModel"

    val postId: Int = savedStateHandle.get<String>("postId")?.toIntOrNull()
        ?: throw IllegalArgumentException("Posts ID is required")

    private val _ui = MutableStateFlow(ComentariosUiState())
    val ui: StateFlow<ComentariosUiState> = _ui.asStateFlow()

    private val _mensajes = Channel<String>(Channel.BUFFERED)
    val mensajes: Flow<String> = _mensajes.receiveAsFlow()

    init {
        viewModelScope.launch {
            _ui.update { it.copy(esDocente = dataStoreHelper.getUserType() == "PROFESSOR") }
        }
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _ui.update { it.copy(cargando = true, error = null) }
            val ahora = ZonedDateTime.now(FechaBackend.ZONA_LOCAL)
            postRepository.getPost(postId).onSuccess { post ->
                _ui.update { it.copy(post = post, fechaPost = FechaAmigable.formatear(FechaBackend.parse(post.date), ahora)) }
            }.onFailure { Log.d(TAG, "No se pudo cargar el anuncio: ${it.message}") }

            repository.getComments(CommentDto(post_id = postId)).onSuccess { lista ->
                _ui.update { it.copy(cargando = false, comentarios = formatear(lista, ahora)) }
            }.onFailure {
                Log.d(TAG, "Error loading comments: ${it.message}")
                _ui.update { it.copy(cargando = false, error = "No se pudieron cargar los comentarios") }
            }
        }
    }

    fun enviar(texto: String) {
        val contenido = texto.trim()
        if (contenido.isEmpty() || _ui.value.escrituraBloqueada || _ui.value.enviando) return
        _ui.update { it.copy(enviando = true) }
        viewModelScope.launch {
            repository.sendComment(CommentRequest(post_id = postId.toString(), content = contenido, is_private = false))
                .onSuccess {
                    val propio = CommentEntity(
                        id = 0,
                        postId = postId,
                        authorName = "Tú",
                        content = contenido,
                        email = "",
                        isMine = true,
                        publishedDate = FechaBackend.ahora()
                    )
                    val ahora = ZonedDateTime.now(FechaBackend.ZONA_LOCAL)
                    _ui.update {
                        it.copy(
                            enviando = false,
                            comentarios = formatear(it.comentarios.map { c -> c.comentario } + propio, ahora),
                            post = it.post?.let { p -> p.copy(countComments = p.countComments + 1) }
                        )
                    }
                }
                .onFailure {
                    Log.d(TAG, it.message.toString())
                    _ui.update { s -> s.copy(enviando = false) }
                    _mensajes.send("No se pudo enviar el comentario")
                }
        }
    }

    /** Solo el docente: vuelve a abrir la escritura desde el propio aviso (mq20). */
    fun alternarComentarios() {
        val post = _ui.value.post ?: return
        val habilitar = !post.allowComments
        _ui.update { it.copy(post = post.copy(allowComments = habilitar)) }
        viewModelScope.launch {
            postRepository.setAllowComments(post.id, habilitar)
                .onSuccess { _mensajes.send(if (habilitar) "Comentarios habilitados" else "Comentarios deshabilitados") }
                .onFailure {
                    _ui.update { it.copy(post = post) }
                    _mensajes.send("No se pudo cambiar el estado de los comentarios")
                }
        }
    }

    private fun formatear(lista: List<CommentEntity>, ahora: ZonedDateTime): List<ComentarioUi> {
        var diaAnterior: String? = null
        return lista.map { c ->
            val fecha = FechaBackend.parse(c.publishedDate)
            val dia = FechaAmigable.dia(fecha, ahora)
            val separador = if (dia != diaAnterior) dia else null
            diaAnterior = dia
            ComentarioUi(c, fecha?.let { FechaAmigable.hora(it) } ?: c.publishedDate, separador)
        }
    }
}
