package app.envelop.ui.main

import android.content.res.Resources
import app.envelop.R
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

class FormatRelativeDate
@Inject constructor(
  private val resources: Resources,
  private val locale: Locale
) {

  private val shortDateFormat by lazy { SimpleDateFormat("MMM d", locale) }
  private val fullDateFormat by lazy { SimpleDateFormat("dd/MM/yyyy", locale) }

  fun format(date: Date): String {
    val now = Calendar.getInstance()
    val calendar = Calendar.getInstance().also { it.time = date }

    return if (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
      // Same year
      if (calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
        // Same day
        val diff = now.timeInMillis - calendar.timeInMillis
        val min = (diff / 1000 / 60).toInt()
        val hour = (min / 60f).roundToInt()

        when {
          min < 1 -> resources.getString(R.string.now)
          min < 60 -> resources.getQuantityString(R.plurals.min, min, min)
          else -> resources.getQuantityString(R.plurals.hour, hour, hour)
        }
      } else {
        shortDateFormat.format(calendar.time)
      }
    } else {
      fullDateFormat.format(calendar.time)
    }
  }
}
