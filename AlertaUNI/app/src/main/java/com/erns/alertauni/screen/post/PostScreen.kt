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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import com.erns.alertauni.ui.theme.esAlturaCompacta
import kotlin.math.roundToInt
import com.erns.alertauni.domain.muro.FiltroMuro
import com.erns.alertauni.screen.post.components.AccionesAnuncio
import com.erns.alertauni.screen.post.components.AnuncioCard
import com.erns.alertauni.screen.post.components.BotonBorradores
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
    onBorradores: () -> Unit,
    viewModel: PostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val cursos by viewModel.cursos.collectAsState()
    val usuario by viewModel.username.collectAsState()
    val resaltado by viewModel.resaltado.collectAsState()
    val borradores by viewModel.borradores.collectAsState()

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

    // Con poca altura (teléfono en horizontal), la cabecera y los chips se ocultan al bajar
    // por la lista y vuelven al subir; así se ven los anuncios (incidencia PT-01).
    // En vertical la cabecera queda fija, como en Figma y en las pruebas de usabilidad.
    val alturaCompacta = esAlturaCompacta()
    val cabecera = remember { CabeceraOcultable() }
    LaunchedEffect(alturaCompacta) { if (!alturaCompacta) cabecera.mostrar() }

    Column(
        modifier
            .fillMaxSize()
            .background(MySecondaryColor)
            .then(if (alturaCompacta) Modifier.nestedScroll(cabecera.conexion) else Modifier)
    ) {
        if (alturaCompacta) {
            // La franja de la barra de estado queda fija y azul aunque la cabecera se oculte.
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .background(MyPrimaryColor)
                    .windowInsetsTopHeight(WindowInsets.statusBars)
            )
        }
        Column(
            if (alturaCompacta) {
                Modifier
                    .consumeWindowInsets(WindowInsets.statusBars)
                    .ocultable(cabecera)
            } else Modifier
        ) {
            MuroHeader(
                usuario = usuario,
                texto = filtro.texto,
                onTexto = viewModel::onTexto,
                accion = { BotonBorradores(borradores, onBorradores) }
            )
            FiltrosChips(
                filtro = filtro,
                cursos = cursos,
                onCurso = viewModel::onCurso,
                onTipo = viewModel::onTipo,
                onPrioridad = viewModel::onPrioridad,
                onArchivados = viewModel::onArchivados
            )
        }
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

/**
 * Estado de la cabecera ocultable: [desplazamiento] va de 0 (visible) a -[alto] (oculta).
 * Al bajar por la lista la cabecera se oculta antes de que la lista se mueva, y al subir
 * reaparece primero (patrón «enterAlways» de Material).
 */
@Stable
private class CabeceraOcultable {
    var alto by mutableIntStateOf(0)
    var desplazamiento by mutableFloatStateOf(0f)

    fun mostrar() {
        desplazamiento = 0f
    }

    val conexion = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (alto == 0) return Offset.Zero
            val nuevo = (desplazamiento + available.y).coerceIn(-alto.toFloat(), 0f)
            val consumido = nuevo - desplazamiento
            desplazamiento = nuevo
            return Offset(0f, consumido)
        }
    }
}

/** Dibuja el contenido desplazado hacia arriba y reserva solo la parte que sigue visible. */
private fun Modifier.ocultable(estado: CabeceraOcultable): Modifier = this
    .clipToBounds()
    .layout { medible, restricciones ->
        val pieza = medible.measure(restricciones.copy(minHeight = 0, maxHeight = Constraints.Infinity))
        estado.alto = pieza.height
        val desplazado = estado.desplazamiento.roundToInt()
        layout(pieza.width, (pieza.height + desplazado).coerceAtLeast(0)) {
            pieza.place(0, desplazado)
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
    // También cuando aparece la línea «N resultados…»: llega tras el debounce y, como
    // se inserta encima de la primera tarjeta visible, la lista la dejaba fuera de la vista.
    LaunchedEffect(filtro, estado.resumen != null) { listState.scrollToItem(0) }

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
