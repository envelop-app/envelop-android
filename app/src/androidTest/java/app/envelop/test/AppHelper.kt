package app.envelop.test

import androidx.test.platform.app.InstrumentationRegistry
import app.envelop.App

object AppHelper {
  val context get() = InstrumentationRegistry.getInstrumentation().targetContext!!
  val application get() = context.applicationContext as App
}