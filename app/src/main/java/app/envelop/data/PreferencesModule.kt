package app.envelop.data

import android.content.Context
import android.content.SharedPreferences
import app.envelop.App
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PreferencesModule {

  @Provides
  @Singleton
  fun sharedPreferences(context: Context, appMode: App.Mode): SharedPreferences =
    context.getSharedPreferences(
      when (appMode) {
        App.Mode.Normal -> PREFERENCES_NAME
        App.Mode.Test -> PREFERENCES_NAME + "_test"
      },
      Context.MODE_PRIVATE
    )

  @Provides
  @Singleton
  fun rxSharedPreferences(sharedPreferences: SharedPreferences) =
    RxSharedPreferences.create(sharedPreferences)

  companion object {
    private const val PREFERENCES_NAME = "envelop"
  }

}