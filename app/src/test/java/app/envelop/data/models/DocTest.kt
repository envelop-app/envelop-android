package app.envelop.data.models

import app.envelop.test.DocFactory
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class DocTest {

  private val baseDoc = DocFactory.build()

  @Test
  fun humanSize() {
    Locale.setDefault(Locale.US)
    assertEquals("0 B", baseDoc.copy(size = 0).humanSize)
    assertEquals("1.0 kB", baseDoc.copy(size = 1_000).humanSize)
    assertEquals("1.2 kB", baseDoc.copy(size = 1_234).humanSize)
    assertEquals("10.0 MB", baseDoc.copy(size = 10_000_000).humanSize)
  }

  @Test
  fun fromJson() {
    Doc.build(
      Gson().fromJson(
        """
        {
         "id": "ABCDEF",
         "name": "file.pdf",
         "url": "UUID-UUID",
         "size": 1000,
         "created_at": "2019-10-10T12:34:56",
         "content_type": "pdf",
         "version": 2,
         "num_parts": 2,
         "part_ivs": ["abc","abc"],
         "username": "username"
        }
      """, JsonObject::class.java
      ).asJsonObject
    ).run {
      assertEquals("ABCDEF", id)
      assertEquals("file.pdf", name)
      assertEquals("UUID-UUID", url)
      assertEquals(1_000, size)
      assertEquals(
        10,
        Calendar.getInstance().also { it.time = createdAt }.get(Calendar.DAY_OF_MONTH)
      )
      assertEquals("pdf", contentType)
      assertEquals(2, version)
      assertEquals(2, numParts)
      assertEquals(2, partIVs?.size)
    }
  }

  @Test
  fun keepUnknownFields() {
    val doc = Doc.build(
      Gson().fromJson(
        """
        {
         "id": "ABCDEF",
         "name": "file.pdf",
         "url": "UUID-UUID",
         "size": 1000,
         "created_at": "2019-10-10T12:34:56",
         "num_parts": 2,
         "username": "username",
         "new_field": "new_value"
        }
      """, JsonObject::class.java
      ).asJsonObject
    )
    assertEquals(
      "new_value",
      doc.toJsonObject().json.getAsJsonPrimitive("new_field").asString
    )
  }

}