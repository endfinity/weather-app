package com.clearsky.weather.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.4f)
) {
    if (pageCount <= 1) return

    Row(
        modifier = modifier
            .padding(8.dp)
            .semantics {
                contentDescription = "Page ${currentPage + 1} of $pageCount"
            },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage

            val width by animateDpAsState(
                targetValue = if (isSelected) 18.dp else 6.dp,
                animationSpec = tween(300),
                label = "dot_width"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor,
                animationSpec = tween(300),
                label = "dot_color"
            )

            Box(
                modifier = Modifier
                    .width(width)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
