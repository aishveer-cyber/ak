package com.rui.app

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rui.app.data.Chat
import com.rui.app.data.ChatStore
import com.rui.app.data.Message
import com.rui.app.data.Role
import com.rui.app.engine.ChatTemplate
import com.rui.app.engine.DeviceStats
import com.rui.app.engine.EngineState
import com.rui.app.engine.LlmEngine
import com.rui.app.engine.LocalModel
import com.rui.app.engine.MediaPipeLlmEngine
import com.rui.app.engine.ModelCatalog
import com.rui.app.engine.ModelDownloader
import com.rui.app.engine.ModelStore
import com.rui.app.engine.Speaker
import com.rui.app.engine.VoiceInput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DownloadUi(
    val active: Boolean = false,
    val done: Long = 0,
    val total: Long = 0,
    val error: String? = null,
    val needsWifiConfirm: Boolean = false,
)

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    private val engine: LlmEngine = MediaPipeLlmEngine(app)
    private val store = ModelStore(app)
    private val downloader = ModelDownloader(app)
    private val chatStore = ChatStore(app)
    private val speaker = Speaker(app)
    private val voice = VoiceInput(app)
    private val prefs = app.getSharedPreferences("rui", Context.MODE_PRIVATE)

    val engineState: StateFlow<EngineState> = engine.state

    private val _chats = MutableStateFlow(chatStore.load())
    val chats = _chats.asStateFlow()
    private val _currentId = MutableStateFlow<String?>(null)

    val messages: StateFlow<List<Message>> =
        combine(_chats, _currentId) { chats, id -> chats.firstOrNull { it.id == id }?.messages ?: emptyList() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val input = MutableStateFlow("")
    val thinkOn = MutableStateFlow(true)
    val voiceOn = MutableStateFlow(false)
    val listening = MutableStateFlow(false)
    val attachment = MutableStateFlow<Pair<String, String>?>(null)
    val instructions = MutableStateFlow(prefs.getString("instructions", "") ?: "")

    private val _models = MutableStateFlow(store.list())
    val models = _models.asStateFlow()
    private val _stats = MutableStateFlow(store.deviceStats())
    val stats: StateFlow<DeviceStats> = _stats.asStateFlow()
    val importProgress = MutableStateFlow<Float?>(null)
    val loadedModel = MutableStateFlow<String?>(null)
    val download = MutableStateFlow(DownloadUi())

    private val _notice = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val notice = _notice.asSharedFlow()

    private var genJob: Job? = null
    private var importJob: Job? = null
    private var downloadJob: Job? = null
    private var sessionChatId: String? = null
    private var replyAloud = false
    private var nextId = (_chats.value.flatMap { it.messages }.maxOfOrNull { it.id } ?: 0L) + 1

    val isGenerating: Boolean get() = genJob?.isActive == true

    init {
        if (_models.value.isEmpty()) startDownload(force = false) else autoLoad()
    }

    fun postNotice(msg: String) { _notice.tryEmit(msg) }

    // ---- First run: download + load -------------------------------------------------------

    private fun onWifi(): Boolean {
        val cm = getApplication<Application>().getSystemService(ConnectivityManager::class.java)
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    /** Starts automatically on Wi-Fi; on mobile data it waits for the user's OK (the file is ~1.7 GB). */
    fun startDownload(force: Boolean = true) {
        if (download.value.active) return
        if (!force && !onWifi()) {
            download.value = DownloadUi(needsWifiConfirm = true)
            return
        }
        download.value = DownloadUi(active = true)
        downloadJob = viewModelScope.launch {
            var last = 0L
            val result = downloader.download(ModelCatalog.DEFAULT) { done, total ->
                val now = SystemClock.uptimeMillis()
                if (now - last > 400) { last = now; download.value = DownloadUi(active = true, done = done, total = total) }
            }
            result.onSuccess {
                download.value = DownloadUi()
                _models.value = store.list()
                refreshStats()
                autoLoad()
            }.onFailure {
                if (it is CancellationException) { download.value = DownloadUi(); throw it }
                download.value = DownloadUi(error = it.message ?: "Download failed. Check your connection and try again.")
            }
        }
    }

    fun cancelDownload() { downloadJob?.cancel() }

    private fun autoLoad() {
        val last = prefs.getString("last_model", null)
        val m = _models.value.firstOrNull { it.name == last } ?: _models.value.firstOrNull() ?: return
        loadModel(m)
    }

    // ---- Chats ---------------------------------------------------------------------------

    fun newChat() { stopGenerating(); stopListening(); _currentId.value = null; input.value = ""; attachment.value = null }

    fun openChat(id: String) { stopGenerating(); _currentId.value = id }

    fun deleteChat(id: String) {
        _chats.update { list -> list.filterNot { it.id == id } }
        if (_currentId.value == id) _currentId.value = null
        persist()
    }

    private fun persist() {
        val snapshot = _chats.value
        viewModelScope.launch(Dispatchers.IO) { chatStore.save(snapshot) }
    }

    fun saveInstructions(text: String) {
        instructions.value = text.trim()
        prefs.edit().putString("instructions", instructions.value).apply()
        sessionChatId = null // apply to the next message
    }

    // ---- Attach a text/code file ----------------------------------------------------------

    fun attachFile(uri: Uri) {
        viewModelScope.launch {
            val r = withContext(Dispatchers.IO) {
                runCatching {
                    val cr = getApplication<Application>().contentResolver
                    var name = "file.txt"
                    cr.query(uri, null, null, null, null)?.use { c ->
                        val i = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (c.moveToFirst() && i >= 0) name = c.getString(i)
                    }
                    val text = cr.openInputStream(uri)!!.bufferedReader().use { it.readText() }
                    require(text.isNotBlank()) { "That file has no readable text." }
                    name to text
                }
            }
            r.onSuccess { (name, text) ->
                val limit = 6000
                attachment.value = name to text.take(limit)
                if (text.length > limit) postNotice("Long file: only the first $limit characters are used (the model has a small memory).")
            }.onFailure { postNotice("Only text or code files can be attached.") }
        }
    }

    // ---- Voice ----------------------------------------------------------------------------

    fun startListening() {
        if (!voice.available()) { postNotice("Speech recognition isn't available on this phone."); return }
        speaker.stop()
        listening.value = true
        voice.start(
            onPartial = { input.value = it },
            onFinal = { text ->
                listening.value = false
                input.value = text
                if (text.isNotBlank()) { replyAloud = true; send(text) }
            },
            onError = { listening.value = false; postNotice(it) },
        )
    }

    fun stopListening() { voice.stop(); listening.value = false }

    fun speak(text: String) = speaker.speak(text)

    // ---- Sending --------------------------------------------------------------------------

    fun send(text: String = input.value) {
        val q = text.trim()
        val att = attachment.value
        if ((q.isEmpty() && att == null) || isGenerating) return
        if (!requireModel()) return

        val chatId = _currentId.value ?: Chat(title = (q.ifEmpty { att?.first ?: "New chat" }).take(40)).also { c ->
            _chats.update { it + c }
            _currentId.value = c.id
        }.id
        val history = _chats.value.first { it.id == chatId }.messages
        val userMsg = Message(nextId++, Role.User, q, attachName = att?.first, attachText = att?.second)
        append(chatId, userMsg)
        input.value = ""
        attachment.value = null
        generate(chatId, history, userMsg, forceReplay = false)
    }

    /** Re-answers the last question with a fresh attempt. */
    fun regenerate() {
        if (isGenerating || !requireModel()) return
        val chatId = _currentId.value ?: return
        val msgs = _chats.value.firstOrNull { it.id == chatId }?.messages ?: return
        val idx = msgs.indexOfLast { it.role == Role.User }
        if (idx < 0) return
        _chats.update { list -> list.map { if (it.id == chatId) it.copy(messages = msgs.take(idx + 1)) else it } }
        generate(chatId, msgs.take(idx), msgs[idx], forceReplay = true)
    }

    private fun requireModel(): Boolean {
        if (engine.state.value is EngineState.Ready) return true
        postNotice(if (_models.value.isEmpty()) "Rui is still setting up its model." else "Load a model first (Models & storage).")
        return false
    }

    private fun generate(chatId: String, history: List<Message>, userMsg: Message, forceReplay: Boolean) {
        speaker.stop()
        val replay = forceReplay || sessionChatId != chatId
        if (replay) { engine.resetSession(); sessionChatId = chatId }
        val prompt = buildPrompt(history, userMsg, replay)

        val replyId = nextId++
        append(chatId, Message(replyId, Role.Assistant, "", streaming = true))
        val speakIt = replyAloud || voiceOn.value
        replyAloud = false

        genJob = viewModelScope.launch {
            val sb = StringBuilder()
            var last = 0L
            try {
                engine.generate(prompt).collect { piece ->
                    sb.append(piece)
                    val now = SystemClock.uptimeMillis() // ~20 UI updates/second keeps streaming smooth
                    if (now - last >= 50) { update(chatId, replyId, sb.toString(), true); last = now }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                sb.append("\n\n⚠️ ").append(t.message ?: "Generation failed")
                sessionChatId = null
            } finally {
                update(chatId, replyId, sb.toString(), false)
                persist()
            }
            if (speakIt) speaker.speak(sb.toString())
        }
    }

    fun stopGenerating() { genJob?.cancel(); speaker.stop() }

    private fun buildPrompt(history: List<Message>, userMsg: Message, replay: Boolean): String {
        val custom = instructions.value.takeIf { it.isNotBlank() }?.let { "\nThe user's custom instructions: $it" } ?: ""
        val system = "You are Rui, a friendly, helpful assistant and expert programmer who runs privately on the user's phone. " +
            "Answer clearly in markdown, use a few fitting emojis, and reply in the language the user writes in.$custom"
        val think = if (thinkOn.value) "\n\n(Reason step by step before answering.)" else ""
        val turns = buildList {
            if (replay) history.takeLast(8).filter { it.text.isNotBlank() || it.attachText != null }.forEach {
                add((if (it.role == Role.User) "user" else "model") to it.promptText().take(1500))
            }
            add("user" to userMsg.promptText() + think)
        }
        return ChatTemplate.build(loadedModel.value ?: "", system, turns, includeSystem = replay)
    }

    private fun append(chatId: String, m: Message) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(messages = it.messages + m, updatedAt = System.currentTimeMillis()) else it }
        }
        persist()
    }

    private fun update(chatId: String, msgId: Long, text: String, streaming: Boolean) = _chats.update { list ->
        list.map { c ->
            if (c.id != chatId) c
            else c.copy(messages = c.messages.map { if (it.id == msgId) it.copy(text = text, streaming = streaming) else it })
        }
    }

    // ---- Models --------------------------------------------------------------------------

    fun refreshStats() { _stats.value = store.deviceStats() }

    fun importModel(uri: Uri) {
        if (importJob?.isActive == true) return
        importJob = viewModelScope.launch {
            importProgress.value = 0f
            val result = store.import(uri) { importProgress.value = it }
            importProgress.value = null
            result.onSuccess { postNotice("Imported ${it.name}") }.onFailure { postNotice(it.message ?: "Import failed") }
            _models.value = store.list()
            refreshStats()
            if (result.isSuccess && engine.state.value !is EngineState.Ready) autoLoad()
        }
    }

    fun cancelImport() { importJob?.cancel() }

    fun loadModel(m: LocalModel) {
        refreshStats()
        if (m.estimatedRamBytes > _stats.value.availRam) {
            postNotice("Not enough free RAM for ${m.name}. Close other apps or choose a smaller model.")
            return
        }
        val maxTokens = Regex("ekv(\\d+)").find(m.name)?.groupValues?.get(1)?.toIntOrNull() ?: 2048
        viewModelScope.launch {
            engine.load(m.file.absolutePath, maxTokens)
            if (engine.state.value is EngineState.Ready) {
                loadedModel.value = m.name
                prefs.edit().putString("last_model", m.name).apply()
                sessionChatId = null
            } else (engine.state.value as? EngineState.Error)?.let { postNotice(it.message) }
            refreshStats()
        }
    }

    fun unloadModel() {
        stopGenerating()
        engine.unload()
        loadedModel.value = null
        refreshStats()
    }

    fun deleteModel(m: LocalModel) {
        if (loadedModel.value == m.name) unloadModel()
        store.delete(m)
        _models.value = store.list()
        refreshStats()
    }

    override fun onCleared() {
        voice.stop()
        speaker.shutdown()
        engine.unload()
    }
}
