# Rui Android v2: all source files

Create each file at the path shown in its heading. Put rui_logo.png at app/src/main/res/drawable-nodpi/rui_logo.png (separate file). Also add .github/workflows/build.yml (see build.yml).

## settings.gradle.kts

```kotlin
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "Rui"
include(":app")

```

## build.gradle.kts

```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}

```

## gradle.properties

```text
org.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official

```

## app/build.gradle.kts

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.rui.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rui.app"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        // On-device LLM runtimes ship big native libs: keep 64-bit ARM only.
        ndk { abiFilters += "arm64-v8a" }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    // Never compress model files if you ever bundle one as an asset.
    androidResources { noCompress += listOf("task", "bin", "gguf", "tflite", "onnx") }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.01.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // On-device LLM (Gemma / Phi / Llama .task bundles). Runs fully offline.
    // Check the latest 0.10.x version on Maven Central before building.
    implementation("com.google.mediapipe:tasks-genai:0.10.24")
}

```

## app/proguard-rules.pro

```text
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**
-dontwarn com.google.protobuf.**

```

## app/src/main/AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!--
      INTERNET is used for ONE thing: the first-run model download (and only after that file is saved,
      Rui works fully offline). Chats, voice and AI never leave the phone.
    -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <!-- Lets TextToSpeech find the on-device voice engine on Android 11+ -->
    <queries>
        <intent><action android:name="android.intent.action.TTS_SERVICE" /></intent>
        <intent><action android:name="android.speech.RecognitionService" /></intent>
    </queries>

    <application
        android:allowBackup="false"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher"
        android:label="Rui"
        android:largeHeap="true"
        android:enableOnBackInvokedCallback="true"
        android:theme="@style/Theme.Rui">

        <!-- Needed for the MediaPipe GPU delegate on targetSdk 31+ -->
        <uses-native-library android:name="libOpenCL.so" android:required="false" />
        <uses-native-library android:name="libvndksupport.so" android:required="false" />

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize"
            android:configChanges="orientation|screenSize|keyboardHidden|uiMode">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>

```

## app/src/main/res/drawable/ic_launcher_foreground.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<inset xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/rui_logo"
    android:inset="16%" />

```

## app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/rui_icon_bg" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>

```

## app/src/main/res/values/strings.xml

```xml
<resources><string name="app_name">Rui</string></resources>

```

## app/src/main/res/values/themes.xml

```xml
<resources>
    <color name="rui_bg">#FF0A0A1C</color>
    <color name="rui_icon_bg">#FF05051A</color>
    <style name="Theme.Rui" parent="android:Theme.Material.NoActionBar">
        <item name="android:windowBackground">@color/rui_bg</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowLightStatusBar">false</item>
    </style>
</resources>

```

## app/src/main/res/xml/data_extraction_rules.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Models are huge: never include them in cloud backup or device transfer. -->
<data-extraction-rules>
    <cloud-backup><exclude domain="file" path="models/" /></cloud-backup>
    <device-transfer><exclude domain="file" path="models/" /></device-transfer>
</data-extraction-rules>

```

## app/src/main/java/com/rui/app/ChatViewModel.kt

```kotlin
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

```

## app/src/main/java/com/rui/app/MainActivity.kt

```kotlin
package com.rui.app

import android.content.ComponentCallbacks2
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.rui.app.ui.RuiApp
import com.rui.app.ui.theme.RuiTheme

class MainActivity : ComponentActivity() {
    private val vm: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RuiTheme { RuiApp(vm) } }
    }

    /** Free the model's RAM before Android kills the process (prevents OOM + lets the app resume fast). */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE ||
            level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL
        ) vm.unloadModel()
    }
}

```

## app/src/main/java/com/rui/app/data/Chat.kt

```kotlin
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

```

## app/src/main/java/com/rui/app/data/ChatStore.kt

```kotlin
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

```

## app/src/main/java/com/rui/app/engine/LlmEngine.kt

```kotlin
package com.rui.app.engine

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File

sealed interface EngineState {
    data object Idle : EngineState
    data object Loading : EngineState
    data class Ready(val modelName: String, val backend: String) : EngineState
    data object Generating : EngineState
    data class Error(val message: String) : EngineState
}

/** Swap this implementation to move to llama.cpp (GGUF) or ONNX Runtime without touching the UI. */
interface LlmEngine {
    val state: StateFlow<EngineState>
    suspend fun load(modelPath: String, maxTokens: Int)
    /** Emits partial text pieces as they are produced. Cancelling the collector stops generation. */
    fun generate(prompt: String): Flow<String>
    fun resetSession()
    fun unload()
}

/** Builds the prompt in the format each model family was trained on. */
object ChatTemplate {
    /** turns: role is "user" or "model". includeSystem: true only when a fresh session starts. */
    fun build(modelName: String, system: String, turns: List<Pair<String, String>>, includeSystem: Boolean): String {
        val sb = StringBuilder()
        if (modelName.contains("gemma", ignoreCase = true)) {
            turns.forEachIndexed { i, (role, text) ->
                val t = if (includeSystem && i == 0 && role == "user") "$system\n\n$text" else text
                sb.append("<start_of_turn>$role\n$t<end_of_turn>\n")
            }
            sb.append("<start_of_turn>model\n")
        } else { // ChatML: Qwen and many others
            if (includeSystem) sb.append("<|im_start|>system\n$system<|im_end|>\n")
            turns.forEach { (role, text) ->
                sb.append("<|im_start|>${if (role == "model") "assistant" else role}\n$text<|im_end|>\n")
            }
            sb.append("<|im_start|>assistant\n")
        }
        return sb.toString()
    }
}

class MediaPipeLlmEngine(private val context: Context) : LlmEngine {

    private val _state = MutableStateFlow<EngineState>(EngineState.Idle)
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    private val lock = Any()
    private var llm: LlmInference? = null
    private var session: LlmInferenceSession? = null
    private var backendName = "CPU"
    private var lastModelName = ""

