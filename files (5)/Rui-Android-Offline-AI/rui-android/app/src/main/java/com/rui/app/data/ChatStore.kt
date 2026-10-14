package com.rui.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Saves chats as one JSON file in app-private storage so history survives restarts. */
class ChatStore(context: Context) {
    private val file = File(context.filesDir, "chats.json")

    fun load(): List<Chat> = runCatching {
        val arr = JSONArray(file.readText())
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val ms = o.getJSONArray("messages")
            Chat(
                id = o.getString("id"), title = o.getString("title"), updatedAt = o.getLong("updatedAt"),
                messages = (0 until ms.length()).map { j ->
                    val m = ms.getJSONObject(j)
                    Message(
                        id = m.getLong("id"), role = Role.valueOf(m.getString("role")), text = m.getString("text"),
                        attachName = m.optString("attachName").takeIf { it.isNotEmpty() },
                        attachText = m.optString("attachText").takeIf { it.isNotEmpty() },
                    )
                },
            )
        }
    }.getOrDefault(emptyList())

    fun save(chats: List<Chat>) {
        runCatching {
            val arr = JSONArray()
            chats.forEach { c ->
                arr.put(JSONObject().put("id", c.id).put("title", c.title).put("updatedAt", c.updatedAt).put(
                    "messages", JSONArray().also { ms ->
                        c.messages.filter { it.text.isNotBlank() || it.role == Role.User }.forEach { m ->
                            ms.put(JSONObject().put("id", m.id).put("role", m.role.name).put("text", m.text)
                                .put("attachName", m.attachName ?: "").put("attachText", m.attachText ?: ""))
                        }
                    }))
            }
            val tmp = File(file.path + ".tmp")
            tmp.writeText(arr.toString())
            tmp.renameTo(file)
        }
    }
}
