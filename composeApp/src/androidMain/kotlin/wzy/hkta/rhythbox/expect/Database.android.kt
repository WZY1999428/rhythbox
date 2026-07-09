package wzy.hkta.rhythbox.expect

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import wzy.hkta.rhythbox.database.AppDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("rhythbox.db")

    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
    // 💡 提示：有了上面的 AutoMigration，这里不需要再配置任何破坏性清空，数据会稳稳留住！
}