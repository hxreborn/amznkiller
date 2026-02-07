package eu.hxreborn.amznkiller.ui.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import eu.hxreborn.amznkiller.ui.theme.AppTheme
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig

@Composable
fun PreviewWrapper(content: @Composable () -> Unit) {
    AppTheme(
        darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
        useDynamicColor = false,
        content = content,
    )
}

@Preview(
    name = "Light",
    showBackground = true,
    backgroundColor = 0xFFFEF7FF,
)
@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF141218,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class PreviewLightDark
