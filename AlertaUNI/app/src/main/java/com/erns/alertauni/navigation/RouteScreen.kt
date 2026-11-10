package com.erns.alertauni.navigation

sealed class RouteScreen(val route: String) {
    object Home : RouteScreen("home")
    object MainMenuScreen : RouteScreen("MainScreen")
    object Login : RouteScreen("login")
    object Attendance : RouteScreen("attendance")
    object Participation : RouteScreen("participation")
    object Posts : RouteScreen("posts")
    object Chat : RouteScreen("chat")
    object Profile : RouteScreen("profile")
    object Comment : RouteScreen("comment")
    object ContactComment : RouteScreen("ContactComment")
    /** Formulario único; argumentos opcionales para el atajo desde Contactos. */
    object NuevoAnuncio : RouteScreen("nuevo_anuncio") {
        const val ARGS = "?cursoId={cursoId}&estudianteId={estudianteId}"
        fun crear(cursoId: String? = null, estudianteId: String? = null) =
            route + "?cursoId=${cursoId ?: ""}&estudianteId=${estudianteId ?: ""}"
    }
}
