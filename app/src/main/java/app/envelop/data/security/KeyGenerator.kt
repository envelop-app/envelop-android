package app.envelop.data.security

import org.spongycastle.crypto.PBEParametersGenerator.PKCS5PasswordToUTF8Bytes
import org.spongycastle.crypto.digests.SHA256Digest
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.spongycastle.crypto.params.KeyParameter
import timber.log.Timber
import javax.inject.Inject

class KeyGenerator
@Inject constructor() {
  private val generator by lazy {
    PKCS5S2ParametersGenerator(SHA256Digest())
  }

  @Synchronized
  fun generate(spec: EncryptionSpec, passcode: String): EncryptionKey {
    if (spec !is Pbkdf2AesEncryptionSpec) throw UnsupportedOperationException("Spec not supported")
    return generate(spec, passcode)
  }

  @Synchronized
  private fun generate(spec: Pbkdf2AesEncryptionSpec, passcode: String): EncryptionKey {
    val time = System.currentTimeMillis()
    val key = EncryptionKey(
      generator.let {
        it.init(PKCS5PasswordToUTF8Bytes(passcode.toCharArray()), spec.salt.toByteArray(), spec.keyIterations)
        (it.generateDerivedMacParameters(Pbkdf2AesEncryptionSpec.KEY_SIZE) as KeyParameter).key
      }
    )
    Timber.d("Key generation time: %d", System.currentTimeMillis() - time)
    return key
  }

}