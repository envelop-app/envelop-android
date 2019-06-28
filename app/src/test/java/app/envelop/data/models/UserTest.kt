package app.envelop.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

class UserTest {

  private val baseUser = User(
    username = "",
    decentralizedId = "",
    hubUrl = "",
    profile = null
  )

  @Test
  fun usernameShort() {
    baseUser.copy(username = "johnsmith.id.blockstack").also {
      assertEquals("johnsmith", it.usernameShort)
    }

    baseUser.copy(username = "johnsmith.id.example").also {
      assertEquals("johnsmith.id.example", it.usernameShort)
    }
  }
}