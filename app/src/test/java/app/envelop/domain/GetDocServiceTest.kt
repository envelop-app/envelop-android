package app.envelop.domain

import app.envelop.common.Operation
import app.envelop.data.models.Doc
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.RemoteRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetDocServiceTest {

  @MockK
  lateinit var docRepository: DocRepository
  @MockK
  lateinit var indexService: IndexService
  @MockK
  lateinit var remoteRepository: RemoteRepository
  @InjectMockKs
  lateinit var serviceGet: GetDocService

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
  }

  @Test
  fun delete_successful() {
    every { remoteRepository.deleteFile(any()) } returns Single.just(Operation.success(Unit))
    every { indexService.upload() } returns Completable.complete()

    assertTrue(
      serviceGet.delete(Doc()).blockingGet().isSuccessful
    )

    verify { docRepository.delete(any()) }
    verify { indexService.upload() }
  }

  @Test
  fun delete_error() {
    every { remoteRepository.deleteFile(any()) } returns Single.just(Operation.error(Exception()))

    assertTrue(
      serviceGet.delete(Doc()).blockingGet().isError
    )

    verify(exactly = 0) { docRepository.delete(any()) }
    verify(exactly = 0) { indexService.upload() }
  }
}