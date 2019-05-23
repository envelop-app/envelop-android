package app.envelop.test

import androidx.test.platform.app.InstrumentationRegistry
import app.envelop.App

object AppHelper {
  fun getContext() = InstrumentationRegistry.getInstrumentation().targetContext
  fun getApplication() = getContext().applicationContext as App
  fun getAppComponent() = getApplication().component
  fun getResources() = getContext().resources
}