package com.erns.alertauni.screen.post.nuevo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.CourseCatalogRequest
import com.erns.alertauni.data.model.NuevoAnuncioRequest
import com.erns.alertauni.data.model.Prioridad
import com.erns.alertauni.data.model.StudentEntity
import com.erns.alertauni.data.repository.BorradorNuevo
import com.erns.alertauni.data.repository.ContactRepository
import com.erns.alertauni.data.repository.DraftRepository
import com.erns.alertauni.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val MAX_CONTENIDO = 280

/** Datos que escribe el docente (también es lo que se guarda como borrador). */
data class DatosAnuncio(
    val privado: Boolean = false,
    val cursoId: String? = null,
    val estudianteId: String? = null,
    val prioridad: Int = Prioridad.MEDIA,
    val titulo: String = "",
    val contenido: String = "",
    val permitirComentarios: Boolean = true
)

enum class Campo { CURSO, ESTUDIANTE, TITULO, CONTENIDO }

/** Barra fija junto al botón que dice quién recibirá el anuncio (mq01, mq02, mq17). */
data class ResumenDestinatario(val titulo: String, val detalle: String, val tono: Tono) {
    enum class Tono { PUBLICO, PRIVADO, NEUTRO, ERROR }
}

data class FormUiState(
    val datos: DatosAnuncio = DatosAnuncio(),
    val cursos: List<CatalogCourseEntity> = emptyList(),
    val estudiantes: List<StudentEntity> = emptyList(),
    val cargandoEstudiantes: Boolean = false,
    val intentoEnviar: Boolean = false,
    val enviando: Boolean = false,
    val confirmandoPrivado: Boolean = false,
    val errores: Map<Campo, String> = emptyMap(),
    val resumen: ResumenDestinatario = ResumenDestinatario("", "", ResumenDestinatario.Tono.NEUTRO),
    val textoBoton: String = "Publicar",
    /** Borrador que se está editando (null = anuncio nuevo). */
    val borradorId: Long? = null,
    /** Últimos datos guardados (para saber si hay cambios sin guardar). */
    val datosGuardados: DatosAnuncio = DatosAnuncio(),
    val confirmandoSalida: Boolean = false,
    val guardandoBorrador: Boolean = false
) {
    val hayCambios: Boolean get() = datos != datosGuardados
    val estaVacio: Boolean get() = datos.titulo.isBlank() && datos.contenido.isBlank() && datos.estudianteId == null
    val curso: CatalogCourseEntity? get() = cursos.firstOrNull { it.id == datos.cursoId }
    val estudiante: StudentEntity? get() = estudiantes.firstOrNull { it.student_id == datos.estudianteId }
    fun error(campo: Campo): String? = if (intentoEnviar) errores[campo] else null
}

sealed interface FormEvento {
    data class Publicado(val postId: Int, val mensaje: String) : FormEvento
    data class Error(val mensaje: String) : FormEvento
    data class Mensaje(val texto: String) : FormEvento
    /** Cerrar el formulario (tras guardar el borrador o descartar los cambios). */
    data class Salir(val mensaje: String? = null) : FormEvento
}

/**
 * Formulario único de publicación (Problema 1, alternativa A).
 * Argumentos opcionales de navegación: cursoId y estudianteId (atajo desde Contactos).
 */
