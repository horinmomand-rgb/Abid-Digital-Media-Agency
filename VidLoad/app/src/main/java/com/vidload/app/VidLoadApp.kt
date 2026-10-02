package com.vidload.app
import android.app.Application
import dev.ffmpegkit_maintained.ytdlp.YtDlp
import dev.ffmpegkit_maintained.ytdlp.YtDlpException
class VidLoadApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try { YtDlp.init(this) } catch (_: YtDlpException) {}
    }
}
