package app.envelop.data

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused")
data class InnerJsonObject(
  val json: JsonObject = JsonObject()
) {

  private val dateTimeFormat by lazy {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.getDefault()).also {
      it.timeZone = TimeZone.getTimeZone("UTC")
    }
  }
  private val fallbackDateTimeFormat by lazy {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
  }

  fun getObject(key: JsonKey) = optObject(key)!!
  fun optObject(key: JsonKey) = (json.get(key.value) as? JsonObject)?.toInnerJsonObject()
  fun optObjectOrEmpty(key: JsonKey) = optObject(key) ?: InnerJsonObject()
  fun set(key: JsonKey, value: InnerJsonObject?) = json.add(key.value, value?.json)

  fun getString(key: JsonKey) = optString(key)!!
  fun optString(key: JsonKey) = (json.get(key.value) as? JsonPrimitive)?.asString
  fun set(key: JsonKey, value: String?) = json.addProperty(key.value, value)

  fun getLong(key: JsonKey) = optLong(key)!!
  private fun optLong(key: JsonKey) = (json.get(key.value) as? JsonPrimitive)?.asLong
  fun set(key: JsonKey, value: Long?) = json.addProperty(key.value, value)

  fun getInt(key: JsonKey) = optInt(key)!!
  fun optInt(key: JsonKey) = (json.get(key.value) as? JsonPrimitive)?.asInt
  fun set(key: JsonKey, value: Int?) = json.addProperty(key.value, value)

  fun getBoolean(key: JsonKey) = optBoolean(key)!!
  fun optBoolean(key: JsonKey) = (json.get(key.value) as? JsonPrimitive)?.asBoolean
  fun set(key: JsonKey, value: Boolean?) = json.addProperty(key.value, value)

  fun getDate(key: JsonKey) = optDate(key)!!
  private fun optDate(key: JsonKey): Date? = optString(key)?.let { parseDateWithFallback(it) }
  fun set(key: JsonKey, value: Date?) = set(key, value?.let { dateTimeFormat.format(it) })

  fun getListString(key: JsonKey) = optListString(key)!!
  fun optListString(key: JsonKey): List<String>? =
    (json.get(key.value) as? JsonArray)
      ?.mapNotNull { (it as? JsonPrimitive)?.asString }

  fun set(key: JsonKey, value: List<String>?) =
    json.add(
      key.value,
      value?.let { list ->
        JsonArray(list.size).also { jsonArray ->
          list.forEach { jsonArray.add(it) }
        }
      }
    )

  override fun toString() = "InnerJsonObject(${hashCode()})"

  fun clone() =
    copy(json = json.deepCopy())

  private fun parseDateWithFallback(value: String) =
    try {
      dateTimeFormat.parse(value)
    } catch (e: Throwable) {
      fallbackDateTimeFormat.parse(value)
    }

}

fun JsonObject.toInnerJsonObject() =
  InnerJsonObject(this)

fun JsonElement.toInnerJsonObject() =
  (this as? JsonObject)?.let { InnerJsonObject(this) }

interface JsonKey {
  val value: String
}
