package com.erns.alertauni.navigation

import androidx.compose.foundation.layout.padding
import android.app.Activity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.erns.alertauni.ui.theme.MyPrimaryColor
import com.erns.alertauni.ui.theme.MyPrimaryForegroundColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.erns.alertauni.screen.contact.StudentContactScreen
import com.erns.alertauni.screen.contact.TeacherContactScreen
import com.erns.alertauni.screen.course.StudentCourseScreen
import com.erns.alertauni.screen.login.LoginScreen
import com.erns.alertauni.screen.login.ProfileScreen
import com.erns.alertauni.screen.post.PostCommentScreen
import com.erns.alertauni.screen.post.PostScreen
import com.erns.alertauni.screen.post.PostViewModel
import com.erns.alertauni.screen.post.nuevo.NuevoAnuncioScreen
import com.erns.alertauni.screen.post.nuevo.NuevoAnuncioViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun MainAppNavigation(navController: NavHostController = rememberNavController()) {
    val userTypeSelected = remember { mutableStateOf("") }
    NavHost(
        navController = navController,
        startDestination = RouteScreen.Login.route
    ) {
        composable(RouteScreen.Login.route) {
            LoginScreen(
                onIncompleteProfile = {
                    navController.navigate(RouteScreen.Profile.route) {
                        popUpTo(RouteScreen.Login.route) {
                            inclusive = false
                        }
                    }
                },
                onLoginSuccess = {userType ->
                    userTypeSelected.value = userType
                }
            )
        }

        composable(RouteScreen.Profile.route) {
            ProfileScreen(
                onCompleteProfile = { userType ->
                    userTypeSelected.value = userType
                }
            )
        }

    }

    if(userTypeSelected.value.isNotEmpty()){
        MainMenuScreen(userTypeSelected.value)
    }
}

private const val RESULTADO_POST_ID = "publicado_post_id"
private const val RESULTADO_MENSAJE = "publicado_mensaje"

@Composable
fun MainMenuScreen(userType: String) {
    val items = mutableListOf<RouteMainMenu>()
    val fabAction = remember { mutableStateOf<() -> Unit>({}) }
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry.value?.destination?.route

    if (userType == "PROFESSOR") {
        items.addAll(
            listOf(
                RouteMainMenu.Posts,
                RouteMainMenu.TeacherContacts,
                RouteMainMenu.Courses
            )
        )
    } else if (userType == "STUDENT") {
        items.addAll(
            listOf(
                RouteMainMenu.Posts,
                RouteMainMenu.StudentContacts,
                RouteMainMenu.Courses
            )
        )
    }

    // Texto e íconos claros sobre la cabecera azul.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    Scaffold(
        // Las cabeceras gestionan la barra de estado; la NavigationBar, la de navegación.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (currentDestination == RouteScreen.Posts.route) {
                // FAB extendido con etiqueta: la acción es explícita (antes era un "+" circular).
                ExtendedFloatingActionButton(
                    onClick = fabAction.value,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nuevo anuncio") },
                    containerColor = MyPrimaryColor,
                    contentColor = MyPrimaryForegroundColor,
                    shape = RoundedCornerShape(16.dp)
                )
            } else if (currentDestination == RouteMainMenu.Courses.route) {
                FloatingActionButton(
                    shape = CircleShape,
                    onClick = fabAction.value
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                    )
                }
            }
        },
        bottomBar = {
            // La barra inferior solo se ve en las pestañas principales; el formulario y
            // los comentarios son pantallas de detalle con flecha atrás (como en Figma).
            if (items.any { it.route == currentDestination }) NavigationBar {
                val currentDestination =
                    navController.currentBackStackEntryAsState().value?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentDestination?.route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RouteMainMenu.Posts.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(RouteMainMenu.Posts.route) { entry ->
                val postViewModel: PostViewModel = hiltViewModel()
                // Resultado del formulario: recarga, resalta el anuncio nuevo y confirma (mq23).
                val publicado by entry.savedStateHandle
                    .getStateFlow<Int?>(RESULTADO_POST_ID, null).collectAsState()
                // Al volver de Comentarios o de otra pestaña se refrescan contadores y estados.
                LaunchedEffect(Unit) { postViewModel.recargarSiHayDatos() }
                LaunchedEffect(publicado) {
                    publicado?.let { id ->
                        postViewModel.onPublicado(id, entry.savedStateHandle[RESULTADO_MENSAJE] ?: "Anuncio publicado")
                        entry.savedStateHandle[RESULTADO_POST_ID] = null
                    }
                }
                PostScreen(
                    onClickComment = { postId ->
                        navController.navigate(RouteScreen.Comment.route + "/$postId")
                    },
                    snackbarHostState = snackbarHostState,
                    onFabActionReady = { fabAction.value = it },
                    onNuevoAnuncio = { navController.navigate(RouteScreen.NuevoAnuncio.crear()) },
                    viewModel = postViewModel
                )
            }
            composable(
                route = RouteScreen.NuevoAnuncio.route + RouteScreen.NuevoAnuncio.ARGS,
                arguments = listOf(
                    navArgument(NuevoAnuncioViewModel.ARG_CURSO) { type = NavType.StringType; defaultValue = "" },
                    navArgument(NuevoAnuncioViewModel.ARG_ESTUDIANTE) { type = NavType.StringType; defaultValue = "" }
                )
            ) {
                NuevoAnuncioScreen(
                    onCerrar = { navController.popBackStack() },
                    onPublicado = { id, mensaje ->
                        // El resultado vuelve al muro aunque se haya abierto desde Contactos.
                        navController.getBackStackEntry(RouteMainMenu.Posts.route).savedStateHandle.apply {
                            set(RESULTADO_MENSAJE, mensaje)
                            set(RESULTADO_POST_ID, id)
                        }
                        navController.navigate(RouteMainMenu.Posts.route) {
                            popUpTo(RouteMainMenu.Posts.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = RouteScreen.Comment.route + "/{postId}",
                arguments = listOf(
                    navArgument("postId") {
                        type = NavType.StringType
                    }
                )
            ) {
                PostCommentScreen(onVolver = { navController.popBackStack() })
            }

            composable(RouteMainMenu.Courses.route) {
                StudentCourseScreen(
                    snackbarHostState = snackbarHostState,
                    onFabActionReady = { fabAction.value = it }
                )
            }

            composable(RouteMainMenu.TeacherContacts.route) {
                TeacherContactScreen(
                    onNuevoPrivado = { cursoId, estudianteId ->
                        navController.navigate(RouteScreen.NuevoAnuncio.crear(cursoId, estudianteId))
                    }
                )
            }
            composable(RouteMainMenu.StudentContacts.route) {
                StudentContactScreen()
            }

        }
    }
}

