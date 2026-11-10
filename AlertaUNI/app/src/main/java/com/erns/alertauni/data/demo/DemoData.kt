package com.erns.alertauni.data.demo

import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.StudentEntity
import com.erns.alertauni.data.model.StudentEnrollment

/**
 * SIMULADO: cursos, estudiantes y cuentas de prueba del modo demo.
 * Los nombres son ficticios; sirven para probar la interfaz sin el backend.
 */
object DemoData {

    data class CuentaDemo(val email: String, val nombre: String, val apellido: String)

    /** Dos cuentas de docente para comprobar que los borradores se aíslan por cuenta. */
    val CUENTA_A = CuentaDemo("docente.a@demo.alertauni.pe", "Docente", "Demo A")
    val CUENTA_B = CuentaDemo("docente.b@demo.alertauni.pe", "Docente", "Demo B")

    data class CursoDemo(val id: String, val codigo: String, val nombre: String, val alumnos: Int)

    val cursos = listOf(
        CursoDemo("demo-cc-inf01", "INF-01", "Nuevas Plataformas", 32),
        CursoDemo("demo-cc-inf02", "INF-02", "Ingeniería de Software", 28),
        CursoDemo("demo-cc-inf03", "INF-03", "Bases de Datos", 25)
    )

    fun curso(id: String): CursoDemo? = cursos.firstOrNull { it.id == id }

    private val nombres = listOf(
        "Juan", "Rosa", "Luis", "Ana", "Carlos", "María", "José", "Lucía", "Diego", "Valeria",
        "Miguel", "Camila", "Jorge", "Sofía", "Renzo", "Fiorella", "Kevin", "Andrea", "Bruno",
        "Gabriela", "Iván", "Daniela", "Óscar", "Paola", "Sebastián", "Ximena", "Álvaro", "Noelia"
    )
    private val apellidos = listOf(
        "Pérez Quispe", "Apaza Mamani", "Choque Huanca", "Torres Vilca", "Quispe Flores",
        "Condori Ramos", "Huamán Ccama", "Mendoza Ríos", "Salas Núñez", "Vargas Llosa",
        "Zúñiga Paz", "Cáceres León", "Ticona Arias", "Gutiérrez Soto", "Medina Cruz",
        "Ramírez Ortiz", "Castillo Díaz", "Rojas Málaga", "Chávez Tito", "Paredes Lazo"
    )

    /** Estudiantes matriculados en un curso (lista fija y reproducible). */
    fun estudiantes(cursoId: String): List<StudentEntity> {
        val curso = curso(cursoId) ?: return emptyList()
        val desplazamiento = cursos.indexOf(curso) * 7
        return (0 until curso.alumnos).map { i ->
            val nombre = nombres[(i + desplazamiento) % nombres.size]
            val apellido = apellidos[(i * 3 + desplazamiento) % apellidos.size]
            val codigo = "2021" + (120 + i * 7 + desplazamiento).toString().padStart(4, '0')
            StudentEntity(
                course_catalog_id = curso.id,
                student_id = "${curso.id}-alumno-$i",
                codigo = codigo,
                firstname = nombre,
                surname = apellido,
                email = "${com.erns.alertauni.domain.muro.TextoBusqueda.normalizar(nombre)}.$i@demo.unsa.edu.pe"
            )
        }
    }

    fun catalogo(): List<CatalogCourseEntity> =
        cursos.map { CatalogCourseEntity(id = it.id, courseCode = it.codigo, courseName = it.nombre) }

    fun matriculas(cuenta: CuentaDemo?): List<StudentEnrollment> = cursos.map {
        StudentEnrollment(
            course_catalog_id = it.id,
            courseId = it.id,
            courseCode = it.codigo,
            courseName = it.nombre,
            semester = "2026-B",
            courseType = "Teoría",
            groupType = "A",
            firstname = cuenta?.nombre ?: "Docente",
            surname = cuenta?.apellido ?: "Demo",
            email = cuenta?.email ?: ""
        )
    }
}
