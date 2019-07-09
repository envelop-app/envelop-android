package app.envelop.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.envelop.App
import app.envelop.data.models.Doc
import app.envelop.data.models.Upload
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.UploadRepository

@androidx.room.Database(
  entities = [
    Doc::class,
    Upload::class
  ], version = 1
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {

  abstract fun docRepository(): DocRepository
  abstract fun uploadRepository(): UploadRepository

  companion object {
    private const val NAME = "app.db"

    fun create(context: Context, appMode: App.Mode) =
      Room
        .databaseBuilder(context, Database::class.java, getName(appMode))
        .addMigrations(MIGRATION_1_2)
        .build()

    private fun getName(appMode: App.Mode) = when (appMode) {
      App.Mode.Normal -> NAME
      App.Mode.Test -> "test_$NAME"
    }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Doc ADD COLUMN uploaded BOOLEAN DEFAULT 1")
        database.execSQL("ALTER TABLE Doc ADD COLUMN parts INTEGER")
        database.execSQL("ALTER TABLE Doc ADD COLUMN deleted BOOLEAN DEFAULT 0")
      }
    }
  }
}