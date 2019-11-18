package app.envelop.data.models

import kotlin.math.roundToInt

class Progress(
  private val current: Int,
  private val total: Int
) {

  private val fraction get() = current.toFloat() / total.toFloat()
  val percentage get() = (fraction * 100).roundToInt()

}