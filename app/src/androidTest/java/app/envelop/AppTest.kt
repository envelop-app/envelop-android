package app.envelop

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.envelop.test.AppHelper.getApplication
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppTest {

  @Test
  fun mode() {
    assertThat(getApplication().mode).isEqualTo(App.Mode.Test)
  }

}
