package app.envelop.domain

import app.envelop.data.repositories.DocRepository
import app.envelop.data.security.HashGenerator
import app.envelop.test.DocFactory
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.Assert.assertEquals
import org.junit.Test

class DocIdGeneratorTest {

  @Test
  fun generate() {
    val hashGenerator = mockk<HashGenerator>()
    val docRepository = mockk<DocRepository>()
    val subject = DocIdGenerator(hashGenerator, docRepository)

    every { hashGenerator.generate(any()) } returnsMany listOf("A", "B", "C")
    every { docRepository.list() } returns Observable.just(
      listOf(
        DocFactory.build().copy(id = "A"),
        DocFactory.build().copy(id = "B")
      )
    )

    assertEquals("C", subject.generate())
  }
}