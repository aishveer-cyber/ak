package com.rui.app.engine

data class ModelSpec(
    val title: String,
    val fileName: String,
    val url: String,
    val approxBytes: Long,
)

/**
 * Qwen2.5 1.5B Instruct (Apache-2.0), public download, no sign-in needed.
 * Gemma models on Hugging Face need a login and license click, so they cannot be auto-downloaded.
 */
object ModelCatalog {
    val DEFAULT = ModelSpec(
        title = "Qwen2.5 1.5B Instruct",
        fileName = "Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.task",
        url = "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/main/" +
            "Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.task",
        approxBytes = 1_700_000_000L,
    )
}
