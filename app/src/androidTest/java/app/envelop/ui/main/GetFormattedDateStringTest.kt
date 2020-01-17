package app.envelop.ui.main

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.envelop.R
import app.envelop.test.AppHelper.context
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class GetFormattedDateStringTest {

  private val localeList =
    listOf<Locale>(Locale.getDefault(), Locale.GERMANY, Locale.CHINA, Locale.US, Locale.UK)

  @Test
  fun uploadOldDateFormatTest() {
    localeList.forEach {
      val getDate = GetFormattedDateString(context, it)
      assertThat(getDate.toDateStringFormatted(Date(1558359368000)), equalTo("20/05/2019"))
    }
  }

  @Test
  fun uploadRecentDateFormatTest() {
    val expectedString = context.resources.getQuantityString(R.plurals.min, 1, 1)

    localeList.forEach {
      val getDate = GetFormattedDateString(context, it)
      assertThat(getDate.toDateStringFormatted(Date()), equalTo(expectedString))
    }
  }

  @Test
  fun uploadTwoHourAgoDateFormatTest() {
    val date = Date()
    val calendar = Calendar.getInstance().also { it.time = date }
    calendar.add(Calendar.HOUR, -2)
    val expectedString = context.resources.getQuantityString(R.plurals.hour, 2, 2)

    localeList.forEach {
      val getDate = GetFormattedDateString(context, it)
      assertThat(getDate.toDateStringFormatted(calendar.time), equalTo(expectedString))
    }
  }
}
