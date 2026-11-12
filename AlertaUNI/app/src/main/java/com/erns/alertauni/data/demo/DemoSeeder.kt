package com.erns.alertauni.data.demo

import com.erns.alertauni.common.FechaBackend
import com.erns.alertauni.data.model.Prioridad.ALTA
import com.erns.alertauni.data.model.Prioridad.BAJA
import com.erns.alertauni.data.model.Prioridad.MEDIA
import java.time.ZonedDateTime

/**
 * SIMULADO: 40 anuncios de prueba para la Tarea 4 (pruebas de funcionamiento).
 *
 * Cubren públicos y privados, las tres prioridades, fechas de hoy, de esta semana y
 * anteriores, títulos y contenidos cortos y largos, y casos borde (tildes, mayúsculas,
 * signos ¿?¡!, símbolos & % # +). Incluye «Práctica calificada 2» para la tarea de búsqueda.
 * Las fechas se calculan respecto al momento de la primera ejecución.
 */
object DemoSeeder {

    private data class Semilla(
        val dias: Long,          // días atrás respecto a hoy
        val hora: Int,
        val minuto: Int,
        val curso: Int,          // índice en DemoData.cursos
        val prioridad: Int,
        val titulo: String,
        val contenido: String,
        val alumno: String? = null, // nombre del estudiante si es privado
        val comentarios: Int = 0,
        val permiteComentarios: Boolean = true,
        val archivadoHaceDias: Long? = null
    )

