package com.erns.alertauni.screen.post.nuevo

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.outlined.SaveAs
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erns.alertauni.data.model.Prioridad
import com.erns.alertauni.screen.post.components.coloresPrioridad
import com.erns.alertauni.ui.theme.AuText
import com.erns.alertauni.ui.theme.MyBackgroundColor
import com.erns.alertauni.ui.theme.MyBorderColor
import com.erns.alertauni.ui.theme.MyForegroundColor
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MyPrimaryForegroundColor
import com.erns.alertauni.ui.theme.MyPrioHigh
import com.erns.alertauni.ui.theme.MyPrioHighBg
import com.erns.alertauni.ui.theme.MyPrioHighFg
import com.erns.alertauni.ui.theme.MyPrivateBg
import com.erns.alertauni.ui.theme.MyPrivateFg
import com.erns.alertauni.ui.theme.MyPublicBg
import com.erns.alertauni.ui.theme.MyPublicFg
import com.erns.alertauni.ui.theme.MySurfaceColor

/**
 * Formulario único «Nuevo anuncio» (Figma F02–F05, maquetas mq01, mq02, mq15, mq16, mq17).
 * La primera pregunta es a quién va dirigido; el destinatario se repite en el resumen y en el botón.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoAnuncioScreen(
    onCerrar: () -> Unit,
    onPublicado: (postId: Int, mensaje: String) -> Unit,
    viewModel: NuevoAnuncioViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var selectorAbierto by remember { mutableStateOf(false) }

    // El gesto/botón atrás del sistema también pasa por la confirmación de salida.
    BackHandler(onBack = viewModel::onVolver)

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { e ->
            when (e) {
                is FormEvento.Publicado -> onPublicado(e.postId, e.mensaje)
                is FormEvento.Error -> snackbar.showSnackbar(e.mensaje)
                is FormEvento.Mensaje -> snackbar.showSnackbar(e.texto)
                is FormEvento.Salir -> onCerrar()
            }
        }
    }

    Scaffold(
        containerColor = MyBackgroundColor,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(if (ui.borradorId != null) "Editar borrador" else "Nuevo anuncio", style = AuText.Title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                actions = {
                    // Tarea 3: guardar sin publicar (siempre visible en la barra superior).
                    TextButton(onClick = { viewModel.guardarBorrador() }, enabled = !ui.guardandoBorrador) {
                        Icon(Icons.Outlined.SaveAs, null, tint = MyPrimaryForegroundColor, modifier = Modifier.size(18.dp))
                        Text("Guardar borrador", style = AuText.LabelLarge, color = MyPrimaryForegroundColor, modifier = Modifier.padding(start = 6.dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = viewModel::onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MyPrimaryColor,
                    titleContentColor = MyPrimaryForegroundColor,
                    navigationIconContentColor = MyPrimaryForegroundColor,
                    actionIconContentColor = MyPrimaryForegroundColor
                )
            )
        },
        bottomBar = {
            BarraEnvio(
                resumen = ui.resumen,
                textoBoton = ui.textoBoton,
                enviando = ui.enviando,
                onEnviar = viewModel::onEnviar
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Etiqueta("¿A quién va dirigido?")
            SelectorTipo(privado = ui.datos.privado, onPrivado = viewModel::onPrivado)

            Espacio()
            Etiqueta("Curso", error = ui.error(Campo.CURSO) != null)
            SelectorCurso(ui, viewModel::onCurso)

            // Divulgación progresiva: el estudiante solo se pide en un anuncio privado.
            AnimatedVisibility(visible = ui.datos.privado) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Espacio()
                    Etiqueta("Estudiante", error = ui.error(Campo.ESTUDIANTE) != null)
                    CampoEstudiante(ui, onClick = { selectorAbierto = true })
                }
            }

            Espacio()
            Etiqueta("Prioridad")
            SelectorPrioridad(ui.datos.prioridad, viewModel::onPrioridad)

            Espacio()
            Etiqueta("Título", error = ui.error(Campo.TITULO) != null)
            OutlinedTextField(
                value = ui.datos.titulo,
                onValueChange = viewModel::onTitulo,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej.: Práctica calificada 2", style = AuText.Input) },
                singleLine = true,
                textStyle = AuText.Input,
                isError = ui.error(Campo.TITULO) != null,
                supportingText = ui.error(Campo.TITULO)?.let { { Text(it) } },
                trailingIcon = ui.error(Campo.TITULO)?.let { { Icon(Icons.Filled.Error, null) } },
                shape = RoundedCornerShape(8.dp),
                colors = coloresCampo()
            )

            Etiqueta("Contenido", error = ui.error(Campo.CONTENIDO) != null)
            OutlinedTextField(
                value = ui.datos.contenido,
                onValueChange = viewModel::onContenido,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Escribe el contenido del anuncio…", style = AuText.Body) },
                minLines = 4,
                maxLines = 8,
                textStyle = AuText.Body,
                isError = ui.error(Campo.CONTENIDO) != null,
                supportingText = {
                    Row(Modifier.fillMaxWidth()) {
                        Text(ui.error(Campo.CONTENIDO) ?: "", modifier = Modifier.weight(1f))
                        Text("${ui.datos.contenido.length} / $MAX_CONTENIDO")
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = coloresCampo()
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Permitir comentarios", style = AuText.LabelLarge, color = MyForegroundColor)
                    Text(
                        if (ui.datos.privado) "El estudiante podrá responder" else "Los estudiantes podrán responder",
                        style = AuText.Caption, color = MyMutedForegroundColor
                    )
                }
                Switch(
                    checked = ui.datos.permitirComentarios,
                    onCheckedChange = viewModel::onPermitirComentarios,
                    colors = SwitchDefaults.colors(checkedTrackColor = MyPrimaryColor)
                )
            }
        }
    }

    if (selectorAbierto) {
        SelectorEstudianteSheet(
            curso = ui.curso,
            estudiantes = ui.estudiantes,
            cargando = ui.cargandoEstudiantes,
            seleccionadoId = ui.datos.estudianteId,
            onElegir = { viewModel.onEstudiante(it); selectorAbierto = false },
            onCerrar = { selectorAbierto = false }
        )
    }

    if (ui.confirmandoSalida) {
        AlertDialog(
            onDismissRequest = viewModel::onSeguirEditando,
            containerColor = MySurfaceColor,
            icon = { Icon(Icons.Outlined.SaveAs, null, tint = MyPrimaryColor) },
            title = { Text("¿Guardar como borrador?", style = AuText.Headline) },
            text = {
                Text(
                    "Tienes cambios sin publicar. Puedes guardarlos como borrador y continuar después, o descartarlos.",
                    style = AuText.Body, color = MyMutedForegroundColor
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.guardarBorrador(yCerrar = true) },
                    colors = ButtonDefaults.buttonColors(containerColor = MyPrimaryColor)
                ) { Text("Guardar borrador") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = viewModel::onDescartar) { Text("Descartar", color = MyPrioHighFg) }
                    TextButton(onClick = viewModel::onSeguirEditando) { Text("Seguir editando", color = MyPrimaryColor) }
                }
            }
        )
    }

    if (ui.confirmandoPrivado) {
        ConfirmarPrivadoDialog(ui, viewModel::onConfirmarPrivado, viewModel::onCancelarConfirmacion)
    }
}

// ---------------- Secciones ----------------

@Composable
private fun Etiqueta(texto: String, error: Boolean = false) {
    Text(texto, style = AuText.Caption, color = if (error) MyPrioHighFg else MyMutedForegroundColor)
}

@Composable
private fun Espacio() = Spacer(Modifier.height(8.dp))

@Composable
private fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MySurfaceColor,
    unfocusedContainerColor = MySurfaceColor,
    errorContainerColor = MySurfaceColor,
    unfocusedBorderColor = MyBorderColor,
    focusedBorderColor = MyPrimaryColor
)

/** Público / Privado: dos opciones excluyentes, siempre visibles (SingleChoiceSegmentedButtonRow). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorTipo(privado: Boolean, onPrivado: (Boolean) -> Unit) {
    val opciones = listOf(
        Triple(false, "Público", Icons.Filled.Groups),
        Triple(true, "Privado", Icons.Filled.Lock)
    )
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        opciones.forEachIndexed { i, (esPrivado, texto, icono) ->
            val activo = privado == esPrivado
            SegmentedButton(
                selected = activo,
                onClick = { onPrivado(esPrivado) },
                shape = SegmentedButtonDefaults.itemShape(i, opciones.size),
                icon = {
                    Icon(if (activo) Icons.Filled.Check else icono, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = if (esPrivado) MyPrivateBg else MyPublicBg,
                    activeContentColor = if (esPrivado) MyPrivateFg else MyPublicFg,
                    inactiveContainerColor = MySurfaceColor,
                    inactiveContentColor = MyForegroundColor,
                    activeBorderColor = MyBorderColor,
                    inactiveBorderColor = MyBorderColor
                )
            ) { Text(texto, style = AuText.LabelLarge) }
        }
    }
}

/** Curso con su acrónimo y nombre (antes: «Selecciona una opción» y solo el nombre largo). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorCurso(ui: FormUiState, onCurso: (String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    val error = ui.error(Campo.CURSO)
    ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { abierto = it }) {
        OutlinedTextField(
            value = ui.curso?.let { "${it.courseCode} · ${it.courseName}" } ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Elige el curso", style = AuText.Input) },
            textStyle = AuText.Input,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            shape = RoundedCornerShape(8.dp),
            colors = coloresCampo(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            containerColor = MySurfaceColor
        ) {
            ui.cursos.forEach { c ->
                DropdownMenuItem(
                    text = { Text("${c.courseCode} · ${c.courseName}") },
                    onClick = { abierto = false; onCurso(c.id) }
                )
            }
        }
    }
}

@Composable
private fun CampoEstudiante(ui: FormUiState, onClick: () -> Unit) {
    val error = ui.error(Campo.ESTUDIANTE)
    val estudiante = ui.estudiante
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MySurfaceColor, RoundedCornerShape(8.dp))
                .border(if (error != null) 2.dp else 1.dp, if (error != null) MyPrioHigh else MyBorderColor, RoundedCornerShape(8.dp))
                .clickable(enabled = ui.curso != null, onClick = onClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (estudiante != null) {
                Iniciales("${estudiante.firstname} ${estudiante.surname}", seleccionado = true)
                Text(
                    "${estudiante.firstname} ${estudiante.surname} · ${estudiante.codigo}",
                    style = AuText.Input, color = MyForegroundColor,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                )
            } else {
                Icon(Icons.Filled.Person, null, tint = MyMutedForegroundColor)
                Text(
                    if (ui.curso == null) "Primero elige el curso" else "Selecciona un estudiante",
                    style = AuText.Input, color = MyMutedForegroundColor,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                )
            }
            if (error != null) Icon(Icons.Filled.Error, null, tint = MyPrioHigh)
            Icon(Icons.Filled.ArrowDropDown, null, tint = MyMutedForegroundColor)
        }
        Text(
            error ?: ui.curso?.let { "Solo estudiantes matriculados en ${it.courseCode}" } ?: "",
            style = AuText.Caption,
            color = if (error != null) MyPrioHighFg else MyMutedForegroundColor,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )
    }
}

/** Tres opciones visibles con color + ícono + texto (sin desplegables). */
@Composable
private fun SelectorPrioridad(prioridad: Int, onPrioridad: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Prioridad.todas.forEach { p ->
            val activo = p == prioridad
            val c = coloresPrioridad(p)
            OutlinedButton(
                onClick = { onPrioridad(p) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(if (activo) 2.dp else 1.dp, if (activo) c.franja else MyBorderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (activo) c.fondo else MySurfaceColor,
                    contentColor = if (activo) c.texto else MyForegroundColor
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
            ) {
                Icon(
                    if (activo) Icons.Filled.Check else Icons.Filled.Flag, null,
                    tint = if (activo) c.texto else c.franja, modifier = Modifier.size(16.dp)
                )
                Text(Prioridad.etiqueta(p), style = AuText.LabelLarge, modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

/** Resumen fijo del destinatario + botón que lo repite (redundancia deliberada en lo crítico). */
@Composable
private fun BarraEnvio(
    resumen: ResumenDestinatario,
    textoBoton: String,
    enviando: Boolean,
    onEnviar: () -> Unit
) {
    val (fondo, color, icono) = when (resumen.tono) {
        ResumenDestinatario.Tono.PUBLICO -> Triple(MyPublicBg, MyPublicFg, Icons.Filled.Groups)
        ResumenDestinatario.Tono.PRIVADO -> Triple(MyPrivateBg, MyPrivateFg, Icons.Filled.Lock)
        ResumenDestinatario.Tono.ERROR -> Triple(MyPrioHighBg, MyPrioHighFg, Icons.Filled.Error)
        ResumenDestinatario.Tono.NEUTRO -> Triple(MyBorderColor, MyMutedForegroundColor, Icons.Filled.Groups)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .background(MyBackgroundColor)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(fondo, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, null, tint = color)
            Column(Modifier.padding(start = 12.dp)) {
                Text(resumen.titulo, style = AuText.LabelLarge, color = color)
                Text(resumen.detalle, style = AuText.Caption, color = color)
            }
        }
        Button(
            onClick = onEnviar,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MyPrimaryColor),
            shape = RoundedCornerShape(22.dp)
        ) {
            if (enviando) {
                CircularProgressIndicator(Modifier.size(20.dp), color = MyPrimaryForegroundColor, strokeWidth = 2.dp)
            } else {
                Text(textoBoton, style = AuText.LabelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun Iniciales(nombre: String, seleccionado: Boolean, tam: androidx.compose.ui.unit.Dp = 32.dp) {
    val ini = nombre.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Box(
        Modifier
            .size(tam)
            .background(if (seleccionado) MyPrivateFg else MyPrivateBg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(ini, style = AuText.Label, color = if (seleccionado) MyPrimaryForegroundColor else MyPrivateFg)
    }
}

/** Confirmación SOLO para privados (mq16): repite destinatario, curso, título y prioridad. */
@Composable
private fun ConfirmarPrivadoDialog(ui: FormUiState, onConfirmar: () -> Unit, onRevisar: () -> Unit) {
    val e = ui.estudiante ?: return
    val prioridad = coloresPrioridad(ui.datos.prioridad)
    AlertDialog(
        onDismissRequest = onRevisar,
        containerColor = MySurfaceColor,
        icon = {
            Box(
                Modifier
                    .size(48.dp)
                    .background(MyPrivateBg, CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Lock, null, tint = MyPrivateFg) }
        },
        title = { Text("¿Enviar anuncio privado?", style = AuText.Headline) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MyBackgroundColor, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilaDato("Para", NuevoAnuncioViewModel.nombreCompleto(e))
                    FilaDato("Curso", ui.curso?.courseCode ?: "")
                    FilaDato("Título", ui.datos.titulo.trim())
                    FilaDato("Prioridad", Prioridad.etiqueta(ui.datos.prioridad), prioridad.texto)
                }
                Text(
                    "Solo este estudiante podrá verlo y responderlo. Ningún otro estudiante del curso lo recibirá.",
                    style = AuText.Body, color = MyMutedForegroundColor
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirmar, colors = ButtonDefaults.buttonColors(containerColor = MyPrimaryColor)) {
                Text("Enviar a ${e.firstname}")
            }
        },
        dismissButton = { TextButton(onClick = onRevisar) { Text("Revisar", color = MyPrimaryColor) } }
    )
}

@Composable
private fun FilaDato(etiqueta: String, valor: String, color: Color = MyForegroundColor) {
    Row {
        Text(etiqueta, style = AuText.Caption, color = MyMutedForegroundColor, modifier = Modifier.size(width = 72.dp, height = 20.dp))
        Text(valor, style = AuText.LabelLarge, color = color, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
