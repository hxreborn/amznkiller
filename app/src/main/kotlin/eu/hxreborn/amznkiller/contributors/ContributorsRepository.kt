package eu.hxreborn.amznkiller.contributors

import android.content.Context
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private const val SNAPSHOT = "contributors.json"
const val DIFF_BLOCK_COUNT = 5

@Immutable
data class ContributorRow(
    val login: String,
    val owner: Boolean,
    val url: String,
    val added: Int,
    val removed: Int,
    val greenBlocks: Int,
    val redBlocks: Int,
    val avatar: ImageBitmap?,
)

class ContributorsRepository(
    private val context: Context,
) {
    suspend fun load(): List<ContributorRow> =
        withContext(Dispatchers.IO) {
            val assets = context.assets
            val json = assets.open(SNAPSHOT).use { JSONArray(it.readBytes().decodeToString()) }
            val entries = List(json.length()) { Entry(json.getJSONObject(it)) }
            entries.map { entry ->
                val avatar = decodeAvatar(assets, entry.avatarPath)
                val (green, red) = blocks(entry.added, entry.removed)
                ContributorRow(
                    login = entry.login,
                    owner = entry.owner,
                    url = entry.url,
                    added = entry.added,
                    removed = entry.removed,
                    greenBlocks = green,
                    redBlocks = red,
                    avatar = avatar,
                )
            }
        }

    private fun decodeAvatar(
        assets: AssetManager,
        path: String,
    ): ImageBitmap? =
        try {
            assets.open(path).use(BitmapFactory::decodeStream)?.asImageBitmap()
        } catch (_: IOException) {
            null
        }

    private class Entry(
        item: JSONObject,
    ) {
        val login: String = item.getString("login")
        val owner: Boolean = item.optBoolean("owner")
        val url: String = item.getString("url")
        val added: Int = item.getInt("added")
        val removed: Int = item.getInt("removed")
        val avatarPath: String = item.optString("avatar")
    }
}

internal fun blocks(
    added: Int,
    removed: Int,
): Pair<Int, Int> {
    val total = added + removed
    if (total == 0) return 0 to 0
    return DIFF_BLOCK_COUNT * added / total to DIFF_BLOCK_COUNT * removed / total
}
