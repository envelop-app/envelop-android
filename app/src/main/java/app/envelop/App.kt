package app.envelop

import android.app.Application
import app.envelop.common.di.AppComponent
import app.envelop.common.di.AppModule
import app.envelop.common.di.DaggerAppComponent
import app.envelop.domain.DeleteDocService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class App : Application() {

  @Inject
  lateinit var deleteDocService: DeleteDocService

  enum class Mode { Normal, Test }

  val mode: Mode by lazy {
    try {
      classLoader.loadClass("app.envelop.AppTest")
      Mode.Test
    } catch (e: Exception) {
      Mode.Normal
    }
  }

  val component: AppComponent by lazy {
    DaggerAppComponent.builder().appModule(AppModule(this)).build()
  }

  private val disposables = CompositeDisposable()

  override fun onCreate() {
    super.onCreate()
    component.inject(this)

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }

    deleteDocService
      .deletePending()
      .subscribe()
      .addTo(disposables)
  }

  override fun onTerminate() {
    super.onTerminate()
    disposables.clear()
  }
}