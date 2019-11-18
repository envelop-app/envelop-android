package app.envelop.data.security

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class EncrypterProvider
@Inject constructor(
  private val pbkdf2AesEncrypter: Pbkdf2AesEncrypter
) {

  fun get(spec: EncryptionSpec?) =
    when (spec) {
      is Pbkdf2AesEncryptionSpec -> pbkdf2AesEncrypter
      else -> null
    }

  fun getOrError(spec: EncryptionSpec?): Encrypter =
    when (spec) {
      is Pbkdf2AesEncryptionSpec -> pbkdf2AesEncrypter
      else -> throw UnsupportedOperationException("Unsupported encryption spec")
    }

}

abstract class Encrypter(
  private val keyGenerator: KeyGenerator,
  private val base64: Base64Encoder
) {

  // Encrypt

  abstract fun encrypt(input: ByteArray, spec: EncryptionSpec, key: EncryptionKey): Result

  private fun encrypt(input: ByteArray, spec: EncryptionSpec, passcode: String) =
    encrypt(input, spec, keyGenerator.generate(spec, passcode))

  fun encryptToBase64(input: ByteArray, spec: EncryptionSpec, passcode: String) =
    encrypt(input, spec, passcode)
      .toBase64(base64)

  // Decrypt

  abstract fun decrypt(input: ByteArray, spec: EncryptionSpec, key: EncryptionKey): ByteArray

  fun decryptFromBase64(input: String, spec: EncryptionSpec, passcode: String) =
    decrypt(
      input = base64.decode(input),
      spec = spec,
      key = keyGenerator.generate(spec, passcode)
    )

  class Result(
    val data: ByteArray
  ) {
    fun toBase64(base64: Base64Encoder) = ResultBase64(
      base64.encode(data)
    )
  }

  class ResultBase64(
    val data: String
  )

}


class Pbkdf2AesEncrypter
@Inject constructor(
  keyGenerator: KeyGenerator,
  private val base64: Base64Encoder
) : Encrypter(keyGenerator, base64) {

  private val cipher by lazy {
    Cipher.getInstance("AES/CTR/NoPadding")
  }

  // Encrypt

  @Synchronized
  override fun encrypt(input: ByteArray, spec: EncryptionSpec, key: EncryptionKey) =
    with(cipher) {
      init(
        Cipher.ENCRYPT_MODE,
        buildAESKey(key),
        IvParameterSpec(base64.decode((spec as Pbkdf2AesEncryptionSpec).iv))
      )
      Result(doFinal(input))
    }

  // Decrypt

  @Synchronized
  override fun decrypt(input: ByteArray, spec: EncryptionSpec, key: EncryptionKey): ByteArray =
    with(cipher) {
      init(
        Cipher.DECRYPT_MODE,
        buildAESKey(key),
        IvParameterSpec(base64.decode((spec as Pbkdf2AesEncryptionSpec).iv))
      )
      doFinal(input)
    }

  // Helpers

  private fun buildAESKey(key: EncryptionKey) =
    SecretKeySpec(key.key, "AES")

}