    override suspend fun load(modelPath: String, maxTokens: Int) = withContext(Dispatchers.IO) {
        synchronized(lock) { releaseLocked() }
        _state.value = EngineState.Loading
        try {
            // Try the GPU delegate first, fall back to CPU if the device/driver rejects it.
            val inference = try {
                backendName = "GPU"
                create(modelPath, LlmInference.Backend.GPU, maxTokens)
            } catch (t: Throwable) {
                backendName = "CPU"
                create(modelPath, LlmInference.Backend.CPU, maxTokens)
            }
            synchronized(lock) {
                llm = inference
                session = newSession(inference)
            }
            lastModelName = File(modelPath).name
            _state.value = EngineState.Ready(lastModelName, backendName)
        } catch (oom: OutOfMemoryError) {
            synchronized(lock) { releaseLocked() }
            _state.value = EngineState.Error("Not enough memory for this model. Close other apps or pick a smaller model.")
        } catch (t: Throwable) {
            synchronized(lock) { releaseLocked() }
            _state.value = EngineState.Error(t.message ?: "Could not load the model")
        }
    }

    private fun create(path: String, backend: LlmInference.Backend, maxTokens: Int): LlmInference {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(path)
            .setMaxTokens(maxTokens)
            .setPreferredBackend(backend)
            .build()
        return LlmInference.createFromOptions(context, options)
    }

    private fun newSession(inference: LlmInference): LlmInferenceSession {
        val opts = LlmInferenceSession.LlmInferenceSessionOptions.builder()
            .setTopK(40)
            .setTopP(0.95f)
            .setTemperature(0.8f)
            .build()
        return LlmInferenceSession.createFromOptions(inference, opts)
    }

    override fun generate(prompt: String): Flow<String> = callbackFlow {
        val s = synchronized(lock) { session }
        if (s == null) {
            close(IllegalStateException("No model is loaded"))
            return@callbackFlow
        }
        _state.value = EngineState.Generating
        try {
            s.addQueryChunk(prompt)
            // Partial results arrive on MediaPipe's own thread, never on the main thread.
            s.generateResponseAsync { partial, done ->
                trySend(partial)
                if (done) close()
            }
        } catch (t: Throwable) {
            close(t)
        }
        awaitClose {
            // Runs on normal completion AND when the collector is cancelled (Stop button).
            runCatching { s.cancelGenerateResponseAsync() }
            _state.value = if (synchronized(lock) { llm } != null) {
                EngineState.Ready(lastModelName, backendName)
            } else EngineState.Idle
        }
    }

    override fun resetSession() = synchronized(lock) {
        val inference = llm ?: return@synchronized
        runCatching { session?.close() }
        session = newSession(inference)
    }

    override fun unload() {
        synchronized(lock) { releaseLocked() }
        _state.value = EngineState.Idle
    }

    private fun releaseLocked() {
        runCatching { session?.close() }
        runCatching { llm?.close() }
        session = null
        llm = null
    }
}

```

## app/src/main/java/com/rui/app/engine/ModelCatalog.kt

```kotlin
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

```

## app/src/main/java/com/rui/app/engine/ModelDownloader.kt

```kotlin
package com.rui.app.engine

import android.content.Context
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext

/** One-time model download with resume. After this, Rui never needs the internet again. */
class ModelDownloader(context: Context) {
    private val dir = File(context.filesDir, "models").apply { mkdirs() }

    suspend fun download(spec: ModelSpec, onProgress: (done: Long, total: Long) -> Unit): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val out = File(dir, spec.fileName)
                if (out.exists() && out.length() > 0) return@runCatching out
                val part = File(dir, spec.fileName + ".part")
                var existing = if (part.exists()) part.length() else 0L

                val conn = (URL(spec.url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Rui-Android")
                    if (existing > 0) setRequestProperty("Range", "bytes=$existing-")
                }
                try {
                    val code = conn.responseCode
                    require(code == 200 || code == 206) { "Download server answered $code. Try again later." }
                    val resuming = code == 206
                    if (!resuming) { existing = 0; part.delete() }
                    val remaining = conn.contentLengthLong
                    val total = if (remaining > 0) existing + remaining else spec.approxBytes
                    require(StatFs(dir.path).availableBytes > (total - existing) + 200L * 1024 * 1024) {
                        "Not enough free storage (need about ${(total shr 20)} MB)."
                    }
                    conn.inputStream.use { input ->
                        FileOutputStream(part, resuming).use { o ->
                            val buf = ByteArray(1 shl 18)
                            var done = existing
                            while (true) {
                                coroutineContext.ensureActive()
                                val n = input.read(buf)
                                if (n < 0) break
                                o.write(buf, 0, n)
                                done += n
                                onProgress(done, total)
                            }
                            o.fd.sync()
                        }
                    }
                } finally {
                    conn.disconnect()
                }
                check(part.length() > 50L * 1024 * 1024) { "Download was incomplete. Try again." }
                if (out.exists()) out.delete()
                check(part.renameTo(out)) { "Could not save the model." }
                out
            }
        }
}

```

## app/src/main/java/com/rui/app/engine/ModelStore.kt

```kotlin
package com.rui.app.engine

import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.StatFs
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.coroutineContext

data class LocalModel(val file: File) {
    val name: String get() = file.name
    val sizeBytes: Long get() = file.length()
    /** Weights + KV cache + runtime overhead. Rough, but good enough to warn before loading. */
    val estimatedRamBytes: Long get() = (sizeBytes * 1.2).toLong()
}

data class DeviceStats(
    val totalRam: Long,
    val availRam: Long,
    val storageTotal: Long,
    val storageFree: Long,
    val modelsBytes: Long,
)

/**
 * Models live in app-private storage (filesDir/models): no storage permission needed,
 * excluded from backup, removed automatically on uninstall.
 * They arrive through the system file picker (no network involved).
 */
class ModelStore(private val context: Context) {

    private val dir = File(context.filesDir, "models").apply { mkdirs() }
    private val allowed = setOf("task", "bin")

    fun list(): List<LocalModel> =
        dir.listFiles { f -> f.isFile && f.extension in allowed }
            ?.sortedBy { it.name }?.map(::LocalModel) ?: emptyList()

    fun delete(model: LocalModel) = model.file.delete()

