package app.envelop.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
  indices = [
    Index(value = ["id"], unique = true),
    Index(value = ["createdAt"], unique = false)
  ]
)
data class Doc(
  @PrimaryKey val id: String = "",
  val url: String = "",
  val size: Long = 0,
  val contentType: String? = null,
  val createdAt: Date = Date()
) {

  val name get() = url.split("/").last()

  val humanSize: String
    get() {
      val unit = 1024.0
      if (size < unit) return "$size B";
      val exp = (Math.log(size.toDouble()) / Math.log(unit)).toInt()
      val pre = "KMGTPE"[exp - 1]
      return "%.1f %sB".format(size / Math.pow(unit, exp.toDouble()), pre)
    }

}
