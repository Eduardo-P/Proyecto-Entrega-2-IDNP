package com.erns.alertauni.screen.post.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CommentsDisabled
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.data.model.Prioridad
import com.erns.alertauni.screen.post.AnuncioUi
import com.erns.alertauni.ui.theme.AuText
import com.erns.alertauni.ui.theme.MyBorderColor
import com.erns.alertauni.ui.theme.MyForegroundColor
import com.erns.alertauni.ui.theme.MyMutedColor
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MyPrioHigh
import com.erns.alertauni.ui.theme.MyPrioHighBg
import com.erns.alertauni.ui.theme.MyPrioHighFg
import com.erns.alertauni.ui.theme.MyPrioLow
import com.erns.alertauni.ui.theme.MyPrioLowBg
import com.erns.alertauni.ui.theme.MyPrioLowFg
import com.erns.alertauni.ui.theme.MyPrioMed
import com.erns.alertauni.ui.theme.MyPrioMedBg
import com.erns.alertauni.ui.theme.MyPrioMedFg
import com.erns.alertauni.ui.theme.MyPrivateBg
import com.erns.alertauni.ui.theme.MyPrivateFg
import com.erns.alertauni.ui.theme.MyPublicBg
import com.erns.alertauni.ui.theme.MyPublicFg
import com.erns.alertauni.ui.theme.MySurfaceColor

/** Colores de la doble codificación de prioridad (franja + chip con texto). */
data class ColoresPrioridad(val franja: Color, val fondo: Color, val texto: Color)

fun coloresPrioridad(p: Int) = when (p) {
    Prioridad.ALTA -> ColoresPrioridad(MyPrioHigh, MyPrioHighBg, MyPrioHighFg)
    Prioridad.BAJA -> ColoresPrioridad(MyPrioLow, MyPrioLowBg, MyPrioLowFg)
    else -> ColoresPrioridad(MyPrioMed, MyPrioMedBg, MyPrioMedFg)
}

/** Chip pequeño de 22 dp de alto (componentes «Chip» de Figma). */
@Composable
fun EtiquetaChip(
    texto: String,
    fondo: Color,
    color: Color,
    icono: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(22.dp)
            .background(fondo, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icono != null) Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(texto, style = AuText.Label, color = color, maxLines = 1)
    }
}

@Composable
fun CursoChip(codigo: String) = EtiquetaChip(codigo, MyMutedColor, MyForegroundColor)

/** El ícono va acompañado siempre de la palabra: un ícono solo es ambiguo y los lectores de pantalla necesitan la etiqueta. */
@Composable
fun TipoChip(privado: Boolean) = if (privado) {
    EtiquetaChip("Privado", MyPrivateBg, MyPrivateFg, Icons.Filled.Lock)
} else {
    EtiquetaChip("Público", MyPublicBg, MyPublicFg, Icons.Filled.Groups)
}

@Composable
fun PrioridadChip(prioridad: Int) {
    val c = coloresPrioridad(prioridad)
    EtiquetaChip(Prioridad.etiqueta(prioridad), c.fondo, c.texto, Icons.Filled.Flag)
}

