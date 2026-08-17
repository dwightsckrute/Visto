package com.pranshulgg.watchmaster.feature.setting.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.core.net.toUri
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.theme.ShapeRadius
import com.pranshulgg.watchmaster.core.ui.theme.Spacing

private const val TMDB_API_SETTINGS = "https://www.themoviedb.org/settings/api"

/**
 * Explica cómo conseguir una clave de TMDB propia.
 *
 * Va plegada por defecto: quien no vaya a usar clave propia no necesita leer nada, y quien sí,
 * tiene los pasos donde se pega la clave en vez de tener que buscarlos fuera.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TmdbApiKeyHelpCard(modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "tmdb-help-chevron",
    )

    val title = localized("¿Cómo consigo una clave?", "How do I get a key?")

    val steps = listOf(
        localized(
            "Crea una cuenta gratuita en themoviedb.org.",
            "Create a free account at themoviedb.org.",
        ),
        localized(
            "Entra en Ajustes de tu perfil y abre la sección API.",
            "Open your profile settings and go to the API section.",
        ),
        localized(
            "Solicita una clave de tipo Developer. El uso personal se aprueba al momento.",
            "Request a Developer key. Personal use is approved straight away.",
        ),
        localized(
            "Copia el valor \"API Key (v3 auth)\": es una cadena larga de letras y números.",
            "Copy the \"API Key (v3 auth)\" value: a long string of letters and numbers.",
        ),
        localized(
            "Pégalo aquí arriba. Visto la usará a partir de ese momento, sin reiniciar.",
            "Paste it above. Visto starts using it right away, no restart needed.",
        ),
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShapeRadius.Large),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .heightIn(min = Spacing.minTouchTarget)
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Symbol(
                    icon = R.drawable.info_24px,
                    desc = null,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Symbol(
                    icon = R.drawable.keyboard_arrow_down_24px,
                    desc = null,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevronRotation),
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(motionScheme.defaultSpatialSpec()),
                exit = shrinkVertically(motionScheme.defaultSpatialSpec()),
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = Spacing.lg,
                        end = Spacing.lg,
                        bottom = Spacing.lg,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    steps.forEachIndexed { index, step ->
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            // El número se lee junto al texto en una sola frase; leerlo aparte
                            // no aportaría nada a quien use lector de pantalla.
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clearAndSetSemantics { },
                            )
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clearAndSetSemantics {
                                    contentDescription = "${index + 1}. $step"
                                },
                            )
                        }
                    }

                    FilledTonalButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = Spacing.minTouchTarget)
                            .padding(top = Spacing.sm),
                        shapes = ButtonDefaults.shapes(),
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, TMDB_API_SETTINGS.toUri())
                                )
                            }
                        },
                    ) {
                        Text(localized("Abrir TMDB", "Open TMDB"))
                    }
                }
            }
        }
    }
}
