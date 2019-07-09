package app.envelop.domain

import app.envelop.common.doIfSuccessful
import app.envelop.common.flatMapIfSuccessful
import app.envelop.data.models.Doc
import app.envelop.data.models.Index
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.RemoteRepository
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class IndexService
@Inject constructor(
  private val docRepository: DocRepository,
  private val remoteRepository: RemoteRepository
) {

  fun download(keepLocalDocs: List<Doc> = emptyList()) =
    remoteRepository
      .getJson(INDEX_FILE_NAME, Index::class, true)
      .doIfSuccessful {
        docRepository.replace((it.element()?.docs ?: emptyList()) + keepLocalDocs)
        remoteRepository.printListFiles()
      }

  fun uploadWithDoc(docToUpload: Doc) =
    download(listOf(docToUpload))
      .flatMapIfSuccessful { upload() }

  fun get() =
    docRepository.listVisible().toObservable()

  private fun upload() =
    docRepository
      .list()
      .firstOrError()
      .map { Index(it) }
      .flatMap {
        remoteRepository
          .uploadJson(INDEX_FILE_NAME, it, true)
      }
      .subscribeOn(Schedulers.io())

  companion object {
    private const val INDEX_FILE_NAME = "index"
  }

}