    fun deviceStats(): DeviceStats {
        val am = context.getSystemService(ActivityManager::class.java)
        val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val sf = StatFs(dir.path)
        return DeviceStats(mi.totalMem, mi.availMem, sf.totalBytes, sf.availableBytes, list().sumOf { it.sizeBytes })
    }

    /** Streams the picked file into app storage with progress. Atomic: a .part file is renamed at the end. */
    suspend fun import(uri: Uri, onProgress: (Float) -> Unit): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val cr = context.contentResolver
            var name = "model.task"
            var size = -1L
            cr.query(uri, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val n = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val s = c.getColumnIndex(OpenableColumns.SIZE)
                    if (n >= 0) name = c.getString(n)
                    if (s >= 0) size = c.getLong(s)
                }
            }
            require(name.substringAfterLast('.', "").lowercase() in allowed) {
                "Unsupported file. Pick a .task model bundle."
            }
            if (size > 0) {
                require(StatFs(dir.path).availableBytes > size + 256L * 1024 * 1024) {
                    "Not enough free storage for this model."
                }
            }
            val tmp = File(dir, "$name.part")
            val out = File(dir, name)
            try {
                cr.openInputStream(uri)!!.use { input ->
                    FileOutputStream(tmp).use { o ->
                        val buf = ByteArray(1 shl 20)
                        var done = 0L
                        while (true) {
                            coroutineContext.ensureActive()
                            val n = input.read(buf)
                            if (n < 0) break
                            o.write(buf, 0, n)
                            done += n
                            if (size > 0) onProgress(done.toFloat() / size)
                        }
                        o.fd.sync()
                    }
                }
                if (out.exists()) out.delete()
                check(tmp.renameTo(out)) { "Could not save the model" }
                out
            } finally {
                if (tmp.exists()) tmp.delete()
            }
        }
    }
}

```

## app/src/main/java/com/rui/app/engine/Speaker.kt

```kotlin
package com.rui.app.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** On-device text-to-speech. Works offline when the system voice data is installed. */
class Speaker(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) tts.language = Locale.getDefault()
    }

    fun speak(markdown: String) {
        if (!ready) return
        val plain = markdown
            .replace(Regex("```[\\s\\S]*?```"), " code omitted. ")
            .replace(Regex("[*_`#>]"), "")
            .replace(Regex("[\\p{So}\\p{Cn}]"), "")
        tts.speak(plain, TextToSpeech.QUEUE_FLUSH, null, "rui")
    }

    fun stop() { tts.stop() }
    fun shutdown() { tts.stop(); tts.shutdown() }
}

```

## app/src/main/java/com/rui/app/engine/VoiceInput.kt

```kotlin
package com.rui.app.engine

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/** Speech-to-text. Uses the on-device recognizer when the phone has one. Call from the main thread. */
class VoiceInput(private val context: Context) {
    private var recognizer: SpeechRecognizer? = null

    fun available(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun start(onPartial: (String) -> Unit, onFinal: (String) -> Unit, onError: (String) -> Unit) {
        stop()
        val r = if (Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context))
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        else SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onPartialResults(b: Bundle?) {
                b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(onPartial)
            }
            override fun onResults(b: Bundle?) {
                val text = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                stop(); onFinal(text)
            }
            override fun onError(error: Int) {
                stop()
                onError(when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't catch that. Tap the mic and try again."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is needed."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT, SpeechRecognizer.ERROR_SERVER ->
                        "Speech recognition needs its voice pack. Install your language's offline speech pack in phone settings."
                    else -> "Voice input failed (code $error)."
                })
            }
            override fun onReadyForSpeech(p: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(t: Int, b: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        r.startListening(intent)
    }

    fun stop() {
        recognizer?.let { runCatching { it.stopListening() }; runCatching { it.destroy() } }
        recognizer = null
    }
}

```

## app/src/main/java/com/rui/app/ui/RuiApp.kt

```kotlin
package com.rui.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.rui.app.ChatViewModel
import com.rui.app.ui.screens.ChatScreen
import com.rui.app.ui.screens.ModelsScreen
import com.rui.app.ui.screens.Sidebar
import com.rui.app.ui.screens.SetupScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

enum class Screen { Chat, Models }

@Composable
fun RuiApp(vm: ChatViewModel) {
    val models by vm.models.collectAsStateWithLifecycle()
    if (models.isEmpty()) SetupScreen(vm) else MainContent(vm)
}

@Composable
private fun MainContent(vm: ChatViewModel) {
    var screen by rememberSaveable { mutableStateOf(Screen.Chat) }
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    BackHandler(screen != Screen.Chat) { screen = Screen.Chat }

    ModalNavigationDrawer(
        drawerState = drawer,
        gesturesEnabled = screen == Screen.Chat,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                Sidebar(
                    vm = vm,
                    onNewChat = { vm.newChat(); screen = Screen.Chat; scope.launch { drawer.close() } },
                    onOpenChat = { vm.openChat(it); screen = Screen.Chat; scope.launch { drawer.close() } },
                    onOpenModels = { screen = Screen.Models; scope.launch { drawer.close() } },
                )
            }
        },
    ) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                (fadeIn(tween(220)) + slideInHorizontally(tween(260)) { it / 14 }) togetherWith fadeOut(tween(120))
            },
            label = "screen",
        ) { s ->
            when (s) {
                Screen.Chat -> ChatScreen(vm, onMenu = { scope.launch { drawer.open() } }, onOpenModels = { screen = Screen.Models })
                Screen.Models -> ModelsScreen(vm, onBack = { screen = Screen.Chat })
            }
        }
    }
}

```

## app/src/main/java/com/rui/app/ui/components/Markdown.kt

```kotlin
package com.rui.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private sealed interface Block {
    data class Heading(val level: Int, val text: String) : Block
    data class Code(val lang: String, val code: String) : Block
    data class Bullet(val text: String, val indent: Int) : Block
    data class Numbered(val n: String, val text: String) : Block
    data class Quote(val text: String) : Block
    data class Para(val text: String) : Block
}

