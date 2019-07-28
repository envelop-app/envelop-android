package app.envelop.data

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.text.SimpleDateFormat
import java.util.*

data class InnerJsonObject(
  val json: JsonObject = JsonObject()
) {

  private val dateTimeFormat by lazy {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
  }

  fun getString(key: JsonKey) = optString(key)!!
  fun optString(key: JsonKey) = (json.get(key.value) as? JsonPrimitive)?.asString
  fun setString(key: JsonKey, value: String?) = json.addProperty(key.value, value)

  fun getLong(key: JsonKey) = optLong(key)!!
  fun optLong(key: JsonKey) = (json.get(key.value) as? JsonPrimitive)?.asLong
  fun setLong(key: JsonKey, value: Long?) = json.addProperty(key.value, value)

  fun getInt(key: JsonKey) = optInt(key)!!
  fun optInt(key: JsonKey) = (json.get(key.value) as? JsonPrimitive)?.asInt
  fun setInt(key: JsonKey, value: Int?) = json.addProperty(key.value, value)

  fun getBoolean(key: JsonKey) = optBoolean(key)!!
  fun optBoolean(key: JsonKey) = (json.get(key.value) as? JsonPrimitive)?.asBoolean
  fun setBoolean(key: JsonKey, value: Boolean?) = json.addProperty(key.value, value)

  fun getDate(key: JsonKey) = optDate(key)!!
  fun optDate(key: JsonKey): Date? = optString(key)?.let { dateTimeFormat.parse(it) }
  fun setDate(key: JsonKey, value: Date?) = setString(key, value?.let { dateTimeFormat.format(it) })

}

interface JsonKey {
  val value: String
}