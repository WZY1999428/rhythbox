package wzy.hkta.rhythbox

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.linroid.ketch.core.Ketch
import com.linroid.ketch.engine.KtorHttpEngine
import com.linroid.ketch.sqlite.DriverFactory
import wzy.hkta.rhythbox.database.AppDatabase
import wzy.hkta.rhythbox.expect.FloatLyric
import wzy.hkta.rhythbox.expect.RhythboxPlayer
import wzy.hkta.rhythbox.expect.ScanMusic
import wzy.hkta.rhythbox.expect.getDatabaseBuilder
import wzy.hkta.rhythbox.expect.getRoomDatabase
import wzy.hkta.rhythbox.expect.initKetch
import wzy.hkta.rhythbox.utils.DownloadEngine
import java.io.File

class MainActivity : ComponentActivity() {
    // 这样它们不仅绝对安全，而且在整个 Activity 的任何地方都能直接访问
    private lateinit var database: AppDatabase
    private lateinit var rhythboxPlayer: RhythboxPlayer
    private lateinit var scanMusic: ScanMusic
    private lateinit var downloadEngine: Ketch
    private val floatLyric = FloatLyric(this)

    @SuppressLint("RememberReturnType")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savePath = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "rhythbox")
        database = getRoomDatabase(getDatabaseBuilder(this))
        rhythboxPlayer = RhythboxPlayer(this, database)
        scanMusic = ScanMusic(this, database)
        downloadEngine = initKetch(savePath.absolutePath, DriverFactory(this,"rhythbox-ketch.db"))
        setContent {
            App(rhythboxPlayer, database, scanMusic, floatLyric, downloadEngine)
        }
    }
}