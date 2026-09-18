package com.dwightsckrute.visto.core.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.dwightsckrute.visto.BuildConfig
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.tiles.ActionTile
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.theme.Spacing

/**
 * Quién hace Visto, al mantener pulsada la marca.
 *
 * Es lo que la marca de una aplicación suele esconder: el toque corto es el guiño y la pulsación
 * larga es la ficha. Va aquí y no sólo en el aviso legal porque el logotipo es lo que se toca cuando
 * uno se pregunta de quién es esto, y mandar a Ajustes → Aviso legal para responderlo es un viaje
 * largo para una pregunta corta. El texto es el mismo que el del aviso legal a propósito: dos
 * versiones de quién hace la aplicación es una de más.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtalayaSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ActionBottomSheet(
        sheetState = sheetState,
        onCancel = onDismiss,
        onConfirm = onDismiss,
        showActions = false,
    ) { hide ->
        AtalayaSheetBody(
            onWeb = {
                hide()
                openAtalayaWeb(context)
            },
            onMail = {
                hide()
                writeToAtalaya(context)
            },
            onCoffee = {
                hide()
                openKofi(context)
            },
        )
    }
}

/**
 * El contenido de la ficha, aparte de la hoja que lo contiene.
 *
 * Separado para poder verlo: una hoja modal vive en su propia ventana y no se puede pintar en un
 * `ComposeView`, así que sin esto la única forma de revisar el diseño era tener el teléfono
 * desbloqueado delante. Ver `AtalayaSheetLookTest`.
 */
@Composable
internal fun AtalayaSheetBody(
    onWeb: () -> Unit,
    onMail: () -> Unit,
    onCoffee: () -> Unit,
) {
        Column(
            modifier = Modifier.padding(
                start = Spacing.lg,
                end = Spacing.lg,
                bottom = Spacing.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            AtalayaBrand()

            Text(
                text = localized(
                    "Visto es una aplicación de Atalaya Software.",
                    "Visto is an Atalaya Software application.",
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = localized(
                    "Mide a qué distancia tienes la pantalla y te avisa cuando te acercas " +
                        "demasiado. Lo hace con la cámara frontal, dentro del teléfono y sin " +
                        "guardar ninguna imagen.",
                    "It measures how far the screen is from your face and warns you when you get " +
                        "too close. It does it with the front camera, inside the phone, and " +
                        "without storing any image.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Las dos filas van con las esquinas de un grupo —arriba grande, la junta pequeña,
            // abajo grande— para que se lean como una sola tarjeta partida, igual que en Ajustes.
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ActionTile(
                    headline = "atalayasoftware.com",
                    description = localized(
                        "La web, donde se publica Visto",
                        "The site, where Visto is published",
                    ),
                    leading = { SettingsTileIcon(R.drawable.open_in_new_24px) },
                    shapes = RoundedCornerShape(
                        topStart = ShapeRadius.ExtraLarge,
                        topEnd = ShapeRadius.ExtraLarge,
                        bottomStart = ShapeRadius.ExtraSmall,
                        bottomEnd = ShapeRadius.ExtraSmall,
                    ),
                    itemBgColor = MaterialTheme.colorScheme.surfaceBright,
                    onClick = onWeb,
                )
                ActionTile(
                    headline = BRAND_EMAIL,
                    description = localized(
                        "Errores, sugerencias y cualquier otra cosa",
                        "Bugs, suggestions and anything else",
                    ),
                    leading = { SettingsTileIcon(R.drawable.mail_24px) },
                    shapes = RoundedCornerShape(ShapeRadius.ExtraSmall),
                    itemBgColor = MaterialTheme.colorScheme.surfaceBright,
                    onClick = onMail,
                )
                // El café va el último de los tres a propósito. Visto no tiene ni anuncios ni
                // compras, y esto es lo único que hay para sostenerla mientras no exista otra cosa;
                // ponerlo en la primera fila convertiría la ficha de quién la hace en un cepillo.
                ActionTile(
                    headline = localized("Invítame a un café", "Buy me a coffee"),
                    description = localized(
                        "Visto es gratis y sin anuncios. Si te sirve, esto la mantiene.",
                        "Visto is free and ad-free. If it helps you, this keeps it going.",
                    ),
                    leading = { SettingsTileIcon(R.drawable.local_cafe_24px) },
                    shapes = RoundedCornerShape(
                        topStart = ShapeRadius.ExtraSmall,
                        topEnd = ShapeRadius.ExtraSmall,
                        bottomStart = ShapeRadius.ExtraLarge,
                        bottomEnd = ShapeRadius.ExtraLarge,
                    ),
                    itemBgColor = MaterialTheme.colorScheme.surfaceBright,
                    onClick = onCoffee,
                )
            }

            Text(
                text = localized(
                    "Visto ${BuildConfig.VERSION_NAME} · © 2026 Atalaya Software",
                    "Visto ${BuildConfig.VERSION_NAME} · © 2026 Atalaya Software",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
}

private const val BRAND_EMAIL = "visto@atalayasoftware.com"

private fun openAtalayaWeb(context: Context) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, "https://atalayasoftware.com".toUri()))
    }
}

/**
 * El perfil donde se puede invitar a un café.
 *
 * Es un enlace al navegador y no una compra dentro de la aplicación: Visto no se reparte por Google
 * Play, así que no hay facturación que usar, y montar un cobro propio para un donativo voluntario
 * sería pedirle a la aplicación que gestione dinero sin necesidad ninguna.
 */
private fun openKofi(context: Context) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, KOFI_URL.toUri()))
    }
}

private const val KOFI_URL = "https://ko-fi.com/atalayasoftware"

private fun writeToAtalaya(context: Context) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:$BRAND_EMAIL".toUri()))
    }
}
