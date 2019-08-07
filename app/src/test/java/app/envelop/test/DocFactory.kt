package app.envelop.test

import app.envelop.data.models.Doc

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

}