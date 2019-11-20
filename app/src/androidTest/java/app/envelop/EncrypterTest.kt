package app.envelop

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.envelop.data.security.Base64Encoder
import app.envelop.data.security.KeyGenerator
import app.envelop.data.security.Pbkdf2AesEncrypter
import app.envelop.data.security.Pbkdf2AesEncryptionSpec
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncrypterTest {

  private val base64 = Base64Encoder()
  private val subject = Pbkdf2AesEncrypter(
    KeyGenerator(),
    base64
  )

  @Test
  fun encryptAndDecrypt() {
    val data = "Lorem ipsum"
    val passcode = "very_secure_password"
    val spec = Pbkdf2AesEncryptionSpec(
      salt = "envelop",
      iv = "YhgzuN+x+X0aWZ7P2pAsPw=="
    )

    val encryptResult = subject.encryptToBase64(data.toByteArray(Charsets.UTF_8), spec, passcode)
    assertEquals("PnUqCSCgEV8FSGQ=", encryptResult.data)

    val decryptResult = subject.decryptFromBase64(encryptResult.data, spec, passcode)
    assertEquals(data, decryptResult.toString(Charsets.UTF_8))
  }
}