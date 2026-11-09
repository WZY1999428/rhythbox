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
    ).fallbackToDestructiveMigration(false)
}