    private val semillas = listOf(
        // --- HOY ---
        Semilla(0, 9, 15, 0, ALTA, "Práctica calificada 2",
            "La práctica será el jueves 4 de junio en el laboratorio 3. Traer calculadora y DNI.", comentarios = 5),
        Semilla(0, 8, 2, 0, MEDIA, "Inasistencia a la práctica",
            "Juan, no asististe a la práctica del jueves. ¿Podrías indicarme el motivo?", alumno = "Juan", comentarios = 1),
        Semilla(0, 7, 40, 1, BAJA, "Material de la semana 6",
            "Ya está disponible en el aula virtual el material sobre Jetpack Compose y navegación."),
        Semilla(0, 11, 30, 2, ALTA, "CAMBIO DE HORARIO: EXAMEN PARCIAL",
            "El examen parcial se adelanta al lunes a las 08:00. Revisen el temario publicado.", comentarios = 3),
        Semilla(0, 12, 5, 1, MEDIA, "Entrega del avance del proyecto",
            "Recuerden subir el avance en PDF antes de las 23:59. No se aceptan entregas por correo."),
        Semilla(0, 6, 50, 0, BAJA, "Felicitaciones",
            "¡Excelente trabajo en la exposición, Rosa! Tu demostración fue muy clara.", alumno = "Rosa"),

        // --- ESTA SEMANA ---
        Semilla(1, 18, 20, 0, MEDIA, "Cambio de aula: clase del lunes",
            "La clase del lunes 8 será en el aula 204 del pabellón de Sistemas.", comentarios = 2),
        Semilla(1, 10, 0, 2, ALTA, "Laboratorio cerrado por mantenimiento",
            "El laboratorio 2 estará cerrado hasta el viernes. Las prácticas se harán en el laboratorio 5."),
        Semilla(2, 16, 45, 1, BAJA, "Lectura recomendada",
            "Lean el capítulo 3 de «Clean Architecture» antes de la próxima clase."),
        Semilla(2, 9, 30, 0, ALTA, "Nota del examen corregida",
            "Tu nota del examen parcial fue corregida a 16. Revisa el sistema académico.", alumno = "Luis"),
        Semilla(3, 14, 10, 2, MEDIA, "Consultas sobre normalización",
            "Habrá una sesión de consultas sobre 1FN, 2FN y 3FN el miércoles a las 16:00.", comentarios = 4),
        Semilla(3, 20, 5, 1, MEDIA, "C++ & Kotlin: ¿100% listos para el reto #3?",
            "El reto #3 combina C++ y Kotlin. Lean el enunciado y traigan sus dudas (incluidas las de punteros & memoria)."),
        Semilla(4, 8, 0, 0, BAJA, "Encuesta de satisfacción",
            "Completen la encuesta anónima del curso; toma menos de 5 minutos."),
        Semilla(4, 17, 30, 2, ALTA, "Justificación pendiente",
            "Ana, falta tu justificación de la inasistencia del 20 de mayo. Envíala esta semana.", alumno = "Ana"),
        Semilla(5, 11, 15, 1, MEDIA, "Grupos para el proyecto final",
            "Ya se publicaron los grupos del proyecto final. Revisen si su nombre aparece correctamente.", comentarios = 6),
        Semilla(6, 9, 45, 0, MEDIA, "Asesoría individual",
            "Luis, puedo atenderte el martes a las 15:00 para revisar tu avance.", alumno = "Luis", comentarios = 2),

        // --- ANTERIORES ---
        Semilla(8, 10, 0, 0, ALTA, "Examen sustitutorio",
            "El examen sustitutorio será el 15 de junio. Solo pueden rendirlo quienes tengan nota menor a 10 en un parcial."),
        Semilla(9, 15, 20, 1, BAJA, "Video de la clase grabada",
            "Subí el video de la clase del jueves al canal del curso."),
        Semilla(10, 8, 30, 2, MEDIA, "Modelo entidad-relación",
            "Corrijan el modelo E-R según las observaciones hechas en clase y vuelvan a subirlo."),
        Semilla(11, 19, 0, 0, MEDIA,
            "Instrucciones detalladas para la entrega final del proyecto integrador del curso de Nuevas Plataformas",
            "La entrega final incluye el código fuente en GitHub, un README con capturas, el informe en PDF y la demostración presencial. " +
                "Cada integrante debe poder explicar cualquier parte del código. Las entregas tardías tendrán una penalidad del 20 % por día. " +
                "Revisen la rúbrica publicada.", comentarios = 8),
        Semilla(12, 7, 55, 1, ALTA, "Suspensión de clases",
            "Por el feriado regional no habrá clases el viernes. La clase se recuperará el sábado a las 09:00."),
        Semilla(13, 13, 40, 2, BAJA, "Recordatorio",
            "Recuerden traer sus laptops cargadas.", alumno = "Diego"),
        Semilla(15, 16, 0, 0, MEDIA, "Tareas",
            "Resolver los ejercicios de la página 12 del libro."),
        Semilla(16, 10, 10, 1, BAJA, "Bibliografía actualizada",
            "Se agregó bibliografía sobre arquitectura MVVM y patrones de repositorio."),
        Semilla(18, 12, 0, 2, ALTA, "Ejercicios de SQL",
            "Practiquen JOIN, GROUP BY y subconsultas; vendrán en la práctica calificada.", comentarios = 3),
        Semilla(19, 9, 5, 0, MEDIA, "Revisión de la práctica 1",
            "Pueden revisar su práctica 1 en mi oficina el lunes de 10:00 a 12:00."),
        Semilla(21, 18, 30, 1, BAJA, "Charla de egresados",
            "Este jueves habrá una charla de egresados sobre desarrollo móvil. Asistencia opcional."),
        Semilla(22, 8, 15, 2, MEDIA, "Respaldo de la base de datos",
            "Carlos, tu respaldo de la base de datos llegó incompleto. Vuelve a enviarlo.", alumno = "Carlos"),
        Semilla(24, 11, 45, 0, ALTA, "Plagio detectado",
            "Se detectaron trabajos idénticos en la tarea 2. Los involucrados deben presentarse el lunes.", permiteComentarios = false, comentarios = 2),
        Semilla(26, 14, 0, 1, MEDIA, "Diagrama de clases",
            "El diagrama de clases debe incluir las relaciones de herencia y composición."),
        Semilla(28, 17, 25, 2, BAJA, "Horario de consultas",
            "Las consultas serán los miércoles de 16:00 a 17:00 en la oficina 305.", comentarios = 2, archivadoHaceDias = 2),
        Semilla(30, 9, 0, 0, MEDIA, "Justificación aceptada",
            "Rosa, tu justificación por la inasistencia del 20 de mayo fue aceptada.", alumno = "Rosa", comentarios = 1, archivadoHaceDias = 12),
        Semilla(33, 10, 30, 1, ALTA, "Primera práctica calificada",
            "La primera práctica calificada será el martes. Temas: requisitos y casos de uso."),
        Semilla(35, 15, 15, 2, MEDIA, "Instalación de PostgreSQL",
            "Instalen PostgreSQL 16 y pgAdmin antes de la clase del jueves."),
        Semilla(38, 8, 45, 0, BAJA, "Bienvenida",
            "¡Bienvenidos al curso de Nuevas Plataformas! Revisen el sílabo en el aula virtual.", comentarios = 4),
        Semilla(40, 12, 20, 1, BAJA, "Sílabo",
            "El sílabo actualizado ya está disponible."),
        Semilla(42, 16, 50, 2, MEDIA, "Grupo de laboratorio",
            "María, fuiste asignada al grupo B de laboratorio.", alumno = "María"),
        Semilla(45, 9, 10, 0, MEDIA, "Configurar Android Studio",
            "Instalen Android Studio y creen un emulador con API 34 antes de la primera práctica."),
        Semilla(50, 13, 0, 1, ALTA, "Matrícula en el aula virtual",
            "Quienes no estén matriculados en el aula virtual deben escribirme hoy mismo."),
        Semilla(55, 10, 0, 2, BAJA, "Presentación del curso",
            "Primera clase: presentación del curso, evaluación y cronograma.")
    )

