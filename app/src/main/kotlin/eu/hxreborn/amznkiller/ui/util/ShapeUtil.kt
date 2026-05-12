package eu.hxreborn.amznkiller.ui.util

import androidx.compose.foundation.shape.RoundedCornerShape
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
