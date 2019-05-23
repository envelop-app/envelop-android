package app.envelop.common.di

import android.content.Context
import app.envelop.App
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(
  private val app: App
) {

  @Provides
  fun app() = app

  @Provides
  fun context() = app as Context

  @Provides
  fun appMode() = app.mode

  @Provides
  fun resources(context: Context) = context.resources

  @Provides
  @Singleton
  fun gson() =
    GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .create()

}