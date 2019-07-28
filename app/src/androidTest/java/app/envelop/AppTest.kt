package app.envelop

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.envelop.test.AppHelper.app
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppTest {

  @Test
  fun mode() {
    assertEquals(app.mode, App.Mode.Test)
  }

}