    private val autoresComentario = listOf(
        "Juan Pérez Quispe" to "juan.0@demo.unsa.edu.pe",
        "Ana Torres Vilca" to "ana.3@demo.unsa.edu.pe",
        "Luis Choque Huanca" to "luis.2@demo.unsa.edu.pe"
    )
    private val textosComentario = listOf(
        "Profesor, ¿la práctica es individual?",
        "¿Podemos usar calculadora científica?",
        "Gracias por la información.",
        "¿Habrá material de repaso?",
        "Entendido, profesor."
    )

    /** Inserta los anuncios de prueba si la tabla está vacía. */
    suspend fun sembrarSiVacio(dao: DemoDao, autor: String) {
        if (dao.contarPosts() > 0) return
        val ahora = ZonedDateTime.now(FechaBackend.ZONA_LOCAL)
        semillas.forEach { s ->
            val fecha = ahora.minusDays(s.dias).withHour(s.hora).withMinute(s.minuto).withSecond(0)
            val curso = DemoData.cursos[s.curso]
            val alumno = s.alumno?.let { nombre -> DemoData.estudiantes(curso.id).firstOrNull { it.firstname == nombre } }
            val id = dao.insertPost(
                DemoPostEntity(
                    courseCatalogId = curso.id,
                    courseCode = curso.codigo,
                    courseName = curso.nombre,
                    title = s.titulo,
                    content = s.contenido,
                    isPrivate = alumno != null,
                    receiverId = alumno?.student_id,
                    receiverName = alumno?.let { "${it.firstname} ${it.surname}" },
                    priority = s.prioridad,
                    allowComments = s.permiteComentarios,
                    archivedAt = s.archivadoHaceDias?.let { FechaBackend.format(ahora.minusDays(it)) },
                    published = FechaBackend.format(fecha),
                    authorName = autor
                )
            ).toInt()
            dao.insertComments((0 until s.comentarios).map { k ->
                val (nombre, email) = autoresComentario[k % autoresComentario.size]
                DemoCommentEntity(
                    postId = id,
                    authorName = nombre,
                    email = email,
                    content = textosComentario[k % textosComentario.size],
                    published = FechaBackend.format(fecha.plusMinutes(6L + k * 9))
                )
            })
        }
    }
}