/** Texto con las coincidencias de la búsqueda resaltadas (rangos en el texto original). */
fun resaltar(texto: String, rangos: List<IntRange>): AnnotatedString = buildAnnotatedString {
    append(texto)
    val estilo = SpanStyle(color = MyPrioMedFg, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
    rangos.forEach { r ->
        if (r.first >= 0 && r.last < texto.length) addStyle(estilo, r.first, r.last + 1)
    }
}

data class AccionesAnuncio(
    val onAbrir: (PostEntity) -> Unit,
    val onAlternarComentarios: (PostEntity) -> Unit,
    val onArchivar: (PostEntity) -> Unit,
    val onRestaurar: (PostEntity) -> Unit
)

/**
 * Tarjeta del muro (componente AnuncioCard de Figma): franja de prioridad de 5 dp,
 * chips de curso/tipo/prioridad, menú ⋮, título, destinatario si es privado, resumen de
 * 2 líneas, fecha amigable y número de comentarios. Sin autor (siempre es el docente).
 * La altura se adapta al contenido (antes era fija de 220 dp).
 */
@Composable
fun AnuncioCard(
    anuncio: AnuncioUi,
    acciones: AccionesAnuncio,
    resaltada: Boolean = false,
    modifier: Modifier = Modifier
) {
    val post = anuncio.post
    val prioridad = coloresPrioridad(post.priority)
    var menu by remember { mutableStateOf(false) }

    Column(modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { acciones.onAbrir(post) },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MySurfaceColor),
            border = BorderStroke(if (resaltada) 2.dp else 1.dp, if (resaltada) MyPrimaryColor else MyBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(Modifier.height(IntrinsicSize.Min)) {
                Box(
                    Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(prioridad.franja)
                )
                Column(
                    Modifier
                        .weight(1f)
                        .alpha(if (post.isArchived) 0.6f else 1f)
                        .padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CursoChip(post.courseCode)
                            TipoChip(post.isPrivate)
                            PrioridadChip(post.priority)
                        }
                        Box {
                            IconButton(onClick = { menu = true }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "Acciones del anuncio",
                                    tint = MyMutedForegroundColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            MenuAnuncio(menu, { menu = false }, post, acciones)
                        }
                    }

                    Text(
                        resaltar(post.title, anuncio.coincidenciasTitulo),
                        style = AuText.Title,
                        color = MyForegroundColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (post.isPrivate) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Person, null, tint = MyPrivateFg, modifier = Modifier.size(14.dp))
                            Text(
                                "Para: ${post.receiverName ?: "estudiante"}",
                                style = AuText.Label,
                                color = MyPrivateFg,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        resaltar(post.content, anuncio.coincidenciasContenido),
                        style = AuText.Body,
                        color = MyForegroundColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    HorizontalDivider(color = MyBorderColor)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, null, tint = MyMutedForegroundColor, modifier = Modifier.size(16.dp))
                        Text(
                            anuncio.fechaArchivado ?: anuncio.fecha,
                            style = AuText.Caption,
                            color = MyMutedForegroundColor,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Spacer(Modifier.weight(1f))
                        val cerrados = !post.allowComments
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.semantics {
                                contentDescription = "${post.countComments} comentarios" + if (cerrados) ", cerrados" else ""
                            }
                        ) {
                            Icon(
                                if (cerrados) Icons.Outlined.CommentsDisabled else Icons.Outlined.ChatBubbleOutline,
                                null,
                                tint = if (cerrados) MyMutedForegroundColor else MyPrimaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                comentariosTexto(post.countComments) + if (cerrados) " · cerrados" else "",
                                style = AuText.Caption,
                                color = if (cerrados) MyMutedForegroundColor else MyPrimaryColor,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        if (post.isArchived) {
            TextButton(
                onClick = { acciones.onRestaurar(post) },
                modifier = Modifier.align(Alignment.End)
            ) { Text("Restaurar al muro", style = AuText.LabelLarge, color = MyPrimaryColor) }
        }
    }
}

fun comentariosTexto(n: Int) = if (n == 1) "1 comentario" else "$n comentarios"

@Composable
private fun MenuAnuncio(
    abierto: Boolean,
    onCerrar: () -> Unit,
    post: PostEntity,
    acciones: AccionesAnuncio
) {
    DropdownMenu(expanded = abierto, onDismissRequest = onCerrar, containerColor = MySurfaceColor) {
        DropdownMenuItem(
            text = { Text("Ver comentarios (${post.countComments})") },
            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Chat, null) },
            onClick = { onCerrar(); acciones.onAbrir(post) }
        )
        DropdownMenuItem(
            // El texto cambia según el estado actual.
            text = { Text(if (post.allowComments) "Deshabilitar comentarios" else "Habilitar comentarios") },
            leadingIcon = {
                Icon(if (post.allowComments) Icons.Outlined.CommentsDisabled else Icons.Outlined.ChatBubbleOutline, null)
            },
            onClick = { onCerrar(); acciones.onAlternarComentarios(post) }
        )
        HorizontalDivider(color = MyBorderColor)
        if (post.isArchived) {
            DropdownMenuItem(
                text = { Text("Restaurar al muro") },
                leadingIcon = { Icon(Icons.Outlined.Unarchive, null) },
                onClick = { onCerrar(); acciones.onRestaurar(post) }
            )
        } else {
            DropdownMenuItem(
                text = { Text("Archivar") },
                leadingIcon = { Icon(Icons.Outlined.Archive, null) },
                onClick = { onCerrar(); acciones.onArchivar(post) }
            )
        }
    }
}
