package app.envelop.ui.main

import android.content.Context
import app.envelop.R
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

class GetFormattedDateString
@Inject constructor(
  private val context: Context,
  private val locale: Locale
) {

  fun toDateStringFormatted(createdAt: Date): String {
    val now = Calendar.getInstance()
    val calendar = Calendar.getInstance().also { it.time = createdAt }
    return if (calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
      && calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    ) {
      val diff = now.timeInMillis - calendar.timeInMillis
      val min = max(diff / 1000 / 60, 1).toInt()
      val hour = (min / 60f).roundToInt()

      if (min < 60) {
        context.resources.getQuantityString(R.plurals.min, min, min)
      } else {
        context.resources.getQuantityString(R.plurals.hour, hour, hour)
      }
    } else {
      val format = SimpleDateFormat("dd/MM/yyyy", locale)
      format.format(calendar.time)
    }
  }
}
