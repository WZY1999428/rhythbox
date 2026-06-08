package wzy.hkta.rhythbox

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import wzy.hkta.rhythbox.database.AppDatabase
import wzy.hkta.rhythbox.expect.FloatLyric
import wzy.hkta.rhythbox.expect.RhythboxPlayer
import wzy.hkta.rhythbox.expect.ScanMusic
import wzy.hkta.rhythbox.expect.getDatabaseBuilder
import wzy.hkta.rhythbox.expect.getRoomDatabase

class MainActivity : ComponentActivity() {
    // 这样它们不仅绝对安全，而且在整个 Activity 的任何地方都能直接访问
    private lateinit var database: AppDatabase
    private lateinit var rhythboxPlayer: RhythboxPlayer
    private lateinit var scanMusic: ScanMusic
    private val floatLyric = FloatLyric(this)

    @SuppressLint("RememberReturnType")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = getRoomDatabase(getDatabaseBuilder(this))
        rhythboxPlayer = RhythboxPlayer(this, database)
        scanMusic = ScanMusic(this, database)
        setContent {
            App(rhythboxPlayer, database, scanMusic, floatLyric)
        }
    }
}