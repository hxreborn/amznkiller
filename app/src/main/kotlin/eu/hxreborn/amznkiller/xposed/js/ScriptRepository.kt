package eu.hxreborn.amznkiller.xposed.js

import eu.hxreborn.amznkiller.util.Logger

object ScriptRepository {
    private val cache = mutableMapOf<ScriptId, String>()

    fun get(id: ScriptId): String =
        cache.getOrPut(id) {
            runCatching {
                val loader =
                    checkNotNull(javaClass.classLoader) {
                        "Missing classLoader"
                    }
                val stream =
                    checkNotNull(loader.getResourceAsStream(id.path)) {
                        "Missing script: ${id.path}"
                    }
                stream.bufferedReader().readText()
            }.onFailure {
                Logger.log("ScriptRepository: failed to load ${id.path}", it)
            }.getOrDefault("")
        }
}
