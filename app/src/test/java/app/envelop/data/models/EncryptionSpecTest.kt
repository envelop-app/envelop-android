package app.envelop.data.models

import app.envelop.data.security.Pbkdf2AesEncryptionSpec
import app.envelop.data.toInnerJsonObject
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test

class EncryptionSpecTest {

  @Test
  fun unknownType() {
    assertNull(
      Pbkdf2AesEncryptionSpec.fromJson(
        JsonParser().parse(
          """
          {
            "type": "unknown",
            "params": {}
          }
          """
        ).toInnerJsonObject()!!
      )
    )
  }

  @Test
  fun pbkdf2Aes_parsing() {
    val spec = Pbkdf2AesEncryptionSpec.fromJson(
      JsonParser().parse(
        """
             {
               "type": "PBKDF2/AES",
               "params": {
                 "key_iterations": 10000,
                 "salt": "f98cnhd132",
                 "iv": "5432n7542n754n78n7985=="
               }
             }
             """
      ).toInnerJsonObject()!!
    )

    assertNotNull(spec!!)
    assertEquals(10000, spec.keyIterations)
    assertEquals("f98cnhd132", spec.salt)
    assertEquals("5432n7542n754n78n7985==", spec.iv)
  }

  @Test
  fun pbkdf2Aes_keepUnknownValues() {
    val baseJson = JsonParser().parse(
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
             """
    ).toInnerJsonObject()!!
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