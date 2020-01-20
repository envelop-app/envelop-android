package app.envelop.ui.main

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.envelop.R
import app.envelop.test.AppHelper.resources
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class FormatRelativeDateTest {

  private val localeList =
    listOf<Locale>(Locale.getDefault(), Locale.GERMANY, Locale.CHINA, Locale.US, Locale.UK)

  @Test
  fun format_otherYear() {
    localeList.forEach {
      val getDate = FormatRelativeDate(resources, it)
      assertThat(getDate.format(Date(1558359368000)), equalTo("20/05/2019"))
    }
  }

  @Test
  fun format_thisYear() {
    val calendar = Calendar.getInstance().also { it.set(Calendar.DAY_OF_YEAR, 1) }

    assertThat(FormatRelativeDate(resources, Locale.US).format(calendar.time), equalTo("Jan 1"))
    assertThat(FormatRelativeDate(resources, Locale.GERMANY).format(calendar.time), equalTo("Jan. 1"))
    assertThat(FormatRelativeDate(resources, Locale.ITALY).format(calendar.time), equalTo("gen 1"))
  }

  @Test
  fun format_twoHoursAgo() {
    val calendar = Calendar.getInstance().also { it.add(Calendar.HOUR, -2) }
    val expectedString = resources.getQuantityString(R.plurals.hour, 2, 2)

    localeList.forEach {
      val getDate = FormatRelativeDate(resources, it)
      assertThat(getDate.format(calendar.time), equalTo(expectedString))
    }
  }

  @Test
  fun format_tenMinutesAgo() {
    val calendar = Calendar.getInstance().also { it.add(Calendar.MINUTE, -10) }
    val expectedString = resources.getQuantityString(R.plurals.min, 10, 10)

    localeList.forEach {
      val getDate = FormatRelativeDate(resources, it)
      assertThat(getDate.format(calendar.time), equalTo(expectedString))
    }
  }

  @Test
  fun format_now() {
    val expectedString = resources.getString(R.string.now)

    localeList.forEach {
      val getDate = FormatRelativeDate(resources, it)
      assertThat(getDate.format(Date()), equalTo(expectedString))
    }
  }
}
