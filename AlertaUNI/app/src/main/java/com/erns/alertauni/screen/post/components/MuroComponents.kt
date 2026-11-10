package com.erns.alertauni.screen.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.erns.alertauni.data.model.Prioridad
import com.erns.alertauni.domain.muro.FiltroMuro
import com.erns.alertauni.ui.theme.AuText
import com.erns.alertauni.ui.theme.MyForegroundColor
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MyPrimaryForegroundColor
import com.erns.alertauni.ui.theme.MySelectedColor
import com.erns.alertauni.ui.theme.MySurfaceColor

/**
 * Cabecera del muro: nombre del docente, título y campo de búsqueda SIEMPRE visible
 * (antes la búsqueda estaba tras una lupa y no filtraba). El estado del texto vive en el ViewModel.
 */
@Composable
fun MuroHeader(
    usuario: String,
    texto: String,
    onTexto: (String) -> Unit,
    accion: (@Composable () -> Unit)? = null
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MyPrimaryColor)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(usuario.uppercase(), style = AuText.Small, color = MyPrimaryForegroundColor.copy(alpha = 0.85f), maxLines = 1)
                Text("Anuncios", style = AuText.Headline, color = MyPrimaryForegroundColor)
            }
            accion?.invoke()
        }
        Spacer(Modifier.height(10.dp))
        CampoBusqueda(texto, onTexto)
    }
}

@Composable
fun CampoBusqueda(texto: String, onTexto: (String) -> Unit) {
    val foco = LocalFocusManager.current
    BasicTextField(
        value = texto,
        onValueChange = onTexto,
        singleLine = true,
        textStyle = AuText.Input.copy(color = MyForegroundColor),
        cursorBrush = SolidColor(MyPrimaryColor),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { foco.clearFocus() }),
        decorationBox = { campo ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(MySurfaceColor, RoundedCornerShape(24.dp))
                    .padding(start = 14.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = MyMutedForegroundColor)
                Box(Modifier.weight(1f).padding(start = 10.dp)) {
                    if (texto.isEmpty()) {
                        Text("Buscar por título o contenido", style = AuText.Input, color = MyMutedForegroundColor)
                    }
                    campo()
                }
                if (texto.isNotEmpty()) {
                    // Volver a la lista completa con un toque.
                    IconButton(onClick = { onTexto(""); foco.clearFocus() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda", tint = MyMutedForegroundColor)
                    }
                }
            }
        }
    )
}

/** Fila de chips combinables: Curso, Tipo, Prioridad y Archivados (LazyRow + DropdownMenu). */
@Composable
fun FiltrosChips(
    filtro: FiltroMuro,
    cursos: List<String>,
    onCurso: (String?) -> Unit,
    onTipo: (Boolean?) -> Unit,
    onPrioridad: (Int?) -> Unit,
    onArchivados: (Boolean) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (filtro.archivados) {
            item { ChipArchivados(true, onArchivados) }
        }
        item {
            ChipDesplegable(
                etiqueta = filtro.curso ?: "Curso",
                seleccionado = filtro.curso != null,
                opciones = listOf<Pair<String, String?>>("Todos los cursos" to null) + cursos.map { it to it },
                onElegir = onCurso
            )
        }
        item {
            ChipDesplegable(
                etiqueta = when (filtro.privado) { true -> "Privados"; false -> "Públicos"; null -> "Tipo" },
                seleccionado = filtro.privado != null,
                opciones = listOf("Todos" to null, "Públicos" to false, "Privados" to true),
                onElegir = onTipo
            )
        }
        item {
            ChipDesplegable(
                etiqueta = filtro.prioridad?.let { "Prioridad: ${Prioridad.etiqueta(it)}" } ?: "Prioridad",
                seleccionado = filtro.prioridad != null,
                opciones = listOf<Pair<String, Int?>>("Todas" to null) + Prioridad.todas.map { Prioridad.etiqueta(it) to it },
                onElegir = onPrioridad
            )
        }
        if (!filtro.archivados) {
            item { ChipArchivados(false, onArchivados) }
        }
    }
}

private val coloresChip
    @Composable get() = FilterChipDefaults.filterChipColors(
        containerColor = MySurfaceColor,
        labelColor = MyForegroundColor,
        iconColor = MyForegroundColor,
        selectedContainerColor = MySelectedColor,
        selectedLabelColor = MyPrimaryColor,
        selectedLeadingIconColor = MyPrimaryColor,
        selectedTrailingIconColor = MyPrimaryColor
    )

@Composable
private fun <T> ChipDesplegable(
    etiqueta: String,
    seleccionado: Boolean,
    opciones: List<Pair<String, T>>,
    onElegir: (T) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = seleccionado,
            onClick = { abierto = true },
            label = { Text(etiqueta, style = AuText.LabelLarge) },
            leadingIcon = if (seleccionado) {
                { Icon(Icons.Filled.Check, null, Modifier.size(18.dp)) }
            } else null,
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, Modifier.size(18.dp)) },
            colors = coloresChip,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true, selected = seleccionado,
                borderColor = Color.Transparent, selectedBorderColor = Color.Transparent
            )
        )
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }, containerColor = MySurfaceColor) {
            opciones.forEach { (texto, valor) ->
                DropdownMenuItem(text = { Text(texto) }, onClick = { abierto = false; onElegir(valor) })
            }
        }
    }
}

@Composable
private fun ChipArchivados(activo: Boolean, onArchivados: (Boolean) -> Unit) {
    FilterChip(
        selected = activo,
        onClick = { onArchivados(!activo) },
        label = { Text("Archivados", style = AuText.LabelLarge) },
        leadingIcon = { Icon(if (activo) Icons.Filled.Check else Icons.Outlined.Archive, null, Modifier.size(18.dp)) },
        colors = coloresChip,
        border = FilterChipDefaults.filterChipBorder(
            enabled = true, selected = activo,
            borderColor = Color.Transparent, selectedBorderColor = Color.Transparent
        )
    )
}

/** Estado vacío / sin resultados / error con ícono, explicación y salidas (mq12, mq22). */
@Composable
fun EstadoMuro(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    accionPrincipal: Pair<String, () -> Unit>? = null,
    accionSecundaria: Pair<String, () -> Unit>? = null
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(88.dp)
                .background(MySurfaceColor, CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(icono, null, tint = MyPrimaryColor, modifier = Modifier.size(40.dp)) }
        Spacer(Modifier.height(20.dp))
        Text(titulo, style = AuText.Title, color = MyForegroundColor, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(detalle, style = AuText.Body, color = MyMutedForegroundColor, textAlign = TextAlign.Center)
        if (accionPrincipal != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = accionPrincipal.second,
                colors = ButtonDefaults.buttonColors(containerColor = MyPrimaryColor)
            ) { Text(accionPrincipal.first, style = AuText.LabelLarge) }
        }
        if (accionSecundaria != null) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = accionSecundaria.second) {
                Text(accionSecundaria.first, style = AuText.LabelLarge, color = MyPrimaryColor)
            }
        }
    }
}

@Composable
fun CargandoMuro() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MyPrimaryColor)
    }
}

object IconosEstado {
    val vacio = Icons.Outlined.Inbox
    val sinResultados = Icons.Outlined.SearchOff
    val error = Icons.Outlined.CloudOff
}
