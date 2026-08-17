package com.pranshulgg.watchmaster.feature.setting

import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pranshulgg.watchmaster.BuildConfig
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.prefs.LocalAppPrefs
import com.pranshulgg.watchmaster.core.ui.components.CheckboxRow
import com.pranshulgg.watchmaster.core.ui.components.DialogBasic
import com.pranshulgg.watchmaster.core.ui.components.LargeTopBarScaffold
import com.pranshulgg.watchmaster.core.ui.components.NavigateUpBtn
import com.pranshulgg.watchmaster.core.ui.components.SettingSection
import com.pranshulgg.watchmaster.core.ui.components.SettingTile
import com.pranshulgg.watchmaster.core.ui.components.SettingsTileIcon
import com.pranshulgg.watchmaster.core.ui.components.TextAlertDialog
import com.pranshulgg.watchmaster.core.network.TmdbApiKey
import com.pranshulgg.watchmaster.core.ui.localization.AppLanguage
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.snackbar.SnackbarManager
import com.pranshulgg.watchmaster.core.utils.PreferencesHelper
import com.pranshulgg.watchmaster.feature.setting.components.ColorPickerBtn
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SettingsScreen(navController: NavController) {

    val prefs = LocalAppPrefs.current
    val isAndroid12Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    val activity = context as ComponentActivity


    var exportWatchlistChecked by remember { mutableStateOf(true) }
    var exportMovieListChecked by remember { mutableStateOf(true) }

    val exportLauncher = exportLauncher(context, exportWatchlistChecked, exportMovieListChecked)
    val importLauncher = importLauncher(context)

    var automaticFrequency by remember { mutableStateOf(AutomaticBackup.currentFrequency()) }
    var pendingFrequency by remember { mutableStateOf(BackupFrequency.WEEKLY) }
    var lastAutomaticBackup by remember { mutableStateOf(AutomaticBackup.lastBackup()) }
    val automaticBackupLauncher = exportLauncher(
        context = context,
        exportWatchlist = true,
        exportMovieList = true,
        onExportFinished = { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
            AutomaticBackup.configure(context, uri, pendingFrequency)
            automaticFrequency = pendingFrequency
            lastAutomaticBackup = AutomaticBackup.lastBackup()
        },
    )

    var tmdbKeyField by remember { mutableStateOf(TmdbApiKey.userKey().orEmpty()) }
    var tmdbKeyIsUserProvided by remember { mutableStateOf(TmdbApiKey.isUserProvided()) }
    val tmdbKeySavedMessage = localized("Clave guardada", "Key saved")
    val tmdbKeyClearedMessage = localized("Se usará la clave incluida", "Using the bundled key")
    var isExportDialogOpen by remember { mutableStateOf(false) }


    var isWarningImportDialogOpen by remember { mutableStateOf(false) }

    val themeLabels = mapOf(
        "dark" to localized("Oscuro", "Dark"),
        "light" to localized("Claro", "Light"),
        "system" to localized("Sistema", "System"),
    )
    val languageLabels = mapOf(
        "es" to localized("Español", "Spanish"),
        "en" to localized("Inglés", "English"),
    )
    val tabLabels = mapOf(
        "home" to localized("Inicio", "Home"),
        "movies" to localized("Películas", "Movies"),
        "tv" to localized("Series", "TV shows"),
    )
    val backupLabels = BackupFrequency.entries.associate {
        it.name to when (it) {
            BackupFrequency.OFF -> localized("Desactivada", "Off")
            BackupFrequency.DAILY -> localized("Diaria", "Daily")
            BackupFrequency.WEEKLY -> localized("Semanal", "Weekly")
        }
    }

    LargeTopBarScaffold(
        title = localized("Ajustes", "Settings"),
        navigationIcon = { NavigateUpBtn(navController) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SettingSection(
                title = localized("Apariencia", "Appearance"),
                tiles = listOf(
                    SettingTile.DialogOptionTile(
                        leading = { SettingsTileIcon(R.drawable.palette_24px) },
                        title = localized("Tema de la aplicación", "App theme"),
                        options = listOf(
                            "dark",
                            "light",
                            "system",
                        ),
                        selectedOption = prefs.appTheme,
                        optionLabel = { themeLabels[it] ?: it },
                        onOptionSelected = {
                            prefs.setAppTheme(it)
                        }
                    ),
                    SettingTile.SwitchTile(
                        leading = {
                            if (prefs.isCustomTheme) ColorPickerBtn() else SettingsTileIcon(
                                R.drawable.brush_24px
                            )
                        },
                        title = localized("Usar un color personalizado", "Use a custom color"),
                        description = localized(
                            "Elige un color base para generar el tema",
                            "Choose a base color to generate the theme",
                        ),
                        checked = prefs.isCustomTheme,
                        enabled = !prefs.useDynamicColor,
                        onCheckedChange = { checked ->
                            prefs.useCustomTheme(checked)
                            if (!checked) {
                                prefs.setThemeColor("#2196f3")
                            }
                        }
                    ),
                    SettingTile.SwitchTile(
                        leading = {
                            SettingsTileIcon(
                                R.drawable.photo_24px
                            )
                        },
                        title = localized("Colores dinámicos", "Dynamic colors"),
                        description = localized(
                            "Usar los colores del fondo de pantalla",
                            "Use colors from your wallpaper",
                        ),
                        checked = prefs.useDynamicColor,
                        enabled = isAndroid12Plus && !prefs.isCustomTheme,
                        onCheckedChange = { checked ->
                            prefs.setDynamicColor(checked)
                        }
                    ),
                    SettingTile.SwitchTile(
                        leading = { SettingsTileIcon(R.drawable.bedtime_24px) },
                        title = localized("Negro OLED", "OLED black"),
                        description = localized(
                            "Usar negro puro en los fondos del tema oscuro",
                            "Use pure black backgrounds in the dark theme",
                        ),
                        checked = prefs.useAmoledBlack,
                        enabled = prefs.appTheme != "light",
                        onCheckedChange = prefs.setAmoledBlack,
                    ),
                ),
            )
            SettingSection(
                title = localized("General", "General"),
                tiles = listOf(
                    SettingTile.DialogOptionTile(
                        leading = { SettingsTileIcon(R.drawable.article_person_24px) },
                        title = localized("Idioma", "Language"),
                        description = localized(
                            "También cambia el idioma de los nuevos datos de TMDB",
                            "Also changes the language of new TMDB data",
                        ),
                        options = listOf("es", "en"),
                        selectedOption = AppLanguage.code(context),
                        optionLabel = { languageLabels[it] ?: it },
                        onOptionSelected = {
                            AppLanguage.update(context, it)
                            MetadataSync.enqueue(context)
                            activity.recreate()
                        },
                    ),
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.refresh_24px) },
                        title = localized("Actualizar textos de TMDB", "Refresh TMDB text"),
                        description = localized(
                            "Traduce de nuevo títulos, sinopsis, temporadas y episodios guardados",
                            "Refresh saved titles, summaries, seasons, and episodes",
                        ),
                        onClick = {
                            MetadataSync.enqueue(context)
                            SnackbarManager.show(
                                AppLanguage.text(
                                    "Actualización programada; se aplicará en segundo plano",
                                    "Refresh scheduled; it will run in the background",
                                )
                            )
                        },
                    ),
                    SettingTile.DialogOptionTile(
                        leading = { SettingsTileIcon(R.drawable.home_filled_24px) },
                        title = localized("Pantalla de inicio predeterminada", "Default start screen"),
                        options = listOf(
                            "home",
                            "movies",
                            "tv",
                        ),
                        selectedOption = prefs.defaultTab,
                        optionLabel = { tabLabels[it] ?: it },
                        onOptionSelected = {
                            prefs.setDefaultTab(it)
                        }
                    ),
                    SettingTile.SwitchTile(
                        leading = { SettingsTileIcon(R.drawable.view_apps_24px) },
                        title = localized("Agrupar temporadas", "Group seasons"),
                        description = localized(
                            "Tocar una serie entra directamente en ella y las temporadas se eligen dentro, en vez de desplegarse en la lista",
                            "Tapping a show opens it directly and seasons are picked inside, instead of expanding in the list",
                        ),
                        checked = prefs.groupSeasons,
                        onCheckedChange = prefs.setGroupSeasons,
                    ),
                )
            )
            SettingSection(
                title = localized("Datos", "Data"),
                tiles = listOf(
                    SettingTile.DialogOptionTile(
                        leading = { SettingsTileIcon(R.drawable.schedule_24px) },
                        title = localized("Copia de seguridad automática", "Automatic backup"),
                        description = automaticBackupDescription(automaticFrequency, lastAutomaticBackup),
                        options = BackupFrequency.entries.map { it.name },
                        selectedOption = automaticFrequency.name,
                        optionLabel = { backupLabels[it] ?: it },
                        onOptionSelected = { selected ->
                            val frequency = BackupFrequency.valueOf(selected)
                            if (frequency == BackupFrequency.OFF) {
                                AutomaticBackup.setFrequency(context, frequency)
                                automaticFrequency = frequency
                            } else if (PreferencesHelper.getString(AutomaticBackup.URI_KEY).isNullOrBlank()) {
                                pendingFrequency = frequency
                                automaticBackupLauncher(createAutomaticBackupDocumentIntent())
                            } else {
                                AutomaticBackup.setFrequency(context, frequency)
                                automaticFrequency = frequency
                            }
                        },
                    ),
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.folder_24px) },
                        title = localized("Cambiar archivo de copia", "Change backup file"),
                        description = localized(
                            "Elige dónde se guardará la copia automática",
                            "Choose where the automatic backup is saved",
                        ),
                        onClick = {
                            pendingFrequency = automaticFrequency.takeUnless { it == BackupFrequency.OFF }
                                ?: BackupFrequency.WEEKLY
                            automaticBackupLauncher(createAutomaticBackupDocumentIntent())
                        },
                    ),
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.upload_24px) },
                        title = localized("Exportar datos de la aplicación", "Export app data"),
                        onClick = {
                            isExportDialogOpen = true
                        }
                    ),
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.download_24px) },
                        title = localized("Importar datos de la aplicación", "Import app data"),
                        onClick = {
                            isWarningImportDialogOpen = true
                        }
                    )
                )
            )
            SettingSection(
                title = localized("API de TMDB", "TMDB API"),
                tiles = listOf(
                    SettingTile.DialogTextFieldTile(
                        leading = { SettingsTileIcon(R.drawable.experiment_24px) },
                        title = localized("Clave de API propia", "Your own API key"),
                        description = if (tmdbKeyIsUserProvided) {
                            localized(
                                "Se está usando tu clave. Vacíala para volver a la incluida.",
                                "Using your key. Clear it to go back to the bundled one.",
                            )
                        } else {
                            localized(
                                "Opcional. Sin ella se usa la clave incluida en la aplicación.",
                                "Optional. Without one the bundled key is used.",
                            )
                        },
                        initialText = tmdbKeyField,
                        placeholder = localized("Clave de API v3", "v3 API key"),
                        placeholderTextField = localized("Pega aquí tu clave", "Paste your key here"),
                        onTextSubmitted = { value ->
                            val trimmed = value.trim()
                            if (trimmed.isEmpty()) TmdbApiKey.clear() else TmdbApiKey.set(trimmed)
                            tmdbKeyField = trimmed
                            tmdbKeyIsUserProvided = TmdbApiKey.isUserProvided()
                            SnackbarManager.show(
                                if (trimmed.isEmpty()) tmdbKeyClearedMessage else tmdbKeySavedMessage
                            )
                        },
                    ),
                )
            )
            SettingSection(
                title = localized("Acerca de", "About"),
                tiles = listOf(
                    SettingTile.TextTile(
                        title = "Visto ${BuildConfig.VERSION_NAME}",
                        description = localized(
                            "Este producto utiliza la API de TMDB, pero TMDB no lo respalda ni certifica.",
                            "This product uses the TMDB API but is not endorsed or certified by TMDB.",
                        )
                    )
                )
            )
        }
    }


    DialogBasic(
        show = isExportDialogOpen,
        onConfirm = {
            val intent = createNewDocumentIntent()
            exportLauncher(intent)
        },
        onDismiss = {
            isExportDialogOpen = false
        },
        title = localized("Exportar datos", "Export data"),
        confirmBtnDisabled = !exportWatchlistChecked && !exportMovieListChecked,
    ) {
        Column() {

            CheckboxRow(
                label = localized("Incluir listas", "Include lists"),
                checked = exportMovieListChecked
            ) { exportMovieListChecked = it }

            Text(
                localized(
                    "Crea siempre una copia después de actualizar la aplicación: es posible que las copias antiguas no funcionen con versiones futuras.",
                    "Create a new backup after updating the app; old backups might not work with future versions.",
                ),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp, start = 16.dp, end = 16.dp)
            )
        }
    }

    TextAlertDialog(
        show = isWarningImportDialogOpen,
        onDismiss = {
            isWarningImportDialogOpen = false
        },
        title = localized("Importar datos", "Import data"),
        onConfirm = {
            val intent = createOpenDocumentIntent()
            importLauncher(intent)
        },
        message = localized(
            "La importación sustituirá los datos actuales. ¿Seguro que quieres continuar?",
            "Importing will replace the current data. Are you sure you want to continue?",
        )
    )
}

@Composable
private fun frequencyLabel(frequency: BackupFrequency): String = when (frequency) {
    BackupFrequency.OFF -> localized("Desactivada", "Off")
    BackupFrequency.DAILY -> localized("Diaria", "Daily")
    BackupFrequency.WEEKLY -> localized("Semanal", "Weekly")
}

@Composable
private fun automaticBackupDescription(
    frequency: BackupFrequency,
    lastBackup: java.time.Instant?,
): String {
    if (frequency == BackupFrequency.OFF) {
        return localized("Desactivada", "Off")
    }
    val frequencyText = frequencyLabel(frequency)
    if (lastBackup == null) {
        return localized("$frequencyText · pendiente de la primera copia", "$frequencyText · first backup pending")
    }
    val locale = if (AppLanguage.code(LocalContext.current) == "en") {
        Locale.US
    } else {
        Locale.forLanguageTag("es-ES")
    }
    val date = lastBackup.atZone(ZoneId.systemDefault()).format(
        DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", locale),
    )
    return localized("$frequencyText · última: $date", "$frequencyText · last: $date")
}
