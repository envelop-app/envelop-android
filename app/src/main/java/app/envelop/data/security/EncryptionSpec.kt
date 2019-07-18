package app.envelop.data.security

import app.envelop.data.InnerJsonObject
import app.envelop.data.JsonKey

interface EncryptionSpec {
  fun toJson(baseJson: InnerJsonObject = InnerJsonObject()): InnerJsonObject
}

data class Pbkdf2AesEncryptionSpec(
  val keyIterations: Int = DEFAULT_KEY_ITERATIONS,
  val salt: String,
  val iv: String
) : EncryptionSpec {

  override fun toJson(baseJson: InnerJsonObject) =
    baseJson.copy().also { json ->
      json.set(
        Key.Type,
        TYPE
      )
      json.set(
        Key.Params,
        json.optObjectOrEmpty(Key.Params).also { paramsJson ->
          paramsJson.set(Key.KeyIterations, keyIterations)
          paramsJson.set(Key.Salt, salt)
          paramsJson.set(Key.IV, iv)
        }
      )
    }

  companion object {
    private const val TYPE = "PBKDF2/AES"
    const val DEFAULT_KEY_ITERATIONS = 10_000
    const val KEY_SIZE = 256 // bits
    const val IV_SIZE = 16 // bytes

    fun fromJson(json: InnerJsonObject) =
      if (json.optString(Key.Type) == TYPE) {
        json.optObject(Key.Params)
          ?.let { paramsJson ->
            val keyIterations = paramsJson.optInt(Key.KeyIterations)
            val salt = paramsJson.optString(Key.Salt)
            val iv = paramsJson.optString(Key.IV)

            if (keyIterations != null && salt != null && iv != null) {
              Pbkdf2AesEncryptionSpec(keyIterations, salt, iv)
            } else null
          }
      } else null
  }

  enum class Key(override val value: String) : JsonKey {
    Type("type"), Params("params"),
    KeyIterations("key_iterations"), Salt("salt"), IV("iv")
  }
}

object EncryptionSpecProvider {

  fun getSpec(json: InnerJsonObject?) =
    json?.let {
      Pbkdf2AesEncryptionSpec.fromJson(json)
    }

}