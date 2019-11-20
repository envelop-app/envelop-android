package app.envelop.data.mappers

import app.envelop.data.models.UnsanitizedIndex
import app.envelop.data.models.User
import app.envelop.domain.UserService
import com.google.gson.JsonParser
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexSanitizerTest {

  private val username = "johnsmith"
  private val userService = mockk<UserService>().also {
    every { it.userSingle() } returns Single.just(
      User(username = username, decentralizedId = "", hubUrl = "", profile = null)
    )
  }
  private val subject = IndexSanitizer(userService)

  @Test
  fun sanitize() {
    val result = subject.sanitize(
      UnsanitizedIndex(
        JsonParser.parseString(
          """
        [{
         "id": "ABCDEF",
         "name": "file.pdf",
         "url": "UUID-UUID",
         "size": 1000,
         "created_at": "2019-10-10T12:34:56",
         "content_type": "pdf",
         "version": 2,
         "num_parts": 2,
         "part_ivs":["abc","abc"]
        }]
        """
        ).asJsonArray
      )
    ).blockingGet()


    assertEquals(username, result.docs.first().username)
  }
}
