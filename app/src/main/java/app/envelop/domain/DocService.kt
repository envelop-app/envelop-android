package app.envelop.domain

import app.envelop.common.Optional
import app.envelop.common.doIfSuccessful
import app.envelop.common.flatMapCompletableIfSuccessful
import app.envelop.common.flatMapIfSuccessful
import app.envelop.common.rx.observeOnIO
import app.envelop.data.models.Doc
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.RemoteRepository
import javax.inject.Inject

class DocService
@Inject constructor(
  private val docRepository: DocRepository,
  private val indexService: IndexService,
  private val remoteRepository: RemoteRepository
) {

  fun get(id: String) =
    docRepository
      .get(id)
      .map { Optional.create(it.firstOrNull()) }
      .toObservable()

  fun delete(doc: Doc) =
    remoteRepository
      .deleteFile(doc.url)
      .flatMapIfSuccessful { remoteRepository.deleteFile(doc.id) }
      .doIfSuccessful { docRepository.delete(doc) }
      .flatMapCompletableIfSuccessful { indexService.upload() }

}