package eu.hxreborn.amznkiller.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs
import kotlin.math.min

private const val MAX_HARMONIZE_DEGREES = 15f
private const val HALF_TURN = 180f
private const val FULL_TURN = 360f

@Immutable
class ExtendedColors(
    val diffAdded: Color,
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors(diffAddedLight) }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable get() = LocalExtendedColors.current

fun extendedColors(
    darkTheme: Boolean,
    primary: Color,
): ExtendedColors = ExtendedColors(diffAdded = harmonize(if (darkTheme) diffAddedDark else diffAddedLight, primary))

private fun harmonize(
    source: Color,
    target: Color,
): Color {
    val from = FloatArray(3).also { android.graphics.Color.colorToHSV(source.toArgb(), it) }
    val to = FloatArray(3).also { android.graphics.Color.colorToHSV(target.toArgb(), it) }
    val difference = ((to[0] - from[0] + FULL_TURN + HALF_TURN) % FULL_TURN) - HALF_TURN
    val shift = min(abs(difference) * 0.5f, MAX_HARMONIZE_DEGREES) * if (difference < 0f) -1f else 1f
    from[0] = (from[0] + shift + FULL_TURN) % FULL_TURN
    return Color(android.graphics.Color.HSVToColor(from))
}
