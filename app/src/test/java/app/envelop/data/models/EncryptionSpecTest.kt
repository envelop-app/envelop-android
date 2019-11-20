package app.envelop.data.models

import app.envelop.data.security.Pbkdf2AesEncryptionSpec
import app.envelop.data.toInnerJsonObject
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.*
import org.junit.Test

class EncryptionSpecTest {

  @Test
  fun unknownType() {
    assertNull(
      Pbkdf2AesEncryptionSpec.fromJson(
        Gson().fromJson(
          """
          {
            "type": "unknown",
            "params": {}
          }
          """, JsonObject::class.java
        ).toInnerJsonObject()
      )
    )
  }

  @Test
  fun pbkdf2Aes_parsing() {
    val spec = Pbkdf2AesEncryptionSpec.fromJson(
      Gson().fromJson(
        """
             {
               "type": "PBKDF2/AES",
               "params": {
                 "key_iterations": 10000,
                 "salt": "f98cnhd132",
                 "iv": "5432n7542n754n78n7985=="
               }
             }
             """, JsonObject::class.java
      ).toInnerJsonObject()
    )

    assertNotNull(spec!!)
    assertEquals(10000, spec.keyIterations)
    assertEquals("f98cnhd132", spec.salt)
    assertEquals("5432n7542n754n78n7985==", spec.iv)
  }

  @Test
  fun pbkdf2Aes_keepUnknownValues() {
    val baseJson = Gson().fromJson(
      """
             {
               "type": "PBKDF2/AES",
               "params": {
                 "key_iterations": 10000,
                 "salt": "f98cnhd132",
                 "iv": "5432n7542n754n78n7985==",
                 "new_key": "new_value"
               }
             }
             """, JsonObject::class.java
    ).toInnerJsonObject()
    val spec = Pbkdf2AesEncryptionSpec.fromJson(baseJson)!!

    assertEquals(
      "new_value",
      spec
        .toJson(baseJson)
        .json
        .getAsJsonObject("params")
        .getAsJsonPrimitive("new_key")
        .asString
    )
  }
}