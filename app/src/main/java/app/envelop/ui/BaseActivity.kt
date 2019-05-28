package app.envelop.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import app.envelop.App
import app.envelop.common.di.ActivityComponent
import app.envelop.common.di.ActivityModule
import app.envelop.ui.common.ActivityResult
import app.envelop.ui.common.Finish
import app.envelop.ui.common.toActivityResult
import io.reactivex.subjects.PublishSubject

abstract class BaseActivity : AppCompatActivity() {

  val component: ActivityComponent by lazy {
    (application as App).component.plus(ActivityModule(this))
  }

  protected val results = PublishSubject.create<ActivityResult>()

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    results.onNext(ActivityResult(requestCode, resultCode, data))
  }

  protected fun finish(finish: Finish) {
    setResult(finish.result.toActivityResult())
    finish()
  }

}