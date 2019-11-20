package app.envelop.data.security

import java.util.*

class TestBase64Encoder : Base64Encoder() {
  override fun encode(input: ByteArray) =
    Base64.getEncoder().encode(input).toString(Charsets.US_ASCII)

  override fun decode(input: String): ByteArray =
    Base64.getDecoder().decode(input)
}