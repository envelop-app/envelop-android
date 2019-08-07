package app.envelop.data.security

import java.security.SecureRandom
import javax.inject.Inject

class IVGenerator
@Inject constructor(
  private val base64Encoder: Base64Encoder
) {

  private val random by lazy {
    SecureRandom()
  }

  fun generate(ivSize: Int) =
    ByteArray(ivSize).also {
      random.nextBytes(it)
    }

  fun generateInBase64(ivSize: Int) =
    base64Encoder.encode(generate(ivSize))

  fun generateList(count: Int, ivSize: Int) =
    (1..count).map {
      generate(ivSize)
    }

  fun generateListInBase64(count: Int, ivSize: Int) =
    generateList(count, ivSize).map {
      base64Encoder.encode(it)
    }

}