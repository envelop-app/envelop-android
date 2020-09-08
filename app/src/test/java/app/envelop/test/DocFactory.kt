package app.envelop.test

import app.envelop.data.models.Doc
import app.envelop.data.security.Pbkdf2AesEncryptionSpec

object DocFactory {

  fun build() =
    Doc(
      id = "ABCDEF",
      name = "file.pdf",
      url = "UUID-UUID",
      size = 1_000,
      contentType = null,
      numParts = 1,
      username = "",
      encryptionSpec = null
    )

  fun fivePartsBuild() =
    Doc(
      id = "ABCDEF",
      name = "file.pdf",
      url = "UUID-UUID",
      size = 5_000,
      contentType = null,
      numParts = 5,
      username = "",
      encryptionSpec = Pbkdf2AesEncryptionSpec(salt = "abc", iv = "hello"),
      partIVs = listOf("1", "2", "3", "4", "5"),
      passcode = "abc"
    )

}
