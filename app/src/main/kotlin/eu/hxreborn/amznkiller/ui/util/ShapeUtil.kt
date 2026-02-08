package eu.hxreborn.amznkiller.ui.util

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val CORNER_LARGE = 24.dp
private val CORNER_SMALL = 4.dp

fun shapeForPosition(
    count: Int,
    index: Int,
): RoundedCornerShape =
    when {
        count == 1 -> {
            RoundedCornerShape(CORNER_LARGE)
        }

        index == 0 -> {
            RoundedCornerShape(CORNER_LARGE, CORNER_LARGE, CORNER_SMALL, CORNER_SMALL)
        }

        index == count - 1 -> {
            RoundedCornerShape(CORNER_SMALL, CORNER_SMALL, CORNER_LARGE, CORNER_LARGE)
        }

        else -> {
            RoundedCornerShape(CORNER_SMALL)
        }
    }
