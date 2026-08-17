package com.pranshulgg.watchmaster.core.ui.components.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.ui.theme.AppMotion
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.theme.GoogleFlexBoldRounded
import com.pranshulgg.watchmaster.core.ui.theme.ShapeRadius

/** El alto y el alpha del plegado, atados a la misma duración y la misma curva. */
private val FoldSizeSpec =
    tween<IntSize>(AppMotion.DurationMedium, easing = AppMotion.EmphasizedDecelerate)
private val FoldFadeSpec =
    tween<Float>(AppMotion.DurationMedium, easing = AppMotion.EmphasizedDecelerate)

@OptIn(ExperimentalTextApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaSectionCard(
    title: String,
    titleIcon: Int,
    showAction: Boolean = false,
    actionText: String = "",
    actionOnClick: () -> Unit = {},
    noPadding: Boolean = false,
    trailingContent: @Composable () -> Unit = {},
    collapsed: Boolean? = null,
    onToggleCollapsed: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    // `collapsed` a null es "esta sección no se pliega", que es el caso de casi todas. Un Boolean
    // con `false` por defecto no distinguiría entre no plegarse y estar desplegada, y le pondría
    // un chevrón inútil a cada tarjeta de la aplicación.
    val collapsible = collapsed != null
    val isCollapsed = collapsed == true

    val chevronRotation by animateFloatAsState(
        targetValue = if (isCollapsed) 0f else 180f,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "section-card-chevron",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (noPadding) 0.dp else 16.dp),
        shape = RoundedCornerShape(ShapeRadius.ExtraLarge),
    ) {
        // Todo lo que aparece y desaparece vive dentro del bloque que se anima: el relleno de
        // abajo es constante y la separación con el encabezado la pone el propio contenido. Con
        // el relleno cambiando de golpe de 16 a 8, la tarjeta pegaba un salto justo al terminar
        // de plegarse, que es lo que se veía como un trozo perdido al final.
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    5.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .then(
                        if (collapsible) {
                            Modifier.clickable(onClick = onToggleCollapsed)
                        } else {
                            Modifier
                        }
                    )
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Symbol(
                    titleIcon,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = GoogleFlexBoldRounded,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (showAction) {
                    Spacer(Modifier.weight(1f))
                    if (actionText != "") {
                        HeaderAction(text = actionText, onClick = { actionOnClick() })
                    }
                }

                if (trailingContent != {} && !showAction) {
                    Spacer(Modifier.weight(1f))
                    trailingContent()
                }

                if (collapsible) {
                    Symbol(
                        R.drawable.keyboard_arrow_down_24px,
                        desc = if (isCollapsed) {
                            localized("Desplegar $title", "Expand $title")
                        } else {
                            localized("Plegar $title", "Collapse $title")
                        },
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.rotate(chevronRotation),
                    )
                }
            }

            AnimatedVisibility(
                visible = !isCollapsed,
                // El tamaño y el desvanecido comparten reloj a propósito. Con las specs de
                // Material —espacial para el alto, de efectos para el alpha— el contenido se
                // desvanecía bastante antes de que la caja terminara de encogerse, y quedaba un
                // hueco vacío bajo el encabezado que lo hacía parecer más alto de lo que es;
                // luego el hueco se cerraba de una vez y todo lo de abajo daba un tirón.
                enter = expandVertically(FoldSizeSpec) + fadeIn(FoldFadeSpec),
                exit = shrinkVertically(FoldSizeSpec) + fadeOut(FoldFadeSpec),
            ) {
                // La columna no es envoltorio de más. `content` puede emitir varios hijos, y
                // antes eran hijos directos de la columna de la tarjeta, que los apilaba con su
                // separación. Dentro de AnimatedVisibility, que coloca como una caja, pasarían a
                // dibujarse unos encima de otros: aquí recupera el apilado y la separación.
                Column(
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    content()
                }
            }
        }
    }
}


@Composable
private fun HeaderAction(text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(
                onClick = {
                    onClick()
                }
            )
            .padding(end = 5.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
