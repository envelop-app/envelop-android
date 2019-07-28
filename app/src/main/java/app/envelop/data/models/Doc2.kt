package app.envelop.data.models

import app.envelop.data.Converters
import com.google.gson.JsonObject
import java.util.*
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow

data class Doc2(
  val json: JsonObject
) {

  var id: String
    get() = json.get("id").asString
    set(value) = json.addProperty("id", value)

  var url: String
    get() = json.get("url").asString
    set(value) = json.addProperty("url", value)

  var size: Long
    get() = json.get("size").asLong
    set(value) = json.addProperty("size", value)

  var contentType: String?
    get() = json.get("content_type").asString
    set(value) = json.addProperty("content_type", value)

  var createdAt: Date?
    get() = Converters().timestampToDate(json.get("created_at").asLong) ?: Date()
    set(value) = json.addProperty("created_at", Converters().dateToTimestamp(value))


  val uploaded: Boolean? = null
  val numParts: Int? = null
  val deleted: Boolean? = null

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

  fun calculateParts(partSize: Long) = calculateNumParts(size, partSize)

  fun allParts() =
    if (numParts == null || numParts <= 1) {
      listOf(DocPart(1, url, true))
    } else {
      (0 until numParts).map { DocPart(it, url, false) }
    }

  companion object {
    fun calculateNumParts(size: Long, partSize: Long) =
      ceil(size.toDouble() / partSize).toInt()
  }

}
