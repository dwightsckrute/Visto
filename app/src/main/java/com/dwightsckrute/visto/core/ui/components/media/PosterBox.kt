package com.dwightsckrute.visto.core.ui.components.media

import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PosterBox(
    posterUrl: String?,
    /** Para saber si el catálogo dio una imagen antes de intentar cargarla. */
    apiPath: String?,
    modifier: Modifier = Modifier,
    width: Dp = 80.dp,
    height: Dp = 120.dp,
    fillMaxWidth: Boolean = false,
    cornerRadius: Dp = ShapeRadius.Medium,
    circular: Boolean = false,
    placeholder: @Composable () -> Unit = { PosterPlaceholder() },
    progressIndicatorSize: Dp = 24.dp,
    shape: Shape? = null,
) {
    Box(
        modifier = modifier.then(
            if (fillMaxWidth)
                Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(
                        shape ?: RoundedCornerShape(
                            if (circular) ShapeRadius.Full else cornerRadius
                        )
                    )
            else Modifier
                .size(width = width, height = height)
                .clip(
                    shape ?: RoundedCornerShape(
                        if (circular) ShapeRadius.Full else cornerRadius
                    )
                )
        ),
        contentAlignment = Alignment.Center
    ) {
        if (!apiPath.isNullOrBlank()) {
            // La clave de caché se fija a la propia URL en lugar de dejar que Coil la derive.
            //
            // Es lo que permite que la ficha de una película use esta misma carátula —ya
            // descargada por la lista— como relleno mientras baja su fondo grande: para pedirla
            // por su clave hay que saber cuál es, y una clave derivada depende del tamaño con el
            // que se dibujó y de la pantalla.
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(posterUrl)
                    .memoryCacheKey(posterUrl)
                    .build(),
            )

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            if (painter.state is AsyncImagePainter.State.Loading) {
                CircularWavyProgressIndicator(
                    modifier = Modifier.size(progressIndicatorSize),
                )
            } else if (painter.state is AsyncImagePainter.State.Error || painter.state is AsyncImagePainter.State.Empty) {
                placeholder()
            }
        } else {
            placeholder()
        }
    }
}
