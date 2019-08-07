package app.envelop.data.security

import org.junit.Assert.assertEquals
import org.junit.Test

class IVGeneratorTest {

  private val generator = IVGenerator(TestBase64Encoder())

  @Test
  fun generate() {
    assertEquals(generator.generate(10).size, 10)
  }

  @Test
  fun generateList() {
    assertEquals(generator.generateList(3, 10).size, 3)
  }
}