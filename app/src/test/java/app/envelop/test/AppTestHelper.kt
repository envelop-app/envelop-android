package app.envelop.test

import app.envelop.App
import app.envelop.common.di.AppModule
import io.mockk.mockk

object AppTestHelper {

  val appModule get() = AppModule(mockk())

}