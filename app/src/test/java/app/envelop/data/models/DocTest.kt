package app.envelop.data.models

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class DocTest {

  private val baseDoc = Doc(
    id = "ABCDEF",
    name = "file.pdf",
    url = "UUID-UUID",
    size = 1_000,
    contentType = null,
    numParts = 1
  )

  @Test
  fun humanSize() {
    assertEquals("0 B", baseDoc.copy(size = 0).humanSize)
    assertEquals("1.0 kB", baseDoc.copy(size = 1_000).humanSize)
    assertEquals("1.2 kB", baseDoc.copy(size = 1_234).humanSize)
    assertEquals("10.0 MB", baseDoc.copy(size = 10_000_000).humanSize)
  }

  @Test
  fun fromJson() {
    Doc.build(
      JsonParser().parse(
        """
        {
         "id": "ABCDEF",
         "name": "file.pdf",
         "url": "UUID-UUID",
         "size": 1000,
         "created_at": "2019-10-10T12:34:56",
         "content_type": "pdf",
         "num_parts": 2
        }
      """
      ).asJsonObject
    ).run {
      assertEquals("ABCDEF", id)
      assertEquals("file.pdf", name)
      assertEquals("UUID-UUID", url)
      assertEquals(1_000, size)
      assertEquals(10, Calendar.getInstance().also { it.time = createdAt }.get(Calendar.DAY_OF_MONTH))
      assertEquals("pdf", contentType)
      assertEquals(2, numParts)
    }
  }

  @Test
  fun keepUnknownFields() {
    val doc = Doc.build(
      JsonParser().parse(
        """
        {
         "id": "ABCDEF",
         "name": "file.pdf",
         "url": "UUID-UUID",
         "size": 1000,
         "created_at": "2019-10-10T12:34:56",
         "num_parts": 2,
         "new_field": "new_value"
        }
      """
      ).asJsonObject
    )
    assertEquals(
      "new_value",
      doc.toJsonObject().getAsJsonPrimitive("new_field").asString
    )
  }
}