package eu.hxreborn.amznkiller.ui.animation

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import kotlin.math.sin

fun Modifier.waveFill(
    fillLevel: Float,
    wavePhase: Float,
): Modifier =
    drawWithCache {
        val wavePath = Path()
        val waveAmplitude = 4.dp.toPx() * (1f - fillLevel)
        val waterY = size.height * (1f - fillLevel)

        wavePath.moveTo(0f, waterY)
        var x = 0f
        while (x <= size.width) {
            wavePath.lineTo(x, waterY + sin(x * 0.08f + wavePhase) * waveAmplitude)
            x += 5f
        }
        wavePath.lineTo(size.width, size.height)
        wavePath.lineTo(0f, size.height)
        wavePath.close()

        onDrawWithContent {
            clipPath(wavePath) {
                this@onDrawWithContent.drawContent()
            }
        }
    }
