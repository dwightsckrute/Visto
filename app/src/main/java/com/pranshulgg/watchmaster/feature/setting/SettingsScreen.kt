package com.pranshulgg.watchmaster.feature.setting

import android.os.Build
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
import com.pranshulgg.watchmaster.feature.setting.components.ColorPickerBtn

@Composable
fun SettingsScreen(navController: NavController) {

    val prefs = LocalAppPrefs.current
    val isAndroid12Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current


    var exportWatchlistChecked by remember { mutableStateOf(true) }
    var exportMovieListChecked by remember { mutableStateOf(true) }

    val exportLauncher = exportLauncher(context, exportWatchlistChecked, exportMovieListChecked)
    val importLauncher = importLauncher(context)

    var isExportDialogOpen by remember { mutableStateOf(false) }


    var isWarningImportDialogOpen by remember { mutableStateOf(false) }

    LargeTopBarScaffold(
        title = "Ajustes",
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
                title = "Apariencia",
                tiles = listOf(
                    SettingTile.DialogOptionTile(
                        leading = { SettingsTileIcon(R.drawable.palette_24px) },
                        title = "Tema de la aplicación",
                        options = listOf(
                            "Oscuro",
                            "Claro",
                            "Sistema",
                        ),
                        selectedOption = prefs.appTheme,
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
                        title = "Usar un color personalizado",
                        description = "Elige un color base para generar el tema",
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
                        title = "Colores dinámicos",
                        description = "Usar los colores del fondo de pantalla",
                        checked = prefs.useDynamicColor,
                        enabled = isAndroid12Plus && !prefs.isCustomTheme,
                        onCheckedChange = { checked ->
                            prefs.setDynamicColor(checked)
                        }
                    ),
                ),
            )
            SettingSection(
                title = "General",
                tiles = listOf(
                    SettingTile.DialogOptionTile(
                        leading = { SettingsTileIcon(R.drawable.home_filled_24px) },
                        title = "Pantalla de inicio predeterminada",
                        options = listOf(
                            "Inicio",
                            "Películas",
                            "Series",
                        ),
                        selectedOption = prefs.defaultTab,
                        onOptionSelected = {
                            prefs.setDefaultTab(it)
                        }
                    )
                )
            )
            SettingSection(
                title = "Datos",
                tiles = listOf(
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.upload_24px) },
                        title = "Exportar datos de la aplicación",
                        onClick = {
                            isExportDialogOpen = true
                        }
                    ),
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.download_24px) },
                        title = "Importar datos de la aplicación",
                        onClick = {
                            isWarningImportDialogOpen = true
                        }
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
        title = "Exportar datos",
        confirmBtnDisabled = !exportWatchlistChecked && !exportMovieListChecked,
    ) {
        Column() {

            CheckboxRow(
                label = "Incluir listas",
                checked = exportMovieListChecked
            ) { exportMovieListChecked = it }

            Text(
                "Crea siempre una copia después de actualizar la aplicación: es posible que las copias antiguas no funcionen con versiones futuras.",
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
        title = "Importar datos",
        onConfirm = {
            val intent = createOpenDocumentIntent()
            importLauncher(intent)
        },
        message = "La importación sustituirá los datos actuales. ¿Seguro que quieres continuar?"
    )
}
