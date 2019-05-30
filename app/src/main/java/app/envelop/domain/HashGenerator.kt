package app.envelop.domain

import java.security.SecureRandom
import javax.inject.Inject

class HashGenerator
@Inject constructor() {

  private val random by lazy {
    SecureRandom()
  }

  fun generate(size: Int) =
    (0..size).map {
      HASH_CHARS[random.nextInt(HASH_CHARS.length)]
    }.joinToString("")

  companion object {
    private const val HASH_CHARS = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz01234567890"
  }
}