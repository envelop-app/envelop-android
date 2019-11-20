package app.envelop.data.models

import app.envelop.data.InnerJsonObject
import app.envelop.data.JsonKey
import app.envelop.data.security.EncryptionSpec
import app.envelop.data.security.EncryptionSpecProvider
import app.envelop.data.toInnerJsonObject
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
  val passcode: String? = null,
  val partIVs: List<String>? = null,
  val username: String,
  val encryptionSpec: EncryptionSpec?,
  private val innerJson: InnerJsonObject = InnerJsonObject()
) {

  val humanSize: String
    get() {
      val unit = 1000.0
      if (size < unit) return "$size B"
      val exp = (ln(size.toDouble()) / ln(unit)).toInt()
      val pre = "kMGTPE"[exp - 1]
      return "%.1f %sB".format(size / unit.pow(exp.toDouble()), pre)
    }

  val fileType get() = FileType.fromContentType(contentType)

  val canEdit get() = version <= CURRENT_VERSION

  fun calculateParts(partSize: Long) = calculateNumParts(size, partSize)

  fun toJsonObject() =
    innerJson.clone().also {
      it.set(Key.Id, id)
      it.set(Key.Name, name)
      it.set(Key.Url, url)
      it.set(Key.Size, size)
      it.set(Key.ContentType, contentType)
      it.set(Key.CreatedAt, createdAt)
      it.set(Key.Uploaded, uploaded)
      it.set(Key.NumParts, numParts)
      it.set(Key.Deleted, deleted)
      it.set(Key.Version, version)
      it.set(Key.Passcode, passcode)
      it.set(Key.PartIVs, partIVs)
      it.set(Key.Username, username)
      it.set(Key.Encryption, encryptionSpecToJsonObject())
    }

  fun encryptionSpecToJsonObject() =
    encryptionSpec?.toJson(innerJson.optObjectOrEmpty(Key.Encryption))

  companion object {
    const val CURRENT_VERSION = 2

    fun calculateNumParts(size: Long, partSize: Long) =
      ceil(size.toDouble() / partSize).toInt()

    fun build(json: JsonObject) =
      json
        .toInnerJsonObject()
        .let {
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
            passcode = it.optString(Key.Passcode),
            partIVs = it.optListString(Key.PartIVs),
            username = it.getString(Key.Username),
            encryptionSpec = EncryptionSpecProvider.getSpec(it.optObject(Key.Encryption)),
            innerJson = it
          )
        }
  }

  enum class Key(override val value: String) : JsonKey {
    Id("id"), Name("name"), Url("url"), Size("size"), ContentType("content_type"),
    CreatedAt("created_at"), Uploaded("uploaded"), NumParts("num_parts"), Deleted("deleted"),
    Version("version"), Passcode("passcode"), PartIVs("part_ivs"), Username("username"),
    Encryption("encryption")
  }
}
