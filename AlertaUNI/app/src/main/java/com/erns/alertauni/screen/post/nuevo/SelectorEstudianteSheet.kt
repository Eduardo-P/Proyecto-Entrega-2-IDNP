package com.erns.alertauni.screen.post.nuevo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.StudentEntity
import com.erns.alertauni.domain.muro.TextoBusqueda
import com.erns.alertauni.screen.post.components.CampoBusqueda
import com.erns.alertauni.ui.theme.AuText
import com.erns.alertauni.ui.theme.MyBackgroundColor
import com.erns.alertauni.ui.theme.MyForegroundColor
import com.erns.alertauni.ui.theme.MyMutedForegroundColor
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MyPrivateBg
import com.erns.alertauni.ui.theme.MyPrivateFg
import com.erns.alertauni.ui.theme.MySecondaryColor
import com.erns.alertauni.ui.theme.MySurfaceColor

/**
 * Hoja inferior «Elegir estudiante» (mq15). Solo lista a los matriculados del curso elegido
 * (service-course-student) y permite buscar por nombre o código: evita escribir nombres a mano
 * y escala a cursos de cientos de estudiantes (LazyColumn).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorEstudianteSheet(
    curso: CatalogCourseEntity?,
    estudiantes: List<StudentEntity>,
    cargando: Boolean,
    seleccionadoId: String?,
    onElegir: (String) -> Unit,
    onCerrar: () -> Unit
) {
    val estado = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var consulta by rememberSaveable { mutableStateOf("") }
    var elegido by rememberSaveable { mutableStateOf(seleccionadoId) }
    val visibles = remember(estudiantes, consulta) {
        estudiantes.filter { TextoBusqueda.contiene("${it.surname} ${it.firstname} ${it.codigo}", consulta) }
    }
    val actual = estudiantes.firstOrNull { it.student_id == elegido }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estado,
        containerColor = MySurfaceColor
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .imePadding()
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Elegir estudiante", style = AuText.Headline, color = MyForegroundColor)
                Text(
                    "${curso?.courseCode ?: ""} · ${estudiantes.size} estudiantes matriculados",
                    style = AuText.Caption, color = MyMutedForegroundColor
                )
                Box(Modifier.padding(vertical = 12.dp)) {
                    CampoBusqueda(consulta, { consulta = it }, placeholder = "Buscar por nombre o código", fondo = MySecondaryColor)
                }
            }

            when {
                cargando -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MyPrimaryColor)
                }
                visibles.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f).padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (estudiantes.isEmpty()) "Este curso no tiene estudiantes matriculados." else "Ningún estudiante coincide con «$consulta».",
                        style = AuText.Body, color = MyMutedForegroundColor
                    )
                }
                else -> LazyColumn(Modifier.weight(1f)) {
                    items(visibles, key = { it.student_id }) { e ->
                        val activo = e.student_id == elegido
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .background(if (activo) MyPrivateBg else MySurfaceColor, RoundedCornerShape(12.dp))
                                .clickable { elegido = e.student_id }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Iniciales("${e.firstname} ${e.surname}", seleccionado = activo, tam = 40.dp)
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${e.surname}, ${e.firstname}",
                                    style = AuText.LabelLarge,
                                    color = if (activo) MyPrivateFg else MyForegroundColor,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                                Text("Cód. ${e.codigo}", style = AuText.Caption, color = MyMutedForegroundColor)
                            }
                            RadioButton(
                                selected = activo,
                                onClick = { elegido = e.student_id },
                                colors = RadioButtonDefaults.colors(selectedColor = MyPrivateFg)
                            )
                        }
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(MyBackgroundColor)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { elegido?.let(onElegir) },
                    enabled = actual != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MyPrimaryColor)
                ) {
                    Text(
                        actual?.let { "Elegir a ${it.firstname} ${it.surname}" } ?: "Elige un estudiante de la lista",
                        style = AuText.LabelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
