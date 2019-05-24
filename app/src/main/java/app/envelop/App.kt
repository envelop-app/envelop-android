package app.envelop

import android.app.Application
import app.envelop.common.di.AppModule
import app.envelop.common.di.DaggerAppComponent
import timber.log.Timber

class App : Application() {

  enum class Mode { Normal, Test }

  val mode: Mode by lazy {
    try {
      classLoader.loadClass("app.envelop.AppTest")
      Mode.Test
    } catch (e: Exception) {
      Mode.Normal
    }
  }

  val component by lazy {
    DaggerAppComponent.builder().appModule(AppModule(this)).build()
  }

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
  }
}