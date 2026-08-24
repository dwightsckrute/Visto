package com.dwightsckrute.visto.core.ui.components

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable

/**
 * Formas de botón que no cambian al pulsar.
 *
 * Un botón de Material 3 Expressive se achata mientras lo tienes pulsado y recupera su forma al
 * soltar. Es un detalle bonito y aquí, en algunos sitios, es un fallo: si el botón deja de estar
 * habilitado **entre el toque y el levantar el dedo**, el aviso de "soltado" no llega nunca y la
 * animación se queda donde estaba. El botón se ve cuadrado hasta que algo lo recompone.
 *
 * Pasa donde `enabled` depende de algo que puede cambiar solo: temporadas que llegan por red,
 * una nota que sube al arrastrar. Por eso aparecía al añadir una serie y no al añadir una
 * película —una película no tiene nada que esperar— y por eso no pasaba siempre.
 *
 * Con las dos formas iguales la animación no tiene a dónde quedarse atascada. Se pierde el
 * achatado en esos botones concretos, que es un precio pequeño por que ninguno se quede cuadrado.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun steadyButtonShapes(): ButtonShapes = ButtonDefaults.shapes(
    shape = ButtonDefaults.shape,
    pressedShape = ButtonDefaults.shape,
)
