package app.envelop.data.security

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyGeneratorTest {

  private val subject = KeyGenerator()

  @Test
  fun generate() {
    val key = subject.generate(
      Pbkdf2AesEncryptionSpec(
        salt = "envelop",
        iv = ""
      ),
      "1234567890"
    )
    val base64Key = TestBase64Encoder().encode(key.key)
    println("Key: $base64Key")
    assertEquals("UO5jyuBhLcLG2roF53OWtQzhdTInmVYgxvMn3egcXqA=", base64Key)
  }
}