private fun parse(src: String): List<Block> {
    val out = mutableListOf<Block>()
    val lines = src.lines()
    var i = 0
    val para = StringBuilder()
    fun flush() { if (para.isNotBlank()) out += Block.Para(para.toString().trim()); para.clear() }
    while (i < lines.size) {
        val line = lines[i]
        val t = line.trim()
        when {
            t.startsWith("```") -> {                       // also handles a fence that is still streaming
                flush()
                val lang = t.removePrefix("```").trim()
                val code = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) { code.appendLine(lines[i]); i++ }
                out += Block.Code(lang, code.toString().trimEnd())
            }
            t.startsWith("#") -> {
                flush()
                val lvl = t.takeWhile { it == '#' }.length.coerceIn(1, 4)
                out += Block.Heading(lvl, t.dropWhile { it == '#' }.trim())
            }
            Regex("^[-*•]\\s+.*").matches(t) ->
                { flush(); out += Block.Bullet(t.drop(1).trim(), (line.length - line.trimStart().length) / 2) }
            Regex("^\\d+[.)]\\s+.*").matches(t) ->
                { flush(); out += Block.Numbered(t.takeWhile { it.isDigit() } + ".", t.dropWhile { it.isDigit() }.drop(1).trim()) }
            t.startsWith(">") -> { flush(); out += Block.Quote(t.removePrefix(">").trim()) }
            t.isEmpty() -> flush()
            else -> { if (para.isNotEmpty()) para.append(' '); para.append(t) }
        }
        i++
    }
    flush()
    return out
}

private fun inline(text: String, codeBg: Color): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        val c = text[i]
        if (text.startsWith("**", i)) {
            val e = text.indexOf("**", i + 2)
            if (e > 0) { withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(inline(text.substring(i + 2, e), codeBg)) }; i = e + 2; continue }
        }
        if (c == '`') {
            val e = text.indexOf('`', i + 1)
            if (e > 0) {
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = codeBg, fontSize = 13.sp)) { append(" ${text.substring(i + 1, e)} ") }
                i = e + 1; continue
            }
        }
        if ((c == '*' || c == '_') && i + 1 < text.length && text[i + 1] != ' ') {
            val e = text.indexOf(c, i + 1)
            if (e > 0 && text[e - 1] != ' ') {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(inline(text.substring(i + 1, e), codeBg)) }
                i = e + 1; continue
            }
        }
        append(c); i++
    }
}

/** Zero-dependency markdown renderer: headings, bold/italic, inline + fenced code, lists, quotes. */
@Composable
fun Markdown(text: String, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val blocks = remember(text) { parse(text) }
    val codeBg = cs.surfaceContainerHigh
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        blocks.forEach { b ->
            when (b) {
                is Block.Heading -> Text(
                    inline(b.text, codeBg),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = (24 - b.level * 2).sp),
                    color = cs.onSurface,
                )
                is Block.Para -> Text(inline(b.text, codeBg), style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
                is Block.Bullet -> Row(Modifier.padding(start = (b.indent * 14).dp)) {
                    Text("•", color = cs.primary, modifier = Modifier.width(18.dp))
                    Text(inline(b.text, codeBg), style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
                }
                is Block.Numbered -> Row {
                    Text(b.n, color = cs.primary, modifier = Modifier.width(26.dp))
                    Text(inline(b.text, codeBg), style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
                }
                is Block.Quote -> Row {
                    Column(Modifier.width(3.dp).background(cs.tertiary)) { Text(" ", fontSize = 18.sp) }
                    Text(inline(b.text, codeBg), Modifier.padding(start = 10.dp), color = cs.onSurfaceVariant, fontStyle = FontStyle.Italic)
                }
                is Block.Code -> CodeBlock(b.lang, b.code)
            }
        }
    }
}

@Composable
private fun CodeBlock(lang: String, code: String) {
    val cs = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF070714))) {
        Row(Modifier.fillMaxWidth().background(cs.surfaceContainer).padding(start = 12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(lang.ifBlank { "code" }, color = cs.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = { clipboard.setText(AnnotatedString(code)) }) { Text("Copy", fontSize = 12.sp) }
        }
        Text(
            code, fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFFD6DBFF),
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(12.dp),
        )
    }
}

```

## app/src/main/java/com/rui/app/ui/components/Shimmer.kt

```kotlin
package com.rui.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement

/** Animated sweep used while the model is "thinking" and no token has arrived yet. */
@Composable
fun Modifier.shimmer(): Modifier {
    val cs = MaterialTheme.colorScheme
    val t = rememberInfiniteTransition(label = "shimmer")
    val x by t.animateFloat(
        initialValue = -400f, targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "x",
    )
    return background(
        Brush.linearGradient(
            colors = listOf(cs.surfaceContainer, cs.primary.copy(alpha = 0.35f), cs.surfaceContainer),
            start = androidx.compose.ui.geometry.Offset(x, 0f),
            end = androidx.compose.ui.geometry.Offset(x + 400f, 0f),
        )
    )
}

@Composable
fun ShimmerLines(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(1f, 0.86f, 0.55f).forEach { w ->
            Box(Modifier.fillMaxWidth(w).height(14.dp).clip(RoundedCornerShape(7.dp)).shimmer())
        }
    }
}

```

## app/src/main/java/com/rui/app/ui/screens/ChatScreen.kt

```kotlin
package com.rui.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rui.app.ChatViewModel
import com.rui.app.R
import com.rui.app.data.Message
import com.rui.app.data.Role
import com.rui.app.engine.EngineState
import com.rui.app.ui.components.Markdown
import com.rui.app.ui.components.ShimmerLines
import com.rui.app.ui.theme.RuiAmber
import com.rui.app.ui.theme.RuiGreen
import com.rui.app.ui.theme.brandBrush
import java.time.LocalTime

