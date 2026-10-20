package eu.hxreborn.amznkiller.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import eu.hxreborn.amznkiller.ui.theme.Tokens

fun shapeForPosition(
    count: Int,
    index: Int,
): RoundedCornerShape {
    val large = Tokens.GroupedShapeCornerLarge
    val small = Tokens.GroupedShapeCornerSmall
    return when {
        count == 1 -> {
            RoundedCornerShape(large)
        }

        index == 0 -> {
            RoundedCornerShape(
                topStart = large,
                topEnd = large,
                bottomEnd = small,
                bottomStart = small,
            )
        }

        index == count - 1 -> {
            RoundedCornerShape(
                topStart = small,
                topEnd = small,
                bottomEnd = large,
                bottomStart = large,
            )
        }

        else -> {
            RoundedCornerShape(small)
        }
    }
}

fun Modifier.groupedItem(
    color: Color,
    count: Int,
    index: Int,
): Modifier = background(color = color, shape = shapeForPosition(count, index))
