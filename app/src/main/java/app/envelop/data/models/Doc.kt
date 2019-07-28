package app.envelop.data.models

import app.envelop.data.InnerJsonObject
import app.envelop.data.JsonKey
import com.google.gson.JsonObject
import java.util.*
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow

data class Doc(
  val id: String,
  val name: String,
  val url: String,
  val size: Long,
  val contentType: String?,
  val createdAt: Date = Date(),
  val uploaded: Boolean = false,
  val numParts: Int,
  val deleted: Boolean = false,
  val version: Int = CURRENT_VERSION,
  private val innerJson: InnerJsonObject = InnerJsonObject()
) {

  val humanSize: String
    get() {
      val unit = 1000.0
      if (size < unit) return "$size B";
      val exp = (ln(size.toDouble()) / ln(unit)).toInt()
      val pre = "kMGTPE"[exp - 1]
      return "%.1f %sB".format(size / unit.pow(exp.toDouble()), pre)
    }

  val fileType get() = FileType.fromContentType(contentType)

  val canEdit get() = version <= CURRENT_VERSION

  fun calculateParts(partSize: Long) = calculateNumParts(size, partSize)

  fun toJsonObject() =
    innerJson.also {
      it.setString(Key.Id, id)
      it.setString(Key.Name, name)
      it.setString(Key.Url, url)
      it.setLong(Key.Size, size)
      it.setString(Key.ContentType, contentType)
      it.setDate(Key.CreatedAt, createdAt)
      it.setBoolean(Key.Uploaded, uploaded)
      it.setInt(Key.NumParts, numParts)
      it.setBoolean(Key.Deleted, deleted)
      it.setInt(Key.Version, version)
    }.json

  companion object {
    private const val CURRENT_VERSION = 1

    fun calculateNumParts(size: Long, partSize: Long) =
      ceil(size.toDouble() / partSize).toInt()

    fun build(json: JsonObject) =
      InnerJsonObject(json).let {
        val url = it.getString(Key.Url)
        Doc(
          id = it.getString(Key.Id),
          name = it.optString(Key.Name) ?: url.split("/").last(),
          url = url,
          size = it.getLong(Key.Size),
          contentType = it.optString(Key.ContentType),
          createdAt = it.getDate(Key.CreatedAt),
          uploaded = it.optBoolean(Key.Uploaded) ?: true,
          numParts = it.optInt(Key.NumParts) ?: 1,
          deleted = it.optBoolean(Key.Deleted) ?: false,
          version = it.optInt(Key.Version) ?: 1,
          innerJson = it
        )
      }
  }

  enum class Key(override val value: String) : JsonKey {
    Id("id"), Name("name"), Url("url"), Size("size"), ContentType("content_type"),
    CreatedAt("created_at"), Uploaded("uploaded"), NumParts("num_parts"), Deleted("deleted"),
    Version("version")
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