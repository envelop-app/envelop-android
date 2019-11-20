package app.envelop.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.envelop.App
import app.envelop.data.models.Upload
import app.envelop.data.repositories.UploadRepository

@androidx.room.Database(
  entities = [
    Upload::class
  ],
  version = 3
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {

  abstract fun uploadRepository(): UploadRepository

  companion object {
    private const val NAME = "app.db"

    fun create(context: Context, appMode: App.Mode) =
      Room
        .databaseBuilder(context, Database::class.java, getName(appMode))
        .fallbackToDestructiveMigrationFrom(1, 2)
        .build()

    private fun getName(appMode: App.Mode) = when (appMode) {
      App.Mode.Normal -> NAME
      App.Mode.Test -> "test_$NAME"
    }
  }
}