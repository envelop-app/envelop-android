package app.envelop.ui.main

import app.envelop.common.Optional
import app.envelop.domain.DeleteDocService
import app.envelop.domain.DocLinkBuilder
import app.envelop.domain.GetDocService
import app.envelop.test.DocFactory
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class DocMenuViewModelTest {

  private val getDocService = mock<GetDocService>()
  private val deleteDocService = mock<DeleteDocService>()
  private val docLinkBuilder = mock<DocLinkBuilder>()

  private lateinit var viewModel: DocMenuViewModel

  @Before
  fun setUp() {
    viewModel = DocMenuViewModel(getDocService, deleteDocService, docLinkBuilder)
    whenever(docLinkBuilder.build(any())).thenReturn("")
    whenever(getDocService.get(any())).thenReturn(Observable.just(Optional.Some(DocFactory.build())))
  }

  @Test
  fun doc() {
    val doc = DocFactory.build()
    whenever(getDocService.get(any())).thenReturn(Observable.just(Optional.Some(doc)))

    viewModel.docIdReceived("DOC")

    viewModel.doc().test().assertValue(doc)
  }

  @Test
  fun docDeleted() {
    val doc = DocFactory.build().copy(deleted = true)
    whenever(getDocService.get(any())).thenReturn(Observable.just(Optional.Some(doc)))

    viewModel.docIdReceived("DOC")

    viewModel.finish().test().awaitCount(1)
  }

  @Test
  fun docDoesNotExist() {
    whenever(getDocService.get(any())).thenReturn(Observable.just(Optional.None))

    viewModel.docIdReceived("DOC")

    viewModel.finish().test().awaitCount(1)
  }

  @Test
  fun docLink() {
    val link = "https://envl.app/user/abced"
    whenever(docLinkBuilder.build(any())).thenReturn(link)

    viewModel.docIdReceived("DOC")

    viewModel.link().test().assertValue(link)
  }
}
