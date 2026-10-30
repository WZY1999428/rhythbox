package wzy.hkta.rhythbox.expect

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.linroid.ketch.core.Ketch
import com.linroid.ketch.engine.KtorHttpEngine
import com.linroid.ketch.sqlite.DriverFactory
import com.linroid.ketch.sqlite.createSqliteTaskStore

actual fun initKetch(savePath: String,driverFactory: DriverFactory): Ketch {
        return Ketch(
            httpEngine = KtorHttpEngine(),
            taskStore = createSqliteTaskStore(driverFactory)
        )
}