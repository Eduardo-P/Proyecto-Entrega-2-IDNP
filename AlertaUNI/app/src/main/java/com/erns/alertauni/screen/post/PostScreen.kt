package com.erns.alertauni.screen.post

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erns.alertauni.domain.muro.FiltroMuro
import com.erns.alertauni.screen.post.components.AccionesAnuncio
import com.erns.alertauni.screen.post.components.AnuncioCard
import com.erns.alertauni.screen.post.components.CargandoMuro
import com.erns.alertauni.screen.post.components.EstadoMuro
import com.erns.alertauni.screen.post.components.FiltrosChips
import com.erns.alertauni.screen.post.components.IconosEstado
import com.erns.alertauni.screen.post.components.MuroHeader
import com.erns.alertauni.ui.theme.AuText
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MySecondaryColor

/**
 * Muro de Anuncios rediseñado (Figma F01, maquetas mq05, mq11, mq12, mq21, mq22).
 * Toda la lógica (filtros, agrupación, fechas, archivado) está en PostViewModel.
 */
@Composable
fun PostScreen(
    modifier: Modifier = Modifier,
    onClickComment: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    onFabActionReady: (() -> Unit) -> Unit,
    onNuevoAnuncio: () -> Unit,
    viewModel: PostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val cursos by viewModel.cursos.collectAsState()
    val usuario by viewModel.username.collectAsState()
    val resaltado by viewModel.resaltado.collectAsState()

    // El FAB «Nuevo anuncio» abre el formulario único (ruta propia).
    onFabActionReady(onNuevoAnuncio)

    // Snackbars: confirmaciones, errores y «Deshacer» al archivar.
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is MuroEvento.Mensaje -> snackbarHostState.showSnackbar(evento.texto)
                is MuroEvento.Archivado -> {
                    val r = snackbarHostState.showSnackbar(
                        message = "«${evento.post.title}» archivado",
                        actionLabel = "Deshacer",
                        duration = SnackbarDuration.Long
                    )
                    if (r == SnackbarResult.ActionPerformed) viewModel.restaurar(evento.post)
                }
            }
        }
    }

    val acciones = AccionesAnuncio(
        onAbrir = { onClickComment(it.id.toString()) },
        onAlternarComentarios = viewModel::alternarComentarios,
        onArchivar = viewModel::archivar,
        onRestaurar = viewModel::restaurar
    )

    Column(
        modifier
            .fillMaxSize()
            .background(MySecondaryColor)
    ) {
        MuroHeader(usuario = usuario, texto = filtro.texto, onTexto = viewModel::onTexto)
        FiltrosChips(
            filtro = filtro,
            cursos = cursos,
            onCurso = viewModel::onCurso,
            onTipo = viewModel::onTipo,
            onPrioridad = viewModel::onPrioridad,
            onArchivados = viewModel::onArchivados
        )
        when (val estado = uiState) {
            MuroUiState.Cargando -> CargandoMuro()
            is MuroUiState.Error -> EstadoMuro(
                icono = IconosEstado.error,
                titulo = "No se pudo cargar el muro",
                detalle = estado.mensaje,
                accionPrincipal = "Reintentar" to viewModel::recargar
            )
            MuroUiState.MuroVacio -> EstadoMuro(
                icono = IconosEstado.vacio,
                titulo = "Aún no hay anuncios",
                detalle = "Publica el primero con «Nuevo anuncio» para avisar a todo un curso o a un solo estudiante."
            )
            is MuroUiState.SinResultados -> EstadoMuro(
                icono = IconosEstado.sinResultados,
                titulo = estado.titulo,
                detalle = estado.detalle,
                accionPrincipal = if (estado.puedeQuitarFiltros) "Quitar filtros" to {
                    viewModel.onTexto(""); viewModel.quitarFiltros()
                } else null,
                accionSecundaria = if (estado.puedeBuscarEnArchivados) {
                    "Buscar también en archivados" to viewModel::buscarEnArchivados
                } else null
            )
            is MuroUiState.Contenido -> ListaAnuncios(estado, acciones, resaltado, filtro)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListaAnuncios(
    estado: MuroUiState.Contenido,
    acciones: AccionesAnuncio,
    resaltado: Int?,
    filtro: FiltroMuro
) {
    val listState = rememberLazyListState()
    // Al cambiar la búsqueda o los filtros se vuelve al inicio de los resultados.
    LaunchedEffect(filtro) { listState.scrollToItem(0) }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        // 88 dp abajo: el FAB extendido nunca tapa la última tarjeta (problema 6).
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        estado.resumen?.let { texto ->
            item(key = "resumen") {
                Text(texto, style = AuText.LabelLarge, color = MyPrimaryColor)
            }
        }
        estado.avisoArchivados?.let { texto ->
            item(key = "aviso") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Archive, null, tint = MyMutedForegroundColor, modifier = Modifier.size(18.dp))
                    Text(texto, style = AuText.Caption, color = MyMutedForegroundColor, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        estado.grupos.forEach { grupo ->
            grupo.grupo?.let { g ->
                stickyHeader(key = "grupo-${g.name}") {
                    Text(
                        g.titulo,
                        style = AuText.Label,
                        color = MyMutedForegroundColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MySecondaryColor)
                            .padding(top = 6.dp, bottom = 2.dp)
                    )
                }
            }
            items(grupo.anuncios, key = { it.post.id }) { anuncio ->
                AnuncioCard(
                    anuncio = anuncio,
                    acciones = acciones,
                    resaltada = anuncio.post.id == resaltado
                )
            }
        }
    }
}
