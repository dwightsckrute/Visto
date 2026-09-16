package com.dwightsckrute.visto.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius

/** La marca de Atalaya Software, aislada del tema para conservar sus colores oficiales. */
@Composable
fun AtalayaBrand(
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShapeRadius.Large),
        color = AtalayaBrandColors.background,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.atalaya_mark),
                contentDescription = null,
                colorFilter = ColorFilter.tint(AtalayaBrandColors.foreground),
                modifier = Modifier.height(height),
                contentScale = ContentScale.FillHeight,
            )
            Image(
                painter = painterResource(R.drawable.atalaya_wordmark),
                contentDescription = "Atalaya Software",
                modifier = Modifier.height(height * WORDMARK_RATIO),
                contentScale = ContentScale.FillHeight,
            )
        }
    }
}

object AtalayaBrandColors {
    val background = Color(0xFF171713)
    val foreground = Color(0xFFF7F5EF)
}

private const val WORDMARK_RATIO = 0.78f
