package com.pranshulgg.watchmaster.feature.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.feature.search.components.SearchRow
import com.pranshulgg.watchmaster.feature.shared.WatchlistViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class SearchContentState { START, LOADING, RESULTS, EMPTY, ERROR }

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalAnimationApi::class,
)
@Composable
fun SearchScreenContent(
    paddingValues: PaddingValues,
    viewModel: SearchViewModel,
    searchType: SearchType,
    watchlistViewModel: WatchlistViewModel,
) {
    val scope = rememberCoroutineScope()
    val searchMotion = motionScheme.defaultSpatialSpec<androidx.compose.ui.unit.Dp>()
    var searchActive by rememberSaveable { mutableStateOf(viewModel.query.isNotBlank()) }
    val state = when {
        viewModel.query.isBlank() -> SearchContentState.START
        viewModel.loading -> SearchContentState.LOADING
        viewModel.searchFailed -> SearchContentState.ERROR
        viewModel.hasSearched && viewModel.results.isEmpty() -> SearchContentState.EMPTY
        viewModel.results.isNotEmpty() -> SearchContentState.RESULTS
        else -> SearchContentState.START
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .imePadding()
            .semantics { isTraversalGroup = true },
    ) {
        val searchAreaHeight = 84.dp
        val searchOffset by animateDpAsState(
            targetValue = if (searchActive) 0.dp
            else (maxHeight - searchAreaHeight).coerceAtLeast(0.dp),
            animationSpec = searchMotion,
            label = "search-position",
        )
        val contentTopPadding by animateDpAsState(
            targetValue = if (searchActive) searchAreaHeight else 0.dp,
            animationSpec = searchMotion,
            label = "search-content-top-padding",
        )
        val contentBottomPadding by animateDpAsState(
            targetValue = if (searchActive) 0.dp else searchAreaHeight,
            animationSpec = searchMotion,
            label = "search-content-bottom-padding",
        )

        AnimatedContent(
            targetState = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = contentTopPadding.coerceIn(0.dp, searchAreaHeight),
                    bottom = contentBottomPadding.coerceIn(0.dp, searchAreaHeight),
                ),
            transitionSpec = {
                (fadeIn() + slideInVertically { it / 12 }) togetherWith
                    (fadeOut() + slideOutVertically { -it / 12 })
            },
            label = "search-content",
        ) { contentState ->
            when (contentState) {
                SearchContentState.START -> SearchMessage(
                    icon = R.drawable.search_24px,
                    title = when (searchType) {
                        SearchType.MOVIE -> localized("Encuentra tu próxima película", "Find your next movie")
                        SearchType.TV -> localized("Encuentra tu próxima serie", "Find your next TV show")
                        else -> localized("¿Qué quieres ver?", "What do you want to watch?")
                    },
                    description = localized(
                        "Escribe al menos dos letras; Visto buscará automáticamente en TMDB.",
                        "Type at least two characters; Visto will search TMDB automatically.",
                    ),
                )

                SearchContentState.LOADING -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator(modifier = Modifier.size(52.dp))
                }

                SearchContentState.ERROR -> SearchMessage(
                    icon = R.drawable.refresh_24px,
                    title = localized("No se pudo completar la búsqueda", "Search could not be completed"),
                    description = localized(
                        "Comprueba la conexión e inténtalo otra vez.",
                        "Check your connection and try again.",
                    ),
                    action = {
                        Button(onClick = { viewModel.search(searchType) }) {
                            Text(localized("Reintentar", "Try again"))
                        }
                    },
                )

                SearchContentState.EMPTY -> SearchMessage(
                    icon = R.drawable.search_24px,
                    title = localized("No hay resultados", "No results"),
                    description = localized(
                        "Prueba con otro título o revisa la ortografía.",
                        "Try another title or check the spelling.",
                    ),
                )

                SearchContentState.RESULTS -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    item {
                        Text(
                            text = localized(
                                "${viewModel.results.size} resultados",
                                "${viewModel.results.size} results",
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                        )
                    }
                    itemsIndexed(
                        items = viewModel.results,
                        key = { _, item -> "${item.mediaType}-${item.id}" },
                    ) { index, item ->
                        SearchRow(
                            item = item,
                            index = index,
                            results = viewModel.results,
                            modifier = Modifier.animateItem(),
                            onSearchItemClick = {
                                scope.launch {
                                    viewModel.onSearchItemClick(item, watchlistViewModel)
                                }
                            },
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = searchOffset),
        ) {
            VistoSearchField(
                viewModel = viewModel,
                searchType = searchType,
                active = searchActive,
                onActivate = { searchActive = true },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VistoSearchField(
    viewModel: SearchViewModel,
    searchType: SearchType,
    active: Boolean,
    onActivate: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    // La profundidad se expresa por tono, no con borde ni sombra: es lo que hace Material 3
    // Expressive con los search bars y lo que pide docs/DESIGN_SYSTEM.md. Al activarse, el
    // campo sube un escalón de superficie en lugar de dibujar un aro de color.
    val containerColor by animateColorAsState(
        targetValue = if (active) {
            MaterialTheme.colorScheme.surfaceContainerHighest
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "search-field-container",
    )
    val leadingIconColor by animateColorAsState(
        targetValue = if (active) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "search-field-leading-icon",
    )
    // El color de contenido se anima en paralelo al del contenedor. No se deriva con
    // contentColorFor porque los valores intermedios de la animación no coinciden con ningún
    // rol del esquema y devolvería Unspecified.
    val leadingIconContentColor by animateColorAsState(
        targetValue = if (active) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "search-field-leading-icon-content",
    )
    val placeholder = when (searchType) {
        SearchType.MOVIE -> localized("Título de la película…", "Movie title…")
        SearchType.TV -> localized("Título de la serie…", "TV show title…")
        SearchType.PERSON -> localized("Nombre de la persona…", "Person name…")
        SearchType.MULTI -> localized("Película o serie…", "Movie or TV show…")
    }

    LaunchedEffect(active) {
        if (active) {
            delay(90)
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        TextField(
            value = viewModel.query,
            onValueChange = { viewModel.onQueryChange(it, searchType) },
            modifier = Modifier
                .fillMaxWidth()
                .height(SearchBarDefaults.InputFieldHeight)
                .focusProperties { canFocus = active }
                .focusRequester(focusRequester)
                .semantics { traversalIndex = -1f },
            readOnly = !active,
            singleLine = true,
            shape = SearchBarDefaults.inputFieldShape,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                )
            },
            leadingIcon = {
                Surface(
                    color = leadingIconColor,
                    shape = CircleShape,
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Symbol(
                            icon = R.drawable.search_24px,
                            color = leadingIconContentColor,
                            size = 20.dp,
                        )
                    }
                }
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedVisibility(
                        visible = viewModel.loading,
                        enter = fadeIn(motionScheme.fastEffectsSpec()) +
                            scaleIn(motionScheme.fastSpatialSpec()),
                        exit = fadeOut(motionScheme.fastEffectsSpec()) +
                            scaleOut(motionScheme.fastSpatialSpec()),
                    ) {
                        LoadingIndicator(modifier = Modifier.size(24.dp))
                    }
                    AnimatedVisibility(
                        visible = viewModel.query.isNotEmpty(),
                        enter = fadeIn(motionScheme.fastEffectsSpec()) +
                            scaleIn(motionScheme.fastSpatialSpec()),
                        exit = fadeOut(motionScheme.fastEffectsSpec()) +
                            scaleOut(motionScheme.fastSpatialSpec()),
                    ) {
                        IconButton(onClick = {
                            viewModel.onQueryChange("", searchType)
                            focusRequester.requestFocus()
                            keyboard?.show()
                        }) {
                            Symbol(
                                icon = R.drawable.close_24px,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    viewModel.search(searchType)
                    focusManager.clearFocus()
                    keyboard?.hide()
                },
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                disabledContainerColor = containerColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
        )

        if (!active) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(SearchBarDefaults.inputFieldShape)
                    .clickable(role = Role.Button, onClick = onActivate)
                    .semantics {
                        contentDescription = placeholder
                        traversalIndex = -1f
                    },
            )
        }
    }
}

@Composable
private fun SearchMessage(
    icon: Int,
    title: String,
    description: String,
    action: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Symbol(
                icon = icon,
                size = 48.dp,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            action?.invoke()
        }
    }
}
