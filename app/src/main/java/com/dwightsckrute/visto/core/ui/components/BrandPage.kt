package com.dwightsckrute.visto.core.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.dwightsckrute.visto.core.ui.theme.Spacing

/**
 * El armazón de una página del carrusel: su contenido, y encima la cabecera que se pliega.
 *
 * Las tres páginas lo usan, pero sólo Ajustes con el logotipo. Ponerlo grande en las tres se probó y
 * se deshizo: la aplicación parecía tres portadas seguidas, y Visto e Historial son para mirar un
 * número y unas barras. Lo que sí es de las tres es el **movimiento del título**: empieza grande y se
 * queda fijo arriba como una barra estrecha, en lugar de irse con el contenido.
 *
 * El contenido se desplaza **por detrás** de la cabecera, que tiene fondo opaco: por eso se le
 * reserva arriba el hueco de la cabecera desplegada en lugar de colocarla dentro del `Column`.
 *
 * @param contentPadding margen lateral del contenido. Ajustes lo pone a cero porque sus secciones
 *   traen el suyo; las otras dos lo quieren.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrandPage(
    title: String,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
    style: BrandHeaderStyle = BrandHeaderStyle.SMALL_MARK,
    navigationIcon: (@Composable () -> Unit)? = null,
    contentPadding: Dp = Spacing.lg,
    spacing: Dp = Spacing.md,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    // El plegado se calcula aquí y se **lee** dentro de la cabecera, en fase de diseño: desplazar no
    // recompone la página entera.
    val collapseDistance = with(LocalDensity.current) {
        BrandHeaderDefaults.collapseDistance(style).toPx()
    }

    // El rebote al llegar a los extremos, en lugar del estirado de serie de Android. Se provee por
    // `LocalOverscrollFactory` y no aplicando un modificador: `verticalScroll` pide su efecto a ese
    // local, así que basta con envolver el contenido y lo recoge él.
    val overscroll = rememberBounceOverscrollFactory()

    Box(modifier = modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalOverscrollFactory provides overscroll) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
        ) {
            // El hueco de la cabecera desplegada.
            Spacer(Modifier.height(BrandHeaderDefaults.expandedHeight(style)))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = contentPadding),
                verticalArrangement = Arrangement.spacedBy(spacing),
                content = content,
            )

            Spacer(Modifier.height(bottomPadding))
        }
        }

        CollapsingBrandHeader(
            title = title,
            style = style,
            navigationIcon = navigationIcon,
            progress = { (scrollState.value / collapseDistance).coerceIn(0f, 1f) },
        )
    }
}

/**
 * La misma página cuando no hay nada que desplazar.
 *
 * El estado vacío del historial no lleva `verticalScroll` a propósito: con contenido que cabe de
 * sobra, el gesto llegaba igualmente a la cabecera y ésta hacía un recorrido mínimo que parecía un
 * salto. Sin desplazamiento la cabecera se queda desplegada, que es lo correcto: no hay nada debajo
 * por lo que plegarla.
 */
@Composable
fun BrandPageStatic(
    title: String,
    modifier: Modifier = Modifier,
    style: BrandHeaderStyle = BrandHeaderStyle.SMALL_MARK,
    contentPadding: Dp = Spacing.lg,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CollapsingBrandHeader(title = title, style = style, progress = { 0f })
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = contentPadding),
            content = content,
        )
    }
}
