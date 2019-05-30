package app.envelop.data

import android.content.Context
import app.envelop.App
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

  @Provides
  @Singleton
  fun database(context: Context, appMode: App.Mode) =
    Database.create(context, appMode)

  @Provides
  fun docRepository(database: Database) =
    database.docRepository()

}