package app.envelop.data.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class HashGeneratorTest {

  private val generator = HashGenerator()

  @Test
  fun length() {
    arrayOf(1, 5, 10).forEach {
      assertEquals(it, generator.generate(it).length)
    }
  }

  @Test
  fun chars() {
    (Random().nextInt(10) + 1).also { length ->
      assertTrue(generator.generate(length).matches(Regex("[A-Za-z0-9]+")))
    }
  }
}