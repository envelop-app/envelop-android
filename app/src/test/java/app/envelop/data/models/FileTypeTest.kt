package app.envelop.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

class FileTypeTest {

  @Test
  fun fromContentType() {
    assertEquals(FileType.Image, FileType.fromContentType("png"))
    assertEquals(FileType.Archive, FileType.fromContentType("zip"))
    assertEquals(FileType.Video, FileType.fromContentType("mov"))
    assertEquals(FileType.PDF, FileType.fromContentType("pdf"))
    assertEquals(FileType.Audio, FileType.fromContentType("mp3"))
    assertEquals(FileType.Default, FileType.fromContentType("any"))
  }

}