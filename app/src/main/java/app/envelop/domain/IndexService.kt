package app.envelop.domain

import app.envelop.common.*
import app.envelop.data.mappers.IndexSanitizer
import app.envelop.data.models.Doc
import app.envelop.data.models.Index
import app.envelop.data.models.UnsanitizedIndex
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.RemoteRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class IndexService
@Inject constructor(
  private val docRepository: DocRepository,
  private val remoteRepository: RemoteRepository,
  private val indexSanitizer: IndexSanitizer
) {

  fun download(docsToKeep: List<Doc> = emptyList(), docToIgnore: List<Doc> = emptyList()) =
    remoteRepository
      .getJson(INDEX_FILE_NAME, UnsanitizedIndex::class, true)
      .flatMapIfSuccessful { opt ->
        when (opt) {
          is Optional.Some -> indexSanitizer.sanitize(opt.element).map { Operation.success(it.docs) }
          is Optional.None -> Single.just(Operation.success(emptyList()))
        }
      }
      .flatMapCompletableIfSuccessful { docsReceived ->
        val docsToIgnoreIds = (docToIgnore + docsToKeep).map { it.id }
        docRepository.replace(
          docsReceived.filterNot { docsToIgnoreIds.contains(it.id) }
              + docsToKeep
        )
      }

  fun uploadKeepingDoc(docToUpload: Doc) =
    download(docsToKeep = listOf(docToUpload))
      .flatMapIfSuccessful { upload() }

  fun uploadIgnoringDoc(docToIgnore: Doc) =
    download(docToIgnore = listOf(docToIgnore))
      .flatMapIfSuccessful { upload() }

  fun get() =
    docRepository.listVisible()

  private fun upload() =
    docRepository
      .list()
      .firstOrError()
      .flatMap { remoteRepository.uploadJson(INDEX_FILE_NAME, Index(it), true) }
      .subscribeOn(Schedulers.io())

  companion object {
    private const val INDEX_FILE_NAME = "index"
  }

}
