package com.dwightsckrute.visto.core.ui.components.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalSlider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwightsckrute.visto.core.ui.theme.GoogleFlexBoldRounded
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.localization.localized
import kotlin.math.roundToInt


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalTextApi::class
)
@Composable
fun RateMediaDialogContent(
    onConfirm: (Double) -> Unit,
    onCancel: () -> Unit,
    updateRating: Boolean = false,
    isBook: Boolean = false,
    /** Si ya está terminado, el botón solo guarda la nota: no hay nada que dar por terminado. */
    alreadyFinished: Boolean = false,
    originalRating: Float = 0f,
) {

    val sliderState = remember {
        SliderState(
            value = if (updateRating) originalRating else 0f,
            valueRange = 0f..10f,
            steps = 19,
        )
    }
    val rating = (sliderState.value * 10).roundToInt() / 10f

    val displayRating =
        if (rating == 10f) "10" else "%.1f".format(rating)


    Row(
        modifier = Modifier
            .height(250.dp)
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {


        Column {
            Text(
                displayRating, fontSize = 100.sp,
                fontFamily = GoogleFlexBoldRounded,
                color = MaterialTheme.colorScheme.secondary
            )
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .width(150.dp)
                    .background(color = MaterialTheme.colorScheme.outlineVariant),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "10",
                fontSize = 34.sp,
                fontFamily = GoogleFlexBoldRounded,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }
        VerticalSlider(
            state = sliderState,
            track = {
                SliderDefaults.Track(
                    sliderState = sliderState,

                    modifier =
                        Modifier
                            .width(36.dp)
                            .drawWithContent {
                                drawContent()
                            },
                    trackCornerSize = ShapeRadius.Medium,
                )
            },
            reverseDirection = true
        )

    }


    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 12.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Button(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp),
            onClick = {
                onCancel()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shapes = ButtonDefaults.shapes()
        ) {
            Text(
                localized("Cancelar", "Cancel"),
                color = MaterialTheme.colorScheme.onErrorContainer,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Button(
            enabled = rating > 0f,
            modifier = Modifier
                .weight(1.6f)
                .heightIn(min = 48.dp),
            onClick = {
                onCancel()
                onConfirm(rating.toDouble())
            },
            shapes = ButtonDefaults.shapes()
        ) {
            Text(
                when {
                    updateRating -> localized("Actualizar", "Update")
                    // Valorar algo que ya está terminado: ofrecer "marcar terminada" era
                    // ofrecer lo que ya pasó, y encima no cabía en el botón.
                    alreadyFinished -> localized("Guardar", "Save")
                    isBook -> localized("Marcar leído", "Mark as read")
                    else -> localized("Marcar terminada", "Mark as finished")
                },
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }

    }
}
