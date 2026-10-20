package eu.hxreborn.amznkiller.ui.screen.update

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.update.Release

private val NoteIndent = 14.dp
private val ExpandIconSize = 20.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateCard(
    release: Release,
    onLater: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    var expanded by rememberSaveable { mutableStateOf(false) }
    val total = release.notesNew.size + release.notesFixed.size + release.notesOther.size
    Surface(
        shape = MaterialTheme.shapes.largeIncreased,
        color = scheme.primaryContainer,
        contentColor = scheme.onPrimaryContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.SystemUpdate, contentDescription = null)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.update_card_title, release.versionName),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                    )
                    if (expanded) {
                        NoteGroup(stringResource(R.string.notes_new), release.notesNew)
                        NoteGroup(stringResource(R.string.notes_fixed), release.notesFixed)
                        NoteGroup(stringResource(R.string.notes_other), release.notesOther)
                    }
                    if (total > 0) {
                        Row(
                            Modifier.heightIn(min = 48.dp).clickable(role = Role.Button) { expanded = !expanded },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                if (expanded) {
                                    stringResource(R.string.notes_less)
                                } else {
                                    pluralStringResource(R.plurals.notes_count, total, total)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Icon(
                                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(ExpandIconSize),
                            )
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                TextButton(onClick = onLater, shapes = ButtonDefaults.shapes()) {
                    Text(stringResource(R.string.update_later), color = scheme.onPrimaryContainer)
                }
                Button(
                    onClick = onUpdate,
                    shapes = ButtonDefaults.shapes(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = scheme.onPrimaryContainer,
                            contentColor = scheme.primaryContainer,
                        ),
                ) {
                    Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.update))
                }
            }
        }
    }
}

@Composable
private fun NoteGroup(
    title: String,
    lines: List<String>,
) {
    if (lines.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium)
        lines.forEach { NoteLine(it) }
    }
}

@Composable
internal fun NoteLine(line: String) {
    Row {
        Text(
            "•",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(NoteIndent).clearAndSetSemantics { },
        )
        Text(line.replaceFirstChar(Char::uppercase), style = MaterialTheme.typography.bodyMedium)
    }
}

internal val previewRelease =
    Release(
        versionName = "2.6.0",
        versionCode = Release.versionCodeOf("2.6.0"),
        publishedAt = "2026-10-20T10:00:00Z",
        url = "https://github.com/hxreborn/amznkiller/releases/tag/v2.6.0",
        notes =
            Release.parseNotes(
                """
                ### Features
                - [`ae38ba3`](https://github.com/hxreborn/amznkiller/commit/ae38ba3) *(ui)* Install updates from the app
                ### Bug Fixes
                - [`212ea59`](https://github.com/hxreborn/amznkiller/commit/212ea59) *(hook)* Apply force dark to the bottom tab bar
                ### Dependencies
                - [`55e063d`](https://github.com/hxreborn/amznkiller/commit/55e063d) *(deps)* Bump navigation3 by @hxreborn
                """.trimIndent(),
            ),
        asset = Release.Asset("amznkiller-v2.6.0-release.apk", 3_032_618, "4b45d372", ""),
    )

@PreviewLightDark
@Composable
private fun UpdateCardPreview() {
    PreviewWrapper {
        UpdateCard(previewRelease, onLater = {}, onUpdate = {}, modifier = Modifier.padding(8.dp))
    }
}
