package eu.hxreborn.amznkiller.ui.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween

const val FILL_SLOW_TARGET = 0.95f
const val FILL_MIN_TARGET = 0.5f
const val BUTTON_UNLOCK_THRESHOLD = 0.95f

fun fillSlowSpec() = tween<Float>(6000, easing = LinearOutSlowInEasing)

fun fillCatchUpSpec(remainingMs: Int) = tween<Float>(remainingMs.coerceIn(300, 1500), easing = LinearOutSlowInEasing)

fun fillFinishSpec() = tween<Float>(500, easing = FastOutSlowInEasing)

fun fillDrainSpec(durationMs: Int) = tween<Float>(durationMs.coerceIn(200, 500), easing = FastOutSlowInEasing)

fun waveCycleSpec() = tween<Float>(1500, easing = LinearEasing)
