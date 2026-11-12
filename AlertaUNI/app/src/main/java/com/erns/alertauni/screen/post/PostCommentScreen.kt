package com.erns.alertauni.screen.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.CommentsDisabled
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erns.alertauni.screen.post.components.CursoChip
import com.erns.alertauni.screen.post.components.PrioridadChip
import com.erns.alertauni.screen.post.components.TipoChip
import com.erns.alertauni.screen.post.components.comentariosTexto
import com.erns.alertauni.ui.theme.AuText
import com.erns.alertauni.ui.theme.MyForegroundColor
import com.erns.alertauni.ui.theme.MyMutedColor
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MyPrimaryForegroundColor
import com.erns.alertauni.ui.theme.MyPrioMedBg
import com.erns.alertauni.ui.theme.MyPrioMedFg
import com.erns.alertauni.ui.theme.MySecondaryColor
import com.erns.alertauni.ui.theme.MySurfaceColor

/**
 * Comentarios del anuncio (Figma F11, mq20): el anuncio aparece arriba como contexto,
 * hay botón atrás y, si la escritura está deshabilitada, un aviso lo explica (con
 * «Habilitar» para el docente), los comentarios previos siguen visibles y la entrada
 * queda bloqueada con un candado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCommentScreen(
    onVolver: () -> Unit,
    viewModel: PostCommentViewModel = hiltViewModel(),
) {
    val ui by viewModel.ui.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { viewModel.mensajes.collect { snackbar.showSnackbar(it) } }

    Scaffold(
        containerColor = MySecondaryColor,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Comentarios", style = AuText.Headline) },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MyPrimaryColor,
                    titleContentColor = MyPrimaryForegroundColor,
                    navigationIconContentColor = MyPrimaryForegroundColor
                )
            )
        },
        bottomBar = {
            EntradaComentario(
                bloqueada = ui.escrituraBloqueada,
                enviando = ui.enviando,
                onEnviar = viewModel::enviar
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ui.post?.let { post ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MySurfaceColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CursoChip(post.courseCode)
                        TipoChip(post.isPrivate)
                        PrioridadChip(post.priority)
                    }
                    Text(post.title, style = AuText.Title, color = MyForegroundColor)
                    if (post.isPrivate && post.receiverName != null) {
                        Text("Para: ${post.receiverName}", style = AuText.Label, color = com.erns.alertauni.ui.theme.MyPrivateFg)
                    }
                    Text(
                        "${ui.fechaPost} · ${comentariosTexto(post.countComments)}",
                        style = AuText.Caption, color = MyMutedForegroundColor
                    )
                }
                if (!post.allowComments) {
                    AvisoComentariosCerrados(esDocente = ui.esDocente, onHabilitar = viewModel::alternarComentarios)
                }
            }

            when {
                ui.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MyPrimaryColor)
                }
                ui.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(ui.error ?: "", style = AuText.Body, color = MyMutedForegroundColor)
                        TextButton(onClick = viewModel::cargar) { Text("Reintentar", color = MyPrimaryColor) }
                    }
                }
                ui.comentarios.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (ui.escrituraBloqueada) "No hay comentarios y la escritura está cerrada."
                        else "Aún no hay comentarios. Escribe el primero.",
                        style = AuText.Body, color = MyMutedForegroundColor
                    )
                }
                else -> ListaComentarios(ui.comentarios)
            }
        }
    }
}

@Composable
private fun ListaComentarios(comentarios: List<ComentarioUi>) {
    val estado = rememberLazyListState()
    LaunchedEffect(comentarios.size) { if (comentarios.isNotEmpty()) estado.animateScrollToItem(comentarios.lastIndex) }
    LazyColumn(
        state = estado,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(comentarios) { c ->
            c.separador?.let { dia ->
                Box(Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                    Text(
                        dia, style = AuText.Caption, color = MyMutedForegroundColor,
                        modifier = Modifier
                            .background(MySurfaceColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            Burbuja(c)
        }
    }
}

@Composable
private fun Burbuja(c: ComentarioUi) {
    val mio = c.comentario.isMine
    Box(Modifier.fillMaxWidth(), contentAlignment = if (mio) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (mio) Color(0xFFDCF8C6) else MySurfaceColor,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!mio) {
                    // Antes en rojo (Color.Red), que comunica error; ahora con el color de la marca.
                    Text(c.comentario.authorName, style = AuText.Label, color = MyPrimaryColor)
                }
                Text(c.comentario.content, style = AuText.Body, color = MyForegroundColor)
                Text(c.hora, style = AuText.Small, color = MyMutedForegroundColor, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

@Composable
private fun AvisoComentariosCerrados(esDocente: Boolean, onHabilitar: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MyPrioMedBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.CommentsDisabled, null, tint = MyPrioMedFg)
        Column(
            Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text("Comentarios deshabilitados", style = AuText.LabelLarge, color = MyPrioMedFg)
            Text(
                "Los estudiantes no pueden escribir; los comentarios anteriores siguen visibles.",
                style = AuText.Caption, color = MyPrioMedFg
            )
        }
        if (esDocente) {
            TextButton(onClick = onHabilitar) { Text("Habilitar", style = AuText.LabelLarge, color = MyPrimaryColor) }
        }
    }
}

@Composable
private fun EntradaComentario(bloqueada: Boolean, enviando: Boolean, onEnviar: (String) -> Unit) {
    var texto by rememberSaveable { mutableStateOf("") }
    Row(
        Modifier
            .fillMaxWidth()
            .background(MySecondaryColor)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = if (bloqueada) "" else texto,
            onValueChange = { texto = it },
            enabled = !bloqueada,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            placeholder = {
                Text(if (bloqueada) "Comentarios cerrados para los estudiantes" else "Escribe un comentario", style = AuText.Input)
            },
            leadingIcon = if (bloqueada) {
                { Icon(Icons.Filled.Lock, contentDescription = "Comentarios cerrados") }
            } else null,
            trailingIcon = {
                if (enviando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MyPrimaryColor)
                } else {
                    IconButton(
                        onClick = { onEnviar(texto); texto = "" },
                        enabled = !bloqueada && texto.isNotBlank()
                    ) { Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Enviar comentario") }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MySurfaceColor,
                unfocusedContainerColor = MySurfaceColor,
                disabledContainerColor = MyMutedColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTrailingIconColor = MyPrimaryColor,
                unfocusedTrailingIconColor = MyPrimaryColor
            ),
            maxLines = 4,
            textStyle = AuText.Body
        )
    }
}
