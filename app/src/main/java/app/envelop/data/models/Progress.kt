package app.envelop.data.models

import kotlin.math.roundToInt

class Progress(
  val current: Int,
  val total: Int
) {

  val fraction get() = current.toFloat() / total.toFloat()
  val percentage get() = (fraction * 100).roundToInt()

}