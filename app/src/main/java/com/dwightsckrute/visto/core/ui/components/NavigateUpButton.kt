package com.dwightsckrute.visto.core.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.localization.localized

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NavigateUpBtn(navController: NavController) {
    Tooltip(
        "Volver",
        preferredPosition = TooltipAnchorPosition.Below,
        spacing = 10.dp
    ) {

        IconButton(
            onClick = { navController.popBackStack() }, shapes = IconButtonDefaults.shapes()
        ) {
            Symbol(
                R.drawable.arrow_back_24px,
                desc = localized("icono de ajustes", "back"),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

}