package app.envelop.data.models

import org.junit.Assert.*
import org.junit.Test

class DocTest {

  @Test
  fun humanSize() {
    assertEquals("0 B", Doc(size = 0).humanSize)
    assertEquals("1.0 kB", Doc(size = 1_000).humanSize)
    assertEquals("1.2 kB", Doc(size = 1_234).humanSize)
    assertEquals("10.0 MB", Doc(size = 10_000_000).humanSize)
  }
}