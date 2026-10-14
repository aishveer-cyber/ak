package com.rui.app.data

import java.util.UUID

enum class Role { User, Assistant }

data class Message(
    val id: Long,
    val role: Role,
    val text: String,
    val streaming: Boolean = false,
    val attachName: String? = null,
    val attachText: String? = null,
) {
    /** What the model actually sees for a user turn (file content first, then the question). */
    fun promptText(): String =
        if (attachText != null) "File \"$attachName\":\n$attachText\n\n$text" else text
}

data class Chat(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val messages: List<Message> = emptyList(),
)
