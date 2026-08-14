package com.pranshulgg.watchmaster.feature.setting

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.room.withTransaction
import com.google.gson.Gson
import com.pranshulgg.watchmaster.core.ui.snackbar.SnackbarManager
import com.pranshulgg.watchmaster.core.ui.localization.AppLanguage
import com.pranshulgg.watchmaster.data.local.WatchMasterDatabase
import com.pranshulgg.watchmaster.data.local.entity.CustomListEntity
import com.pranshulgg.watchmaster.data.local.entity.SeasonEntity
import com.pranshulgg.watchmaster.data.local.entity.WatchlistItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class ExportData(
    val version: Int,
    val watchlist: List<WatchlistItemEntity>,
    val tvSeasons: List<SeasonEntity>,
    val movieLists: List<CustomListEntity>
)

@Composable
fun exportLauncher(
    context: Context,
    exportWatchlist: Boolean = false,
    exportMovieList: Boolean = false,
    onExportFinished: ((Uri) -> Unit)? = null,
): (Intent) -> Unit {
    val scope = rememberCoroutineScope()


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        uri?.let {
            scope.launch {
                if (export(context, uri, exportWatchlist, exportMovieList)) {
                    onExportFinished?.invoke(it)
                }
            }
        }
    }

    return { intent ->
        launcher.launch(intent)
    }
}

@Composable
fun importLauncher(context: Context): (Intent) -> Unit {
    val scope = rememberCoroutineScope()


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        uri?.let {
            scope.launch {
                import(context, uri)
            }
        }
    }

    return { intent ->
        launcher.launch(intent)
    }
}

private suspend fun export(
    context: Context,
    uri: Uri,
    exportWatchlist: Boolean = false,
    exportMovieList: Boolean = false
): Boolean {
    val exported = exportData(context, uri, exportWatchlist, exportMovieList)

    SnackbarManager.show(
        if (exported) AppLanguage.text("Datos exportados correctamente", "Data exported successfully")
        else AppLanguage.text("No se pudieron exportar los datos", "Data could not be exported")
    )
    return exported
}

suspend fun exportData(
    context: Context,
    uri: Uri,
    exportWatchlist: Boolean = true,
    exportMovieList: Boolean = true,
): Boolean {
    val db = WatchMasterDatabase.getInstance(context)
    val watchlistData = if (exportWatchlist) {
        db.watchlistDao().getAll().first()
    } else emptyList()
    val tvSeasons = if (exportWatchlist) {
        db.seasonDao().getAllSeasons().first()
    } else emptyList()
    val movieLists = if (exportMovieList) {
        db.movieListsDao().getAllCustomLists().first()
    } else emptyList()

    val json = Gson().toJson(ExportData(4, watchlistData, tvSeasons, movieLists))

    return runCatching {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { file ->
                file.write(json.toByteArray())
            } ?: error("Unable to open destination")
        }
    }.isSuccess
}

private suspend fun import(context: Context, uri: Uri) {

    val db = WatchMasterDatabase.getInstance(context)

    val data = runCatching {
        val json = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("Empty backup")
        Gson().fromJson(json, ExportData::class.java)
            ?.also { parsed ->
                // Gson can assign null to Kotlin non-null properties when a
                // malformed JSON omits a field, so force validation here.
                parsed.watchlist.size
                parsed.tvSeasons.size
                parsed.movieLists.size
            }
            ?: error("Invalid backup")
    }.getOrElse {
        SnackbarManager.show(AppLanguage.text("La copia seleccionada no es válida", "The selected backup is invalid"))
        return
    }


    if (data.watchlist.isEmpty() && data.movieLists.isEmpty()) {
        SnackbarManager.show(AppLanguage.text("No hay datos que importar", "There is no data to import"))
        return
    }

    if (data.version != 4) {
        SnackbarManager.show(AppLanguage.text("Versión no compatible", "Unsupported backup version"))
        return
    }

    val imported = runCatching {
        db.withTransaction {
            if (data.watchlist.isNotEmpty()) {
                db.watchlistDao().clearAll()
                db.seasonDao().clearAll()
            }

            if (data.movieLists.isNotEmpty()) {
                db.movieListsDao().clearAll()
            }

            if (data.watchlist.isNotEmpty()) {
                db.watchlistDao().insertAll(data.watchlist)
                db.seasonDao().insertSeasons(data.tvSeasons)
            }

            if (data.movieLists.isNotEmpty()) {
                db.movieListsDao().insertAll(data.movieLists)
            }
        }
    }.isSuccess

    SnackbarManager.show(
        if (imported) AppLanguage.text("Datos importados correctamente", "Data imported successfully")
        else AppLanguage.text(
            "No se pudo importar la copia; tus datos actuales no han cambiado",
            "The backup could not be imported; your current data was not changed",
        )
    )
}


fun createNewDocumentIntent(): Intent {
    val randomId = (100000..999999).random()
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/json"
        putExtra(Intent.EXTRA_TITLE, "visto_backup-${randomId}-v4.json")
    }

    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    return intent
}

fun createAutomaticBackupDocumentIntent(): Intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
    addCategory(Intent.CATEGORY_OPENABLE)
    type = "application/json"
    putExtra(Intent.EXTRA_TITLE, "visto_copia_automatica-v4.json")
    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
}


fun createOpenDocumentIntent(): Intent {

    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/json"
    }

    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION


    return intent
}
