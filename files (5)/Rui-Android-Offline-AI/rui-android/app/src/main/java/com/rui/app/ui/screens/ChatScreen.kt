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
