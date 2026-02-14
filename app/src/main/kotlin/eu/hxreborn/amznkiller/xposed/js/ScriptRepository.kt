package eu.hxreborn.amznkiller.xposed.js

import eu.hxreborn.amznkiller.util.Logger

object ScriptRepository {
    private val cache = mutableMapOf<ScriptId, String>()

    fun get(id: ScriptId): String =
        cache.getOrPut(id) {
            runCatching {
                javaClass.classLoader!!
                    .getResourceAsStream(id.path)!!
                    .bufferedReader()
                    .readText()
            }.onFailure {
                Logger.log("ScriptRepository: failed to load ${id.path}", it)
            }.getOrDefault("")
        }
}
