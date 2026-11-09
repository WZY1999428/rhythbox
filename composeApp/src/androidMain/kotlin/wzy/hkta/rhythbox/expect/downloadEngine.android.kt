package wzy.hkta.rhythbox.expect

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.linroid.ketch.core.Ketch
import com.linroid.ketch.engine.KtorHttpEngine
import com.linroid.ketch.sqlite.DriverFactory
import com.linroid.ketch.sqlite.createSqliteTaskStore
import java.io.File

actual fun initKetch(savePath: String, driverFactory: DriverFactory): Ketch {
    return Ketch(
        httpEngine = KtorHttpEngine(),
        taskStore = createSqliteTaskStore(driverFactory)
    )
}

actual fun deleteFile(filePath: String) {
    val file = File(filePath)
    file.delete()
}