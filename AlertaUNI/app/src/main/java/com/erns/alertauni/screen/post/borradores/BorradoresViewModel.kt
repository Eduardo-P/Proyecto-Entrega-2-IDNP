package com.erns.alertauni.screen.post.borradores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.common.FechaBackend
import com.erns.alertauni.data.local.DraftEntity
import com.erns.alertauni.data.repository.DraftRepository
import com.erns.alertauni.domain.muro.FechaAmigable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject

data class BorradorUi(val draft: DraftEntity, val editado: String)

sealed interface BorradoresUiState {
    data object Cargando : BorradoresUiState
    data class Lista(val borradores: List<BorradorUi>) : BorradoresUiState
}

/** Lista de borradores de la cuenta en sesión (Room + Flow: se actualiza sola). */
@HiltViewModel
class BorradoresViewModel @Inject constructor(
    private val repository: DraftRepository
) : ViewModel() {

    val ui: StateFlow<BorradoresUiState> = repository.observarBorradores()
        .map { lista ->
            val ahora = ZonedDateTime.now(FechaBackend.ZONA_LOCAL)
            BorradoresUiState.Lista(lista.map { d ->
                val editado = Instant.ofEpochMilli(d.editadoEn).atZone(FechaBackend.ZONA_LOCAL)
                BorradorUi(d, "Editado: " + FechaAmigable.formatear(editado, ahora).replaceFirstChar { it.lowercase() })
            })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BorradoresUiState.Cargando)

    private val _eliminados = Channel<DraftEntity>(Channel.BUFFERED)
    val eliminados: Flow<DraftEntity> = _eliminados.receiveAsFlow()

    /** Elimina sin diálogo y ofrece «Deshacer» (misma regla que archivar). */
    fun eliminar(draft: DraftEntity) {
        viewModelScope.launch {
            repository.eliminar(draft.id)
            _eliminados.send(draft)
        }
    }

    fun restaurar(draft: DraftEntity) {
        viewModelScope.launch { repository.restaurar(draft) }
    }
}