@HiltViewModel
class NuevoAnuncioViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val contactRepository: ContactRepository,
    private val draftRepository: DraftRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _ui = MutableStateFlow(FormUiState())
    val ui: StateFlow<FormUiState> = _ui.asStateFlow()

    private val _eventos = Channel<FormEvento>(Channel.BUFFERED)
    val eventos: Flow<FormEvento> = _eventos.receiveAsFlow()

    private var cargaEstudiantes: Job? = null

    init {
        val borradorInicial = savedStateHandle.get<String>(ARG_BORRADOR)?.toLongOrNull()
        if (borradorInicial != null) cargarBorrador(borradorInicial)
        val cursoInicial = savedStateHandle.get<String>(ARG_CURSO)?.takeIf { it.isNotBlank() }
        val estudianteInicial = savedStateHandle.get<String>(ARG_ESTUDIANTE)?.takeIf { it.isNotBlank() }
        if (estudianteInicial != null) {
            actualizar { it.copy(privado = true, cursoId = cursoInicial, estudianteId = estudianteInicial) }
        } else if (cursoInicial != null) {
            actualizar { it.copy(cursoId = cursoInicial) }
        } else {
            recalcular()
        }
        viewModelScope.launch {
            postRepository.getCourseCatalog().onSuccess { cursos ->
                _ui.update { it.copy(cursos = cursos) }
                // Con un solo curso, se elige solo (un paso menos).
                if (_ui.value.datos.cursoId == null && cursos.size == 1) onCurso(cursos.first().id)
                else recalcular()
            }.onFailure {
                _eventos.send(FormEvento.Error("No se pudieron cargar tus cursos"))
            }
        }
        _ui.value.datos.cursoId?.let { cargarEstudiantes(it) }
    }

    // ---------------- Edición ----------------

    fun onPrivado(privado: Boolean) = actualizar { it.copy(privado = privado) }

    /** Al cambiar de curso se borra el estudiante elegido (pertenecía al curso anterior). */
    fun onCurso(cursoId: String) {
        if (cursoId == _ui.value.datos.cursoId) return
        actualizar { it.copy(cursoId = cursoId, estudianteId = null) }
        cargarEstudiantes(cursoId)
    }

    fun onEstudiante(estudianteId: String) = actualizar { it.copy(estudianteId = estudianteId) }
    fun onPrioridad(prioridad: Int) = actualizar { it.copy(prioridad = prioridad) }
    fun onTitulo(titulo: String) = actualizar { it.copy(titulo = titulo.take(120)) }
    fun onContenido(contenido: String) = actualizar { it.copy(contenido = contenido.take(MAX_CONTENIDO)) }
    fun onPermitirComentarios(permitir: Boolean) = actualizar { it.copy(permitirComentarios = permitir) }

    /** Carga los datos de un borrador u otra fuente (lo usa la Tarea 3). */
    fun cargarDatos(datos: DatosAnuncio) {
        val cursoCambia = datos.cursoId != _ui.value.datos.cursoId
        actualizar { datos }
        if (cursoCambia) datos.cursoId?.let { cargarEstudiantes(it) }
    }

    private fun cargarEstudiantes(cursoId: String) {
        cargaEstudiantes?.cancel()
        _ui.update { it.copy(cargandoEstudiantes = true, estudiantes = emptyList()) }
        cargaEstudiantes = viewModelScope.launch {
            contactRepository.getStudentsByCourse(CourseCatalogRequest(cursoId))
                .onSuccess { lista ->
                    _ui.update { it.copy(estudiantes = lista.sortedWith(compareBy(ORDEN_ES) { e -> "${e.surname} ${e.firstname}" }), cargandoEstudiantes = false) }
                    recalcular()
                }
                .onFailure {
                    _ui.update { it.copy(cargandoEstudiantes = false) }
                    _eventos.send(FormEvento.Error("No se pudo cargar la lista de estudiantes"))
                    recalcular()
                }
        }
    }

    // ---------------- Borradores (Tarea 3) ----------------

    private fun cargarBorrador(id: Long) {
        viewModelScope.launch {
            val d = draftRepository.obtener(id)
            if (d == null) {
                _eventos.send(FormEvento.Error("No se encontró el borrador"))
                return@launch
            }
            val datos = DatosAnuncio(
                privado = d.privado,
                cursoId = d.cursoId,
                estudianteId = d.estudianteId,
                prioridad = d.prioridad,
                titulo = d.titulo,
                contenido = d.contenido,
                permitirComentarios = d.permitirComentarios
            )
            _ui.update { it.copy(borradorId = d.id, datosGuardados = datos) }
            cargarDatos(datos)
        }
    }

    /** Guarda en Room (persistente) y sigue en el formulario. */
    fun guardarBorrador(yCerrar: Boolean = false) {
        val s = _ui.value
        if (s.guardandoBorrador) return
        if (s.estaVacio) {
            viewModelScope.launch {
                if (yCerrar) _eventos.send(FormEvento.Salir())
                else _eventos.send(FormEvento.Mensaje("Escribe un título o contenido antes de guardar el borrador"))
            }
            return
        }
        _ui.update { it.copy(guardandoBorrador = true, confirmandoSalida = false) }
        viewModelScope.launch {
            runCatching {
                draftRepository.guardar(
                    s.borradorId,
                    BorradorNuevo(
                        privado = s.datos.privado,
                        cursoId = s.datos.cursoId,
                        cursoCodigo = s.curso?.courseCode,
                        estudianteId = s.datos.estudianteId,
                        estudianteNombre = s.estudiante?.let { nombreCompleto(it) },
                        prioridad = s.datos.prioridad,
                        titulo = s.datos.titulo,
                        contenido = s.datos.contenido,
                        permitirComentarios = s.datos.permitirComentarios
                    )
                )
            }.onSuccess { id ->
                _ui.update { it.copy(borradorId = id, datosGuardados = s.datos, guardandoBorrador = false) }
                if (yCerrar) _eventos.send(FormEvento.Salir("Borrador guardado"))
                else _eventos.send(FormEvento.Mensaje("Borrador guardado. Lo encontrarás en «Borradores»."))
            }.onFailure {
                _ui.update { it.copy(guardandoBorrador = false) }
                _eventos.send(FormEvento.Error("No se pudo guardar el borrador"))
            }
        }
    }

    /** Atrás: si hay cambios sin guardar se pregunta qué hacer (prevención de errores). */
    fun onVolver() {
        val s = _ui.value
        if (s.hayCambios && !s.estaVacio) _ui.update { it.copy(confirmandoSalida = true) }
        else viewModelScope.launch { _eventos.send(FormEvento.Salir()) }
    }

    fun onSeguirEditando() = _ui.update { it.copy(confirmandoSalida = false) }

    fun onDescartar() {
        _ui.update { it.copy(confirmandoSalida = false) }
        viewModelScope.launch { _eventos.send(FormEvento.Salir()) }
    }

    // ---------------- Envío ----------------

    /** Nunca es un botón que "no hace nada": si falta algo, muestra los errores. */
    fun onEnviar() {
        val s = _ui.value
        if (s.enviando) return
        if (s.errores.isNotEmpty()) {
            _ui.update { it.copy(intentoEnviar = true) }
            recalcular()
            return
        }
        // Confirmación solo para privados: es donde equivocarse de destinatario es costoso.
        if (s.datos.privado) _ui.update { it.copy(confirmandoPrivado = true) } else publicar()
    }

    fun onCancelarConfirmacion() = _ui.update { it.copy(confirmandoPrivado = false) }

    fun onConfirmarPrivado() {
        _ui.update { it.copy(confirmandoPrivado = false) }
        publicar()
    }

    private fun publicar() {
        val s = _ui.value
        val curso = s.curso ?: return
        val estudiante = if (s.datos.privado) s.estudiante ?: return else null
        _ui.update { it.copy(enviando = true) }
        viewModelScope.launch {
            postRepository.publish(
                NuevoAnuncioRequest(
                    courseCatalogId = curso.id,
                    courseCode = curso.courseCode,
                    title = s.datos.titulo.trim(),
                    content = s.datos.contenido.trim(),
                    isPrivate = s.datos.privado,
                    receiverId = estudiante?.student_id,
                    receiverName = estudiante?.let { nombreCompleto(it) },
                    priority = s.datos.prioridad,
                    allowComments = s.datos.permitirComentarios
                )
            ).onSuccess { r ->
                // Un borrador publicado deja de ser borrador.
                s.borradorId?.let { runCatching { draftRepository.eliminar(it) } }
                _ui.update { it.copy(enviando = false, datosGuardados = it.datos) }
                val mensaje = if (estudiante != null) "Enviado solo a ${nombreCompleto(estudiante)}"
                else "Publicado para ${alumnos(s.estudiantes.size)} de ${curso.courseCode}"
                _eventos.send(FormEvento.Publicado(r.post_id, mensaje))
            }.onFailure {
                _ui.update { it.copy(enviando = false) }
                _eventos.send(FormEvento.Error("No se pudo publicar. Revisa tu conexión e inténtalo de nuevo."))
            }
        }
    }

    // ---------------- Derivados ----------------

    private fun actualizar(cambio: (DatosAnuncio) -> DatosAnuncio) {
        _ui.update { it.copy(datos = cambio(it.datos)) }
        recalcular()
    }

    private fun recalcular() {
        _ui.update { s ->
            val errores = validar(s)
            s.copy(errores = errores, resumen = resumen(s, errores), textoBoton = textoBoton(s))
        }
    }

    private fun validar(s: FormUiState): Map<Campo, String> = buildMap {
        if (s.datos.cursoId == null) put(Campo.CURSO, "Elige el curso")
        if (s.datos.privado && s.datos.estudianteId == null) put(Campo.ESTUDIANTE, "Selecciona el estudiante que recibirá el anuncio")
        if (s.datos.titulo.isBlank()) put(Campo.TITULO, "El título es obligatorio")
        if (s.datos.contenido.isBlank()) put(Campo.CONTENIDO, "Escribe el contenido del anuncio")
    }

    private fun resumen(s: FormUiState, errores: Map<Campo, String>): ResumenDestinatario {
        val curso = s.curso
        val estudiante = s.estudiante
        if (s.intentoEnviar && errores.isNotEmpty()) {
            val falta = if (Campo.CURSO in errores || Campo.ESTUDIANTE in errores) "Falta elegir el destinatario" else "Faltan datos del anuncio"
            val n = errores.size
            return ResumenDestinatario(
                falta,
                "Completa ${if (n == 1) "el campo marcado" else "los $n campos marcados"} para poder ${if (s.datos.privado) "enviar" else "publicar"}.",
                ResumenDestinatario.Tono.ERROR
            )
        }
        return when {
            curso == null -> ResumenDestinatario(
                "Elige un curso", "Así verás quién recibirá el anuncio.", ResumenDestinatario.Tono.NEUTRO
            )
            s.datos.privado && estudiante != null -> ResumenDestinatario(
                "Lo recibirá solo: ${nombreCompleto(estudiante)}",
                "Ningún otro estudiante del curso lo verá.",
                ResumenDestinatario.Tono.PRIVADO
            )
            s.datos.privado -> ResumenDestinatario(
                "Elige el estudiante",
                "Un anuncio privado lo recibe un solo estudiante de ${curso.courseCode}.",
                ResumenDestinatario.Tono.PRIVADO
            )
            else -> ResumenDestinatario(
                if (s.cargandoEstudiantes) "Lo recibirán los estudiantes de ${curso.courseCode}"
                else "Lo recibirán los ${alumnos(s.estudiantes.size)} de ${curso.courseCode}",
                "Todos los matriculados lo verán en su muro.",
                ResumenDestinatario.Tono.PUBLICO
            )
        }
    }

    private fun textoBoton(s: FormUiState): String {
        val estudiante = s.estudiante
        return when {
            s.datos.privado && estudiante != null -> "Enviar a ${nombreCorto(estudiante)}"
            s.datos.privado -> "Enviar anuncio privado"
            s.curso != null && !s.cargandoEstudiantes && s.estudiantes.isNotEmpty() -> "Publicar para ${alumnos(s.estudiantes.size)}"
            else -> "Publicar"
        }
    }

    companion object {
        const val ARG_CURSO = "cursoId"
        const val ARG_ESTUDIANTE = "estudianteId"
        const val ARG_BORRADOR = "borradorId"

        /** Orden alfabético en español (Á junto a A, Ñ después de N). */
        val ORDEN_ES: java.text.Collator = java.text.Collator.getInstance(java.util.Locale("es", "PE"))

        fun nombreCompleto(e: StudentEntity) = "${e.firstname} ${e.surname}".trim()
        fun nombreCorto(e: StudentEntity) = "${e.firstname} ${e.surname.substringBefore(' ')}".trim()
        fun alumnos(n: Int) = if (n == 1) "1 estudiante" else "$n estudiantes"
    }
}
