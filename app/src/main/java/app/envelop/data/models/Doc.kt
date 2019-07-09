package app.envelop.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow

@Entity(
  indices = [
    Index(value = ["id"], unique = true),
    Index(value = ["createdAt"], unique = false)
  ]
)
@Parcelize
data class Doc(
  @PrimaryKey val id: String = "",
  val url: String = "",
  val size: Long = 0,
  val contentType: String? = null,
  val createdAt: Date = Date(),
  val uploaded: Boolean? = null,
  val parts: Int? = null,
  val deleted: Boolean? = null
) : Parcelable {

  val name get() = url.split("/").last()
  val uploadedNonNull get() = uploaded != false
  val deletedNonNull get() = deleted != true

  val humanSize: String
    get() {
      val unit = 1000.0
      if (size < unit) return "$size B";
      val exp = (ln(size.toDouble()) / ln(unit)).toInt()
      val pre = "kMGTPE"[exp - 1]
      return "%.1f %sB".format(size / unit.pow(exp.toDouble()), pre)
    }

  val fileType get() = FileType.fromContentType(contentType)

  fun calculateParts(partSize: Long) = calculateParts(size, partSize)

  fun allParts() =
    if (parts == null || parts <= 1) {
      listOf(DocPart(1, url, true))
    } else {
      (1..parts).map { DocPart(it, url, false) }
    }

  companion object {
    fun calculateParts(size: Long, partSize: Long) =
      ceil(size.toDouble() / partSize).toInt()
  }

}

data class DocPart(
  val part: Int,
  val baseUrl: String,
  val onlyOnePart: Boolean
) {

  val url
    get() = if (onlyOnePart) {
      baseUrl
    } else {
      "$baseUrl.part$part"
    }

}