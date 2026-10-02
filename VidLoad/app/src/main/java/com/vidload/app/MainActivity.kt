package com.vidload.app

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ffmpegkit_maintained.ytdlp.DownloadProgressCallback
import dev.ffmpegkit_maintained.ytdlp.YtDlp
import dev.ffmpegkit_maintained.ytdlp.YtDlpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private val Cyan = Color(0xFF53E9FF)
private val Purple = Color(0xFF8A5CFF)
private val Pink = Color(0xFFFF3EC8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VidLoadScreen(applicationContext) }
    }
}

@Composable
private fun VidLoadScreen(context: Context) {
    var url by remember { mutableStateOf("") }
    var progress by remember { mutableFloatStateOf(0f) }
    var downloading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("READY") }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF151B3D), Color(0xFF05060D))))) {
        Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(26.dp))
            Text("VIDLOAD", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text("3D LIQUID DOWNLOAD REACTOR", fontSize = 9.sp, letterSpacing = 2.sp, color = Cyan)
            Spacer(Modifier.height(28.dp))
            Reactor(progress, downloading) {
                if (!downloading && url.isNotBlank()) {
                    downloading = true
                    message = "DOWNLOADING"
                    scope.launch(Dispatchers.IO) {
                        runDownload(context, url, { value -> progress = value }, { ok, text ->
                            downloading = false
                            message = if (ok) "SAVED TO MOVIES / VIDLOAD" else text
                        })
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = url, onValueChange = { url = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Video URL") }, placeholder = { Text("Paste a supported video link") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan, unfocusedBorderColor = Color.White.copy(.18f),
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedLabelColor = Cyan, unfocusedLabelColor = Color.White.copy(.55f)
                ), shape = RoundedCornerShape(20.dp)
            )
            Spacer(Modifier.height(14.dp))
            Text(message, color = Cyan, fontSize = 10.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(10.dp))
            Text("Downloads are written to Android MediaStore under Movies/VidLoad.", color = Color.White.copy(.45f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun Reactor(progress: Float, active: Boolean, onClick: () -> Unit) {
    val inf = rememberInfiniteTransition(label = "reactor")
    val breathe by inf.animateFloat(0.97f, 1.03f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breathe")
    val spin by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(4500, easing = LinearEasing)), label = "spin")
    val p = progress.coerceIn(0f, 1f)
    Box(Modifier.size(300.dp).graphicsLayer { scaleX = if (active) 1f else breathe; scaleY = if (active) 1f else breathe }.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val r = size.minDimension * .39f
            drawCircle(Cyan.copy(.07f), r * 1.35f)
            drawCircle(Purple.copy(.08f), r * 1.18f)
            drawCircle(Color(0xFF090D1C), r * 1.02f)
            drawCircle(Brush.radialGradient(listOf(Color(0xFF263D70), Color(0xFF080A15))), r)
            val top = c.y + r * .94f - r * 1.88f * p
            val path = Path().apply {
                moveTo(c.x - r * .94f, c.y + r * .94f)
                lineTo(c.x - r * .94f, top)
                for (i in 0..80) {
                    val x = c.x - r * .94f + 2 * r * .94f * i / 80
                    val y = top + kotlin.math.sin(i * .24 + spin * .11) * r * .025f
                    lineTo(x, y)
                }
                lineTo(c.x + r * .94f, c.y + r * .94f)
                close()
            }
            val main = when {
                p < .2f -> Cyan
                p < .65f -> Purple
                p < 1f -> Pink
                else -> Color(0xFF65FFB1)
            }
            drawPath(path, Brush.verticalGradient(listOf(main, Cyan.copy(.35f))))
            drawArc(Brush.sweepGradient(listOf(Cyan, Purple, Pink, Cyan)), -90f, p * 360f, false, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
            drawArc(Color.White.copy(.10f), -90f, 360f, false, style = Stroke(2.dp.toPx()))
            for (i in 0 until 18) {
                val a = Math.toRadians(spin.toDouble()) + i * .7
                val rr = r * (.55f + (i % 4) * .07f)
                drawCircle(Color.White.copy(.16f), 2.dp.toPx(), Offset(c.x + kotlin.math.cos(a).toFloat() * rr, c.y + kotlin.math.sin(a).toFloat() * rr))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (p >= 1f) "✓" else "↓", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Black)
            Text("${{(p * 100).toInt()}%", fontSize = 38.sp, color = Color.White, fontWeight = FontWeight.Black)
            Text(if (active) "DOWNLOADING" else "TAP TO DOWNLOAD", fontSize = 9.sp, color = Cyan, letterSpacing = 2.sp)
        }
    }
}

private suspend fun runDownload(context: Context, url: String, onProgress: (Float) -> Unit, done: (Boolean, String) -> Unit) {
    try {
        val dir = File(context.cacheDir, "vidload").apply { mkdirs() }
        val template = File(dir, "%(title)s.%(ext)s").absolutePath
        val request = YtDlpRequest(url).setOutputTemplate(template)
            .addOption("-f", "bv*[ext=mp4][height<=1080]+ba[ext=m4a]/b[ext=mp4]/b")
            .addOption("--merge-output-format", "mp4").addOption("--newline")
        YtDlp.executeAsync(request, DownloadProgressCallback { value, _, _ ->
            onProgress((value.toFloat() / 100f).coerceIn(0f, 1f))
        }) { response ->
            val file = dir.listFiles()?.maxByOrNull { it.lastModified() }
            if (response.exitCode == 0 && file != null) {
                publish(context, file)
                onProgress(1f)
                done(true, "SAVED TO MOVIES / VIDLOAD")
            } else done(false, response.stderr ?: "Download failed")
        }
    } catch (t: Throwable) { done(false, t.message ?: "Download failed") }
}

private fun publish(context: Context, file: File) {
    val values = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, file.nameWithoutExtension + ".mp4")
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/VidLoad")
        put(MediaStore.Video.Media.IS_PENDING, 1)
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: error("MediaStore insert failed")
    resolver.openOutputStream(uri).use { out -> file.inputStream().use { input -> input.copyTo(out!!) } }
    values.clear()
    values.put(MediaStore.Video.Media.IS_PENDING, 0)
    resolver.update(uri, values, null, null)
    file.delete()
}