@Composable
fun ChatScreen(vm: ChatViewModel, onMenu: () -> Unit, onOpenModels: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val messages by vm.messages.collectAsStateWithLifecycle()
    val input by vm.input.collectAsStateWithLifecycle()
    val engine by vm.engineState.collectAsStateWithLifecycle()
    val think by vm.thinkOn.collectAsStateWithLifecycle()
    val voice by vm.voiceOn.collectAsStateWithLifecycle()
    val listening by vm.listening.collectAsStateWithLifecycle()
    val attach by vm.attachment.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.notice.collect { snack.showSnackbar(it) } }

    val ctx = LocalContext.current
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) vm.startListening() else vm.postNotice("Allow the microphone to talk to Rui.")
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(vm::attachFile) }
    val ctl = ComposerCtl(
        listening = listening, attachName = attach?.first,
        onMic = {
            if (listening) vm.stopListening()
            else if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) vm.startListening()
            else micPermission.launch(Manifest.permission.RECORD_AUDIO)
        },
        onAttach = { filePicker.launch(arrayOf("text/*", "application/json", "application/xml", "application/octet-stream")) },
        onClearAttach = { vm.attachment.value = null },
    )

    val generating = engine is EngineState.Generating
    val status = when (val e = engine) {
        EngineState.Idle -> "No model loaded"
        EngineState.Loading -> "Loading model…"
        is EngineState.Ready -> "Ready"
        EngineState.Generating -> "Thinking…"
        is EngineState.Error -> e.message
    }

    Scaffold(
        containerColor = cs.background,
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            Column(Modifier.statusBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMenu) { Icon(Icons.Rounded.Menu, "Menu", tint = cs.onSurface) }
                    Text(status, color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = onOpenModels) { Icon(Icons.Rounded.Memory, "Models", tint = cs.secondary) }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TogglePill("Think: ${if (think) "On" else "Off"}") { vm.thinkOn.value = !think }
                    // The desktop "Web: Auto" pill becomes a fixed Offline badge: this build has no network permission.
                    TogglePill("Offline", icon = true, onClick = null)
                    TogglePill("Voice: ${if (voice) "On" else "Off"}") { vm.voiceOn.value = !voice }
                }
                if (engine is EngineState.Loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp), color = cs.primary)
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize().imePadding()) {
            if (messages.isEmpty()) {
                Welcome(
                    ctl = ctl, input = input, generating = generating, onInput = { vm.input.value = it },
                    onSend = { vm.send() }, onStop = vm::stopGenerating,
                    onPick = { vm.input.value = it },
                )
            } else {
                Column(Modifier.fillMaxSize()) {
                    MessageList(messages, generating, vm, Modifier.weight(1f))
                    Composer(
                        ctl = ctl, value = input, onValueChange = { vm.input.value = it }, generating = generating,
                        onSend = { vm.send() }, onStop = vm::stopGenerating,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TogglePill(label: String, icon: Boolean = false, onClick: (() -> Unit)?) {
    val cs = MaterialTheme.colorScheme
    val content: @Composable () -> Unit = {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon) { Icon(Icons.Rounded.WifiOff, null, Modifier.size(14.dp), tint = RuiGreen); Spacer(Modifier.width(6.dp)) }
            Text(label, color = cs.secondary, fontSize = 13.sp)
        }
    }
    if (onClick == null) Surface(shape = RoundedCornerShape(10.dp), color = cs.surfaceContainer, content = content)
    else Surface(onClick = onClick, shape = RoundedCornerShape(10.dp), color = cs.surfaceContainer, content = content)
}

// ---------------------------------------------------------------------------------------------
// Welcome (matches the desktop "Good afternoon!" screen)
// ---------------------------------------------------------------------------------------------

private data class QuickAction(val title: String, val sub: String, val starter: String)

private val quickActions = listOf(
    QuickAction("Smart Write", "Improve your text", "Improve this text: "),
    QuickAction("Explain", "Make it simple", "Explain in simple terms: "),
    QuickAction("Code", "Write and debug", "Write code for: "),
    QuickAction("Homework", "Study help", "Help me study: "),
    QuickAction("Plan", "Step by step", "Make a step-by-step plan for: "),
)

private data class Suggestion(val glyph: String, val tint: Color, val title: String, val sub: String, val starter: String)

private val suggestions = listOf(
    Suggestion("✦", RuiAmber, "Brainstorm ideas", "Get creative ideas for a project, story or gift.", "Brainstorm ideas for: "),
    Suggestion("≡", Color(0xFF4F6BFF), "Summarize text", "Paste a long text and get the key points.", "Summarize this text:\n"),
    Suggestion("✓", Color(0xFF9B5CF6), "Create a plan", "Step-by-step plan for your goals or homework.", "Create a plan for: "),
    Suggestion("★", RuiGreen, "Learn something new", "Explain any topic in simple terms.", "Teach me about: "),
)

@Composable
private fun Welcome(
    ctl: ComposerCtl, input: String, generating: Boolean,
    onInput: (String) -> Unit, onSend: () -> Unit, onStop: () -> Unit, onPick: (String) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val hour = remember { LocalTime.now().hour }
    val greeting = when { hour < 12 -> "Good morning!"; hour < 18 -> "Good afternoon!"; else -> "Good evening!" }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(28.dp))
        Image(painterResource(R.drawable.rui_logo), null, Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)))
        Spacer(Modifier.height(16.dp))
        Text(
            greeting, textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge.copy(brush = brandBrush()),
        )
        Spacer(Modifier.height(6.dp))
        Text("How can I help you today?", color = cs.onSurfaceVariant, fontSize = 18.sp)
        Spacer(Modifier.height(24.dp))
        Composer(ctl, input, onInput, generating, onSend, onStop, minHeight = 112.dp)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            quickActions.forEach { a ->
                Surface(onClick = { onPick(a.starter) }, shape = RoundedCornerShape(12.dp), color = cs.surfaceContainer) {
                    Column(Modifier.widthIn(min = 128.dp).padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text(a.title, color = cs.onSurface, fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Text(a.sub, color = cs.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("✦", color = cs.onSurface); Spacer(Modifier.width(8.dp))
            Text("Suggested for you", color = cs.onSurface, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
        }
        Spacer(Modifier.height(12.dp))

        // Two columns on wide screens (tablet/foldable), one on phones.
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val cols = if (maxWidth >= 560.dp) 2 else 1
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                suggestions.chunked(cols).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowItems.forEach { s -> SuggestionCard(s, Modifier.weight(1f)) { onPick(s.starter) } }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SuggestionCard(s: Suggestion, modifier: Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(16.dp), color = cs.surfaceContainer) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(CircleShape).background(s.tint.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                Text(s.glyph, color = s.tint, fontSize = 20.sp)
            }
            Column(Modifier.padding(start = 14.dp)) {
                Text(s.title, color = cs.onSurface, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                Text(s.sub, color = cs.onSurfaceVariant, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Composer: gradient-bordered input with round send / stop button
// ---------------------------------------------------------------------------------------------

class ComposerCtl(
    val listening: Boolean, val attachName: String?,
    val onMic: () -> Unit, val onAttach: () -> Unit, val onClearAttach: () -> Unit,
)

@Composable
fun Composer(
    ctl: ComposerCtl, value: String, onValueChange: (String) -> Unit, generating: Boolean,
    onSend: () -> Unit, onStop: () -> Unit,
    modifier: Modifier = Modifier, minHeight: Dp = 56.dp,
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier.fillMaxWidth().heightIn(min = minHeight).clip(shape)
            .background(cs.surfaceContainer).border(1.5.dp, brandBrush(), shape)
            .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 8.dp),
    ) {
        ctl.attachName?.let { name ->
            Surface(shape = RoundedCornerShape(10.dp), color = cs.surfaceContainerHigh, modifier = Modifier.padding(bottom = 8.dp)) {
                Row(Modifier.padding(start = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("📎 $name", color = cs.onSurface, fontSize = 13.sp)
                    IconButton(onClick = ctl.onClearAttach, modifier = Modifier.size(32.dp)) { Icon(Icons.Rounded.Close, "Remove", Modifier.size(16.dp), tint = cs.onSurfaceVariant) }
                }
            }
        }
        BasicTextField(
            value = value, onValueChange = onValueChange,
            textStyle = TextStyle(color = cs.onSurface, fontSize = 16.sp, lineHeight = 22.sp),
            cursorBrush = SolidColor(cs.primary),
            modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(if (ctl.listening) "Listening… speak now" else "Ask anything…", color = cs.onSurfaceVariant, fontSize = 16.sp)
                inner()
            },
        )
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = ctl.onAttach) { Icon(Icons.Rounded.AttachFile, "Attach a text or code file", tint = cs.onSurfaceVariant) }
            Spacer(Modifier.weight(1f))
            val hasText = value.isNotBlank() || ctl.attachName != null
            val showMic = !hasText && !generating
            val pulse by rememberInfiniteTransition(label = "mic").animateFloat(
                1f, if (ctl.listening) 1.18f else 1f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "p",
            )
            Box(
                Modifier.size(42.dp).scale(if (showMic) pulse else 1f).clip(CircleShape)
                    .background(brandBrush())
                    .clickable { if (generating) onStop() else if (showMic) ctl.onMic() else onSend() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    when { generating -> Icons.Rounded.Stop; showMic -> Icons.Rounded.Mic; else -> Icons.Rounded.ArrowUpward },
                    when { generating -> "Stop"; showMic -> "Speak"; else -> "Send" }, tint = Color.White,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Messages
// ---------------------------------------------------------------------------------------------

@Composable
private fun MessageList(messages: List<Message>, generating: Boolean, vm: ChatViewModel, modifier: Modifier) {
    val state = rememberLazyListState()
    val atBottom by remember { derivedStateOf { val l = state.layoutInfo; (l.visibleItemsInfo.lastOrNull()?.index ?: 0) >= l.totalItemsCount - 2 } }
    val lastLen = messages.lastOrNull()?.text?.length ?: 0
    // Follow the stream only while the reader is already at the bottom.
    LaunchedEffect(messages.size, lastLen) {
        if (atBottom && messages.isNotEmpty()) state.scrollToItem(messages.lastIndex)
    }
    LazyColumn(modifier, state = state, contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        items(messages, key = { it.id }) { m ->
            MessageRow(m, isLast = m.id == messages.last().id, generating = generating, vm = vm)
        }
    }
}

@Composable
private fun MessageRow(m: Message, isLast: Boolean, generating: Boolean, vm: ChatViewModel) {
    val cs = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    if (m.role == Role.User) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Surface(shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp), color = cs.primary, modifier = Modifier.widthIn(max = 320.dp)) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    m.attachName?.let { Text("📎 $it", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp) }
                    if (m.text.isNotBlank()) Text(m.text, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    } else {
        Row(Modifier.fillMaxWidth()) {
            Image(painterResource(R.drawable.rui_logo), null, Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).animateContentSize()) {
                if (m.text.isEmpty() && m.streaming) ShimmerLines(Modifier.padding(top = 6.dp)) else Markdown(m.text)
                if (!m.streaming && m.text.isNotBlank()) {
                    Row(Modifier.padding(top = 4.dp)) {
                        IconButton(onClick = { clipboard.setText(AnnotatedString(m.text)); vm.postNotice("Copied") }, Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.ContentCopy, "Copy", Modifier.size(18.dp), tint = cs.onSurfaceVariant)
                        }
                        IconButton(onClick = { vm.speak(m.text) }, Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.VolumeUp, "Read aloud", Modifier.size(18.dp), tint = cs.onSurfaceVariant)
                        }
                        if (isLast && !generating) IconButton(onClick = vm::regenerate, Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.Refresh, "Regenerate", Modifier.size(18.dp), tint = cs.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

```

## app/src/main/java/com/rui/app/ui/screens/ModelsScreen.kt

```kotlin
package com.rui.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rui.app.ChatViewModel
import com.rui.app.engine.EngineState
import com.rui.app.engine.LocalModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private fun Long.fmt(): String = when {
    this >= 1L shl 30 -> "%.1f GB".format(this / (1L shl 30).toDouble())
    this >= 1L shl 20 -> "%.0f MB".format(this / (1L shl 20).toDouble())
    else -> "$this B"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelsScreen(vm: ChatViewModel, onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val models by vm.models.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val engine by vm.engineState.collectAsStateWithLifecycle()
    val progress by vm.importProgress.collectAsStateWithLifecycle()
    val loaded by vm.loadedModel.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<LocalModel?>(null) }

    // Live RAM readout while this screen is visible.
    LaunchedEffect(Unit) { while (isActive) { vm.refreshStats(); delay(2000) } }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(vm::importModel) }

    Scaffold(
        containerColor = cs.background,
        topBar = {
            TopAppBar(
                title = { Text("Models & storage") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.background, titleContentColor = cs.onSurface, navigationIconContentColor = cs.onSurface),
            )
        },
    ) { pad ->
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Surface(shape = RoundedCornerShape(18.dp), color = cs.surfaceContainer) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Meter("Memory (RAM)", stats.totalRam - stats.availRam, stats.totalRam, "${stats.availRam.fmt()} free")
                        Meter("Storage", stats.storageTotal - stats.storageFree, stats.storageTotal, "${stats.storageFree.fmt()} free")
                        Text("Models on this device: ${stats.modelsBytes.fmt()}", color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { picker.launch(arrayOf("*/*")) }, enabled = progress == null, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Rounded.FileUpload, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Import model (.task)")
                    }
                    progress?.let { p ->
                        val anim by animateFloatAsState(p, label = "import")
                        LinearProgressIndicator(progress = { anim }, Modifier.fillMaxWidth())
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Copying… ${(p * 100).toInt()}%", color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
                            TextButton(onClick = vm::cancelImport) { Text("Cancel") }
                        }
                    }
                }
            }
            if (models.isEmpty()) item {
                Text("No models yet. Copy a .task model file to your phone (USB, SD card or Files app), then tap Import.", color = cs.onSurfaceVariant)
            }
            items(models, key = { it.name }) { m ->
                val isLoaded = loaded == m.name && engine !is EngineState.Idle
                val fits = m.estimatedRamBytes <= stats.availRam || isLoaded
                Surface(shape = RoundedCornerShape(18.dp), color = cs.surfaceContainer) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(m.name, color = cs.onSurface, style = MaterialTheme.typography.titleMedium)
                        Text("${m.sizeBytes.fmt()} on disk  ·  needs about ${m.estimatedRamBytes.fmt()} RAM",
                            color = if (fits) cs.onSurfaceVariant else cs.error, style = MaterialTheme.typography.bodyMedium)
                        if (!fits) Text("Not enough free RAM right now. Close other apps first.", color = cs.error, style = MaterialTheme.typography.labelSmall)
                        Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (isLoaded) FilledTonalButton(onClick = vm::unloadModel) { Text("Unload") }
                            else Button(onClick = { vm.loadModel(m) }, enabled = engine !is EngineState.Loading && engine !is EngineState.Generating) { Text("Load") }
                            if (isLoaded) (engine as? EngineState.Ready)?.let { Text("Running on ${it.backend}", color = cs.secondary, style = MaterialTheme.typography.labelSmall) }
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { toDelete = m }) { Icon(Icons.Rounded.Delete, "Delete", tint = cs.onSurfaceVariant) }
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { m ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete ${m.name}?") },
            text = { Text("This frees ${m.sizeBytes.fmt()}. You can import it again later.") },
            confirmButton = { TextButton(onClick = { vm.deleteModel(m); toDelete = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Meter(label: String, used: Long, total: Long, trailing: String) {
    val cs = MaterialTheme.colorScheme
    val frac = if (total > 0) (used.toFloat() / total).coerceIn(0f, 1f) else 0f
    val anim by animateFloatAsState(frac, label = label)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Text(label, color = cs.onSurface, modifier = Modifier.weight(1f))
            Text(trailing, color = cs.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { anim }, modifier = Modifier.fillMaxWidth().height(8.dp),
            color = if (frac > 0.85f) cs.error else cs.primary, trackColor = cs.surfaceContainerHigh,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
        Text("${used.fmt()} of ${total.fmt()} used", color = cs.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

```

## app/src/main/java/com/rui/app/ui/screens/SetupScreen.kt

```kotlin
package com.rui.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rui.app.ChatViewModel
import com.rui.app.R
import com.rui.app.ui.theme.brandBrush

/** First-run screen: downloads the AI model once, then Rui loads it and works offline. */
@Composable
fun SetupScreen(vm: ChatViewModel) {
    val cs = MaterialTheme.colorScheme
    val d by vm.download.collectAsStateWithLifecycle()
    val importing by vm.importProgress.collectAsStateWithLifecycle()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(vm::importModel) }

    // Keep the screen awake while the big download runs.
    val view = LocalView.current
    DisposableEffect(d.active) {
        view.keepScreenOn = d.active
        onDispose { view.keepScreenOn = false }
    }

    Column(
        Modifier.fillMaxSize().background(cs.background).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Image(painterResource(R.drawable.rui_logo), null, Modifier.size(104.dp).clip(RoundedCornerShape(28.dp)))
        Spacer(Modifier.height(22.dp))
        Text("Setting up Rui", style = MaterialTheme.typography.headlineLarge.copy(brush = brandBrush(), fontSize = 32.sp))
        Spacer(Modifier.height(10.dp))
        Text(
            "Rui downloads its AI brain once (about 1.7 GB). After that it works fully offline.",
            color = cs.onSurfaceVariant, textAlign = TextAlign.Center, fontSize = 16.sp,
        )
        Spacer(Modifier.height(28.dp))

        when {
            d.active -> {
                val frac = if (d.total > 0) (d.done.toFloat() / d.total).coerceIn(0f, 1f) else 0f
                val anim by animateFloatAsState(frac, label = "dl")
                LinearProgressIndicator(progress = { anim }, Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)))
                Spacer(Modifier.height(10.dp))
                Text("${d.done shr 20} MB of ${d.total shr 20} MB  ·  ${(frac * 100).toInt()}%", color = cs.onSurface)
                Text("Keep the app open. The screen stays on.", color = cs.onSurfaceVariant, fontSize = 13.sp)
                TextButton(onClick = vm::cancelDownload) { Text("Pause") }
            }
            d.needsWifiConfirm -> {
                Text("You're not on Wi-Fi. This download is large and may use a lot of mobile data.", color = cs.onSurface, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Button(onClick = { vm.startDownload(true) }, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) { Text("Download anyway") }
            }
            d.error != null -> {
                Text(d.error ?: "", color = cs.error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Button(onClick = { vm.startDownload(true) }, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) { Text("Try again") }
            }
            else -> {
                Button(onClick = { vm.startDownload(true) }, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) { Text("Download and start") }
            }
        }

        Spacer(Modifier.height(24.dp))
        if (importing != null) {
            LinearProgressIndicator(progress = { importing ?: 0f }, Modifier.fillMaxWidth())
            Text("Copying model…", color = cs.onSurfaceVariant)
        } else {
            OutlinedButton(onClick = { picker.launch(arrayOf("*/*")) }, shape = RoundedCornerShape(12.dp)) { Text("I already have a model file") }
        }
    }
}

```

## app/src/main/java/com/rui/app/ui/screens/Sidebar.kt

```kotlin
package com.rui.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rui.app.ChatViewModel
import com.rui.app.R
import com.rui.app.data.Chat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private fun groupOf(ts: Long): String {
    val zone = ZoneId.systemDefault()
    val d = Instant.ofEpochMilli(ts).atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    return when {
        d == today -> "Today"
        d == today.minusDays(1) -> "Yesterday"
        d.isAfter(today.minusDays(8)) -> "Previous 7 days"
        else -> "Older"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Sidebar(vm: ChatViewModel, onNewChat: () -> Unit, onOpenChat: (String) -> Unit, onOpenModels: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val chats by vm.chats.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var deleteId by remember { mutableStateOf<String?>(null) }
    var showInstr by remember { mutableStateOf(false) }
    val instr by vm.instructions.collectAsStateWithLifecycle()
    var draft by remember(showInstr) { mutableStateOf(instr) }
    val grouped = remember(chats, query) {
        chats.filter { it.title.contains(query, ignoreCase = true) }
            .sortedByDescending(Chat::updatedAt)
            .groupBy { groupOf(it.updatedAt) }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.rui_logo), "Rui", Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)))
            Column(Modifier.padding(start = 12.dp)) {
                Text("Rui", style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp), color = cs.onSurface)
                Text("feeling calm", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onNewChat,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = cs.primary),
        ) {
            Icon(Icons.Rounded.Add, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("New chat")
        }
        Spacer(Modifier.height(12.dp))
        TextField(
            value = query, onValueChange = { query = it },
            placeholder = { Text("Search chats…") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = cs.surfaceContainer, unfocusedContainerColor = cs.surfaceContainer,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions.Default,
        )
        LazyColumn(Modifier.weight(1f).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (grouped.isEmpty()) item {
                Text("No chats yet", color = cs.onSurfaceVariant, modifier = Modifier.padding(12.dp))
            }
            listOf("Today", "Yesterday", "Previous 7 days", "Older").forEach { label ->
                val items = grouped[label] ?: return@forEach
                item(key = "h$label") {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 14.dp, bottom = 4.dp))
                }
                items(items, key = { it.id }) { c ->
                    Text(
                        c.title, maxLines = 2, overflow = TextOverflow.Ellipsis, color = cs.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .combinedClickable(onClick = { onOpenChat(c.id) }, onLongClick = { deleteId = c.id }).padding(horizontal = 8.dp, vertical = 10.dp),
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { showInstr = true }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Tune, null, tint = cs.secondary)
            Spacer(Modifier.width(12.dp))
            Text("Custom instructions", color = cs.onSurface)
        }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onOpenModels).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Memory, null, tint = cs.secondary)
            Spacer(Modifier.width(12.dp))
            Text("Models & storage", color = cs.onSurface)
        }
    }
    deleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Delete this chat?") },
            confirmButton = { TextButton(onClick = { vm.deleteChat(id); deleteId = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Cancel") } },
        )
    }
    if (showInstr) {
        AlertDialog(
            onDismissRequest = { showInstr = false },
            title = { Text("Custom instructions") },
            text = {
                TextField(
                    value = draft, onValueChange = { draft = it }, minLines = 3, maxLines = 6,
                    placeholder = { Text("e.g. Reply in Hindi. Keep answers short.") },
                )
            },
            confirmButton = { TextButton(onClick = { vm.saveInstructions(draft); showInstr = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showInstr = false }) { Text("Cancel") } },
        )
    }
}

```

## app/src/main/java/com/rui/app/ui/theme/Theme.kt

```kotlin
package com.rui.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Colours sampled from the Rui desktop UI and logo. */
private val RuiBlue = Color(0xFF4F6BFF)
private val RuiPurple = Color(0xFF9B5CF6)
private val RuiLilac = Color(0xFFB794F6)
val RuiAmber = Color(0xFFF5B83D)
val RuiGreen = Color(0xFF34D399)

private val RuiDark = darkColorScheme(
    primary = RuiBlue, onPrimary = Color.White,
    secondary = RuiLilac, tertiary = RuiPurple,
    background = Color(0xFF0A0A1C), onBackground = Color(0xFFE8EAF6),
    surface = Color(0xFF0A0A1C), onSurface = Color(0xFFE8EAF6),
    onSurfaceVariant = Color(0xFF8A8DB0),
    surfaceContainerLow = Color(0xFF0D0D22),
    surfaceContainer = Color(0xFF14142D),
    surfaceContainerHigh = Color(0xFF1B1B3A),
    outline = Color(0xFF2A2A4A), outlineVariant = Color(0xFF1F1F3C),
)

private val RuiTypography = Typography(
    headlineLarge = TextStyle(fontSize = 38.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
)

private val RuiShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
)

/** Blue → purple gradient used by the greeting, composer border and send button. */
@Composable
fun brandBrush(): Brush {
    val cs = MaterialTheme.colorScheme
    return Brush.horizontalGradient(listOf(cs.primary, cs.tertiary))
}

/**
 * Rui's own dark palette by default so the app matches the desktop design.
 * Pass dynamicColor = true to follow the wallpaper (Material You) on Android 12+.
 */
@Composable
fun RuiTheme(dynamicColor: Boolean = false, content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val dark = if (dynamicColor) isSystemInDarkTheme() else true
    val scheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        else -> RuiDark
    }
    MaterialTheme(colorScheme = scheme, typography = RuiTypography, shapes = RuiShapes, content = content)
}

```

