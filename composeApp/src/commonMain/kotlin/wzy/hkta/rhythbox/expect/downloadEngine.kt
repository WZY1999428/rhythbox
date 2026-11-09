package wzy.hkta.rhythbox.expect

import com.linroid.ketch.core.Ketch
import com.linroid.ketch.sqlite.DriverFactory

expect fun initKetch(savePath: String, driverFactory: DriverFactory): Ketch

expect fun deleteFile(filePath: String)