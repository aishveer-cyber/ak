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
