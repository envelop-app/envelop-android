package app.envelop.ui

import androidx.appcompat.app.AppCompatActivity
import app.envelop.App
import app.envelop.common.di.ActivityComponent
import app.envelop.common.di.ActivityModule

abstract class BaseActivity : AppCompatActivity() {

  val component: ActivityComponent by lazy {
    (application as App).component.plus(ActivityModule(this))
  }

}