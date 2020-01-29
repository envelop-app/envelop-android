package app.envelop.domain

import app.envelop.data.repositories.DocRepository
import app.envelop.data.security.HashGenerator
import app.envelop.test.DocFactory
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Assert.assertEquals
import org.junit.Test

class DocIdGeneratorTest {

  @Test
  fun generate() {
    val hashGenerator = mock<HashGenerator>()
    val docRepository = mock<DocRepository>()
    val subject = DocIdGenerator(hashGenerator, docRepository)

    whenever(hashGenerator.generate(any())).thenReturn("A", "B", "C")
    whenever(docRepository.list()).thenReturn(
      Observable.just(
        listOf(
          DocFactory.build().copy(id = "A"),
          DocFactory.build().copy(id = "B")
        )
      )
    )

    assertEquals("C", subject.generate())
  }
}
