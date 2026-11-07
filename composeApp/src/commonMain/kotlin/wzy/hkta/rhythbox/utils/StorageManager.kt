package wzy.hkta.rhythbox.utils


// 在 commonMain 中定义
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

object StorageManager {
    val settings: Settings = Settings()

    // 存的时候，编译器把 T 替换成具体的类型，从而决定是走 putInt 还是 Json 序列化
    inline fun <reified T : Any> setItem(key: String, value: T) {
        when (value) {
            is String -> settings.putString(key, value)
            is Int -> settings.putInt(key, value)
            is Boolean -> settings.putBoolean(key, value)
            is Float -> settings.putFloat(key, value)
            is Long -> settings.putLong(key, value)
            else -> {
                val jsonString = Json.encodeToString(value)
                settings.putString(key, jsonString)
            }
        }
    }

    // 取的时候，编译器知道你想转成什么对象，从而让 Json.decodeFromString<T> 能够正常工作
    inline fun <reified T> getItem(key: String): T? {
        if (!settings.hasKey(key)) return null

        return when (T::class) {
            String::class -> settings.getString(key, "") as T
            Int::class -> settings.getInt(key, 0) as T
            Boolean::class -> settings.getBoolean(key, false) as T
            Float::class -> settings.getFloat(key, 0f) as T
            Long::class -> settings.getLong(key, 0L) as T

            else -> {
                val jsonString = settings.getString(key, "")
                if (jsonString.isEmpty()) return null

                try {
                    Json.decodeFromString<T>(jsonString)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    fun remove(key: String) {
        settings.remove(key)
    }

    fun clear() {
        settings.clear()
    }

}