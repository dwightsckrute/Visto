package com.dwightsckrute.visto.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dwightsckrute.visto.core.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * La cabecera de las páginas: el título grande arriba, que al desplazar se encoge y se queda fijo
 * como una barra estrecha. En Ajustes lleva además el logotipo, que se va a la esquina derecha.
 *
 * Es una cabecera plegable hecha a mano y no `LargeFlexibleTopAppBar` por dos motivos. El primero es
 * que la barra de Material sólo sabe plegar texto, y aquí lo que se pliega es una pieza gráfica que
 * además **cambia de sitio**: empieza centrada sobre el título y termina a su izquierda. El segundo
 * es que el plegado de una barra de `Scaffold` es **uno para las tres páginas** del carrusel: al
 * desplazar en una y cambiar a otra, la segunda aparecía con el título medio plegado sin haberla
 * tocado. Hubo una temporada en la que el título dejó de plegarse por eso —vivía dentro del contenido
 * y se iba con él—; esta cabecera recupera el plegado con el estado de cada página por separado.
 *
 * **Nada de esto recompone.** `progress` se lee dentro de la política de medida, que es fase de
 * diseño, y las dos piezas se colocan con `placeWithLayer`: el escalado vive en la capa gráfica, así
 * que al desplazar sólo se vuelve a colocar y a dibujar. Los hijos se miden una vez, siempre a su
 * tamaño expandido; lo que cambia es la escala con la que se pintan.
 *
 * El fondo es opaco y del color de la página: el contenido pasa por detrás de la cabecera, que es lo
 * que hace que parezca que se mete debajo en lugar de empujarla.
 *
 * @param progress 0 = desplegada del todo, 1 = plegada del todo. Como lambda, para que leerla no
 *   obligue a recomponer a quien la pasa.
 * @param style [BrandHeaderStyle.MARK] sólo en Ajustes. El logotipo grande en las tres páginas se
 *   probó y se deshizo: la aplicación parecía tres portadas seguidas. Pero el movimiento del título
 *   sí es de las tres, porque es lo que hace que la cabecera se quede arriba en vez de irse con el
 *   contenido.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun CollapsingBrandHeader(
    title: String,
    progress: () -> Float,
    modifier: Modifier = Modifier,
    style: BrandHeaderStyle = BrandHeaderStyle.MARK,
    horizontalPadding: Dp = Spacing.lg,
    navigationIcon: (@Composable () -> Unit)? = null,
) {
    val background = MaterialTheme.colorScheme.surfaceContainer

    // El rebote de entrada: la galleta llega un poco pequeña y se asienta con el muelle espacial del
    // tema, que en Material 3 Expressive rebasa un punto antes de pararse. Es lo único que se mueve
    // solo en toda la pantalla, dura lo que dura y no vuelve a ocurrir: la regla de que nada se anima
    // en reposo sigue en pie —ver `docs/DESIGN_SYSTEM.md`—, y una marca que latiera para siempre
    // sería exactamente lo que esa regla prohíbe.
    val entrance = remember { Animatable(ENTRANCE_FROM) }
    val entranceSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()

    // El giro al tocar la marca. Es lo único de la aplicación que se anima porque se le pide, sin
    // más consecuencia que la de moverse: un guiño, no un control. Se lanza en dos tiempos —primero
    // la galleta y un suspiro después la «A»— porque las dos piezas girando a la vez se leen como
    // una sola imagen dando vueltas, y con el retraso parece que la letra la arrastra el envase.
    val scope = rememberCoroutineScope()
    val spin = remember { Animatable(0f) }
    val markSpin = remember { Animatable(0f) }
    // Aquí no vale el muelle del tema: está afinado para que una pieza se coloque en su sitio en
    // dos o tres décimas, y un adorno que se mira a propósito necesita más rato. Una vuelta de casi
    // un segundo con entrada y salida suaves se sigue con el ojo; con el muelle rápido era un
    // parpadeo.
    val spinSpec = remember { tween<Float>(SPIN_DURATION_MS, easing = FastOutSlowInEasing) }
    val interaction = remember { MutableInteractionSource() }

    // Y mantenerla pulsada abre la ficha de quién hace la aplicación. El toque corto es el
    // guiño; la pulsación larga es la información, que es lo que la marca de una aplicación
    // esconde en todas partes.
    //
    // La ficha se abre **al levantar el dedo**, no al cumplirse la pulsación larga. Medido en el
    // teléfono: una hoja que nace debajo de un dedo todavía apoyado se lleva ese puntero como si
    // fuera un arrastre y se queda parada a media altura hasta que lo levantas —600 ms clavada en un
    // desplazamiento de 1848 sin asentarse—, que es exactamente la sensación de no poder bajarla.
    //
    // A cambio hay que dar señal de que la pulsación ha valido, porque si no parece que no responde:
    // de eso se encarga el toque del motor, que es además lo que Android usa para esto en todas
    // partes.
    val haptics = LocalHapticFeedback.current
    val pressed by interaction.collectIsPressedAsState()
    var sheetPending by remember { mutableStateOf(false) }
    var brandSheet by remember { mutableStateOf(false) }

    LaunchedEffect(sheetPending, pressed) {
        if (sheetPending && !pressed) {
            sheetPending = false
            brandSheet = true
        }
    }
    LaunchedEffect(style) {
        if (style == BrandHeaderStyle.MARK) {
            entrance.animateTo(1f, entranceSpec)
        } else {
            // La marca pequeña aparece ya puesta. Un rebote en cada una de las tres páginas cada vez
            // que se abren es tres veces el mismo truco, y el truco deja de ser uno.
            entrance.snapTo(1f)
        }
    }

    if (brandSheet) {
        AtalayaSheet(onDismiss = { brandSheet = false })
    }

    Layout(
        modifier = modifier
            .fillMaxWidth()
            // Antes de `clipToBounds` y de la política de medida: los dos dibujan contra el alto ya
            // interpolado, que es el que esta capa reporta.
            .drawBehind { drawRect(background) }
            .clipToBounds(),
        content = {
            val badgeSize = when (style) {
                BrandHeaderStyle.MARK -> BADGE_SIZE
                BrandHeaderStyle.SMALL_MARK -> COLLAPSED_BADGE_SIZE
            }
            Box(
                modifier = Modifier
                    // Como lambda: el rebote vive en la capa gráfica y no recompone nada. Se
                    // multiplica con la escala del plegado, que se aplica al colocar.
                    .graphicsLayer {
                        // El pulso de tamaño va y vuelve dentro del mismo recorrido: un seno, que
                        // vale cero en los dos extremos, así que empieza y acaba en su sitio aunque
                        // el muelle rebase al final.
                        val pulse = 1f + SPIN_PULSE * sin(PI.toFloat() * spin.value.coerceIn(0f, 1f))
                        scaleX = entrance.value * pulse
                        scaleY = entrance.value * pulse
                        rotationZ = spin.value * FULL_TURN
                    }
                    .size(badgeSize)
                    // Una de las formas de Material 3 Expressive. El logotipo llega como silueta, así
                    // que se tiñe del color de contraste del contenedor: con cualquier paleta que
                    // elija el usuario el par `primaryContainer`/`onPrimaryContainer` se ve.
                    .clip(MaterialShapes.Cookie9Sided.toShape())
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .combinedClickable(
                        interactionSource = interaction,
                        // Sin destello: la forma es recortada y el destello de serie sale cuadrado
                        // por las esquinas de la galleta.
                        indication = null,
                        onClickLabel = "Atalaya Software",
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            sheetPending = true
                        },
                        onLongClickLabel = "Atalaya Software",
                    ) {
                        scope.launch {
                            spin.snapTo(0f)
                            spin.animateTo(1f, spinSpec)
                        }
                        scope.launch {
                            markSpin.snapTo(0f)
                            delay(MARK_LAG_MS)
                            markSpin.animateTo(1f, spinSpec)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                AtalayaMark(
                    size = badgeSize * MARK_RATIO,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    // La letra deshace el giro del envase y hace el suyo con retraso: si girara con
                    // él no se notaría que gira nada, porque una galleta de nueve puntas vuelve a
                    // ser ella misma cada cuarenta grados.
                    modifier = Modifier.graphicsLayer {
                        rotationZ = (markSpin.value - spin.value) * FULL_TURN
                    },
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Palmo no lo necesita —sus páginas son un carrusel, no hay a dónde volver— y aquí sí:
            // Ajustes se abre desde la barra de la pantalla principal.
            if (navigationIcon != null) navigationIcon()
        },
    ) { measurables, constraints ->
        val padding = horizontalPadding.roundToPx()
        val width = constraints.maxWidth
        val big = style == BrandHeaderStyle.MARK

        val badge = measurables[0].measure(Constraints())
        val nav = if (navigationIcon != null) measurables.last().measure(Constraints()) else null
        // El ancho del título deja fuera el hueco de la marca: en la barra plegada los dos comparten
        // fila, y un título largo no puede meterse por debajo del logotipo.
        val reserved = COLLAPSED_BADGE_SIZE.roundToPx() + Spacing.md.roundToPx()
        val titleRoom = width - padding * 2 - reserved
        val title = measurables[1].measure(Constraints(maxWidth = titleRoom.coerceAtLeast(0)))

        val fraction = progress().coerceIn(0f, 1f)
        val expanded = expandedHeightOf(style).toPx()
        val collapsed = COLLAPSED_HEIGHT.toPx()
        val height = lerp(expanded, collapsed, fraction)

        val collapsedBadge = COLLAPSED_BADGE_SIZE.toPx()
        // La pequeña ya está a su tamaño final: ni se escala ni se mueve al desplazar. Es una firma,
        // no una pieza de la animación, y una marca que baila al desplazar llama la atención sobre sí
        // misma en las dos pantallas donde lo que hay que mirar es un número.
        val badgeScale = if (big) lerp(1f, collapsedBadge / badge.width, fraction) else 1f
        val titleScale = lerp(1f, COLLAPSED_TITLE_SCALE, fraction)

        // La marca se va a la **esquina derecha** y el título se queda donde está en todas las demás
        // páginas: pegado al margen izquierdo. Es lo que evita que se pisen a mitad de camino. Con
        // los dos yendo al mismo sitio —que fue el primer intento— hay un tramo largo en el que la
        // marca todavía es grande y el título ya ha empezado a meterse debajo: se veía el título
        // saliendo por detrás de la galleta.
        //
        // Los destinos se calculan con el tamaño **final** de la marca, no con el que tiene a mitad
        // de camino. Centrar los 148 dp en una barra de 64 daba una posición negativa, y por eso la
        // galleta se salía por arriba al empezar a desplazar.
        val badgeCorner = width - padding - collapsedBadge
        val badgeX = if (big) {
            lerp((width - badge.width) / 2f, badgeCorner, fraction)
        } else {
            badgeCorner
        }
        val badgeY = if (big) {
            lerp(TOP_GAP.toPx(), (collapsed - collapsedBadge) / 2f, fraction)
        } else {
            (collapsed - collapsedBadge) / 2f
        }

        // Desplegado el título se apoya en el margen; plegado tiene que apartarse del botón de
        // volver, que ocupa esa misma esquina. Se interpola en vez de saltar.
        val navRoom = nav?.let { it.width.toFloat() } ?: 0f
        val titleX = lerp(padding.toFloat(), padding + navRoom, fraction)
        // Con marca, el título va debajo de la galleta. Sin ella se apoya en el borde de abajo de la
        // cabecera desplegada, que es donde estaba cuando el título era contenido: así al desplazar
        // sólo sube y
        // encoge, sin dar un salto al empezar.
        val titleTop = if (big) {
            TOP_GAP.toPx() + badge.height + Spacing.md.toPx()
        } else {
            expanded - title.height - Spacing.md.toPx()
        }
        val titleY = lerp(
            titleTop,
            (collapsed - title.height * COLLAPSED_TITLE_SCALE) / 2f,
            fraction,
        )

        layout(width, height.roundToInt()) {
            // El origen de la transformación es la esquina superior izquierda para que la posición
            // colocada sea la del píxel que se ve: con el origen en el centro, escalar movería la
            // pieza y habría que corregir la traslación por la mitad de lo encogido.
            badge.placeWithLayer(badgeX.roundToInt(), badgeY.roundToInt()) {
                scaleX = badgeScale
                scaleY = badgeScale
                transformOrigin = TopLeft
            }
            title.placeWithLayer(titleX.roundToInt(), titleY.roundToInt()) {
                scaleX = titleScale
                scaleY = titleScale
                transformOrigin = TopLeft
            }
            // Siempre a la izquierda y centrado en la altura de la barra plegada, esté la cabecera
            // desplegada o no: un botón de volver que se mueve al desplazar es un botón que hay
            // que perseguir.
            nav?.place(
                x = (padding / 2f).roundToInt(),
                y = ((collapsed - nav.height) / 2f).roundToInt(),
            )
        }
    }
}

/**
 * Cuánta marca lleva la cabecera.
 *
 * [MARK] es sólo Ajustes: la galleta grande arriba del todo, que al plegarse se va a la esquina.
 * [SMALL_MARK] es Visto e Historial: la misma galleta pero **siempre pequeña y siempre en la
 * esquina**, que es donde acaba la de Ajustes. Así la marca está en las tres páginas sin que ninguna
 * parezca una portada, y al pasar de una a otra el logotipo no salta de sitio: en las tres está en el
 * mismo punto.
 */
enum class BrandHeaderStyle { MARK, SMALL_MARK }

/** Las medidas de [CollapsingBrandHeader], que quien la usa necesita para reservarle el hueco. */
object BrandHeaderDefaults {
    fun expandedHeight(style: BrandHeaderStyle): Dp = expandedHeightOf(style)

    val collapsedHeight: Dp = COLLAPSED_HEIGHT

    /** Cuánto hay que desplazar para plegarla del todo. */
    fun collapseDistance(style: BrandHeaderStyle): Dp = expandedHeightOf(style) - COLLAPSED_HEIGHT
}

private fun expandedHeightOf(style: BrandHeaderStyle): Dp = when (style) {
    BrandHeaderStyle.MARK -> EXPANDED_HEIGHT
    BrandHeaderStyle.SMALL_MARK -> SMALL_MARK_HEIGHT
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

private val TopLeft = TransformOrigin(0f, 0f)

private val BADGE_SIZE = 148.dp
private val COLLAPSED_BADGE_SIZE = 44.dp

/** El logotipo dentro de su forma: deja aire para que las puntas de la galleta se lean. */
private const val MARK_RATIO = 0.42f

private const val COLLAPSED_TITLE_SCALE = 0.62f

private val TOP_GAP = 8.dp

/** De dónde arranca el rebote. Un 12% más pequeña: se nota y no parece que la pantalla dé un salto. */
private const val ENTRANCE_FROM = 0.88f

/** Una vuelta entera: la galleta acaba exactamente donde estaba. */
private const val FULL_TURN = 360f

/** Cuánto crece la galleta a mitad del giro: se acerca mientras da la vuelta y se retira. */
private const val SPIN_PULSE = 0.26f

/** El suspiro que la «A» tarda en arrancar detrás del envase. */
private const val MARK_LAG_MS = 140L

/** Lo que dura la vuelta entera. */
private const val SPIN_DURATION_MS = 900
private val EXPANDED_HEIGHT = 216.dp

/**
 * El alto de la cabecera con la marca pequeña, desplegada.
 *
 * 96 dp: el título de `headlineLarge` más su aire. Poco más: el recorrido
 * hasta los 64 de la barra plegada tiene que notarse, pero esta cabecera está en la pantalla donde
 * se mira el número grande, y robarle alto al contenido para que el título tenga sitio sería el
 * negocio al revés.
 */
private val SMALL_MARK_HEIGHT = 96.dp
private val COLLAPSED_HEIGHT = 64.dp
