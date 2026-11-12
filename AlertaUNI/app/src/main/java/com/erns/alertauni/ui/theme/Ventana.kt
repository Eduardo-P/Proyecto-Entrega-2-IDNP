package com.erns.alertauni.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Altura por debajo de la cual la ventana se considera compacta (criterio de Material Design:
 * menos de 480 dp). Es el caso de un teléfono en horizontal.
 */
const val ALTURA_COMPACTA_DP = 480

/**
 * true cuando falta altura (teléfono en horizontal). En ese caso la navegación pasa a un riel
 * lateral y la cabecera del muro se oculta al desplazar, para dejar sitio a los anuncios
 * (incidencia PT-01 de las pruebas de tamaños de pantalla). En vertical no cambia nada.
 */
@Composable
fun esAlturaCompacta(): Boolean = LocalConfiguration.current.screenHeightDp < ALTURA_COMPACTA_DP
