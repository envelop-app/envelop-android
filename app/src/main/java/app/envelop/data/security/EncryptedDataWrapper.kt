package app.envelop.data.security

import com.google.gson.JsonObject

data class EncryptedDataWrapper(
  val payload: String,
  val encryption: JsonObject
)