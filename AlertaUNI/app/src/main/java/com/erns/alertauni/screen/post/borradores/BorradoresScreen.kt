package com.erns.alertauni.screen.post.borradores

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erns.alertauni.data.local.DraftEntity
import com.erns.alertauni.screen.post.components.CargandoMuro
import com.erns.alertauni.screen.post.components.CursoChip
import com.erns.alertauni.screen.post.components.EstadoMuro
import com.erns.alertauni.screen.post.components.EtiquetaChip
import com.erns.alertauni.screen.post.components.PrioridadChip
import com.erns.alertauni.screen.post.components.TipoChip
import com.erns.alertauni.ui.theme.AuText
import com.erns.alertauni.ui.theme.MyForegroundColor
import com.erns.alertauni.ui.theme.MyMutedColor
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MyPrimaryForegroundColor
import com.erns.alertauni.ui.theme.MyPrioHighFg
import com.erns.alertauni.ui.theme.MyPrivateFg
import com.erns.alertauni.ui.theme.MySecondaryColor

/**
 * Borradores (Tarea 3). Esta pantalla no está en Figma: se diseñó con el mismo sistema visual
 * del muro, pero los borradores se distinguen claramente de los publicados por el borde
 * discontinuo, el fondo gris, la etiqueta «BORRADOR» y la ausencia de franja de prioridad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorradoresScreen(
    onVolver: () -> Unit,
    onContinuar: (Long) -> Unit,
    viewModel: BorradoresViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eliminados.collect { d ->
            val r = snackbar.showSnackbar(
                "Borrador «${d.titulo.ifBlank { "sin título" }}» eliminado",
                actionLabel = "Deshacer",
                duration = SnackbarDuration.Long
            )
            if (r == SnackbarResult.ActionPerformed) viewModel.restaurar(d)
        }
    }

    Scaffold(
        containerColor = MySecondaryColor,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Borradores", style = AuText.Headline) },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MyPrimaryColor,
                    titleContentColor = MyPrimaryForegroundColor,
                    navigationIconContentColor = MyPrimaryForegroundColor
                )
            )
        }
    ) { padding ->
        when (val estado = ui) {
            BorradoresUiState.Cargando -> Column(Modifier.padding(padding)) { CargandoMuro() }
            is BorradoresUiState.Lista -> if (estado.borradores.isEmpty()) {
                Column(Modifier.padding(padding)) {
                    EstadoMuro(
                        icono = Icons.Outlined.EditNote,
                        titulo = "No tienes borradores",
                        detalle = "En «Nuevo anuncio» usa «Guardar borrador» para terminarlo después. Los borradores no los ve nadie más."
                    )
                }
            } else {
                LazyColumn(
                    Modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Info, null, tint = MyMutedForegroundColor, modifier = Modifier.size(18.dp))
                            Text(
                                "Solo están guardados en este teléfono y en tu cuenta. Los estudiantes no los ven hasta que publiques.",
                                style = AuText.Caption, color = MyMutedForegroundColor,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    items(estado.borradores, key = { it.draft.id }) { b ->
                        BorradorCard(b, onContinuar = { onContinuar(b.draft.id) }, onEliminar = { viewModel.eliminar(b.draft) })
                    }
                }
            }
        }
    }
}

@Composable
private fun BorradorCard(b: BorradorUi, onContinuar: () -> Unit, onEliminar: () -> Unit) {
    val d: DraftEntity = b.draft
    val forma = RoundedCornerShape(12.dp)
    val colorBorde = MyMutedForegroundColor
    Column(
        Modifier
            .fillMaxWidth()
            .background(MyMutedColor, forma)
            // Borde discontinuo: comunica "todavía no publicado".
            .drawBehind {
                drawRoundRect(
                    color = colorBorde,
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
                )
            }
            .clickable(onClick = onContinuar)
            .padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            EtiquetaChip("BORRADOR", MyForegroundColor, MySecondaryColor, Icons.Outlined.EditNote)
            d.cursoCodigo?.let { CursoChip(it) }
            TipoChip(d.privado)
            PrioridadChip(d.prioridad)
        }
        Text(
            d.titulo.ifBlank { "(Sin título)" },
            style = if (d.titulo.isBlank()) AuText.Title.copy(fontStyle = FontStyle.Italic) else AuText.Title,
            color = if (d.titulo.isBlank()) MyMutedForegroundColor else MyForegroundColor,
            maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        if (d.privado) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Person, null, tint = MyPrivateFg, modifier = Modifier.size(14.dp))
                Text(
                    "Para: ${d.estudianteNombre ?: "sin elegir"}",
                    style = AuText.Label, color = MyPrivateFg, modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        if (d.contenido.isNotBlank()) {
            Text(d.contenido, style = AuText.Body, color = MyForegroundColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(b.editado, style = AuText.Caption, color = MyMutedForegroundColor, modifier = Modifier.weight(1f))
            TextButton(onClick = onEliminar) {
                Icon(Icons.Outlined.Delete, null, tint = MyPrioHighFg, modifier = Modifier.size(18.dp))
                Text("Eliminar", color = MyPrioHighFg, style = AuText.LabelLarge, modifier = Modifier.padding(start = 4.dp))
            }
            Spacer(Modifier.size(4.dp))
            FilledTonalButton(
                onClick = onContinuar,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = MyPrimaryColor, contentColor = MyPrimaryForegroundColor)
            ) { Text("Continuar", style = AuText.LabelLarge) }
        }
    }
}
