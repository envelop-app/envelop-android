package app.envelop.domain

import app.envelop.common.flatMapIfSuccessful
import app.envelop.common.mapIfSuccessful
import app.envelop.data.models.Doc
import app.envelop.data.repositories.RemoteRepository
import io.reactivex.Single
import javax.inject.Inject

class UpdateDocRemotely
@Inject constructor(
  private val indexService: IndexService,
  private val remoteRepository: RemoteRepository
) {

  fun update(doc: Doc) =
    remoteRepository.uploadJson(doc.id, doc, false)
      .flatMapIfSuccessful { indexService.uploadKeepingDoc(doc) }
      .mapIfSuccessful { doc }


  fun delete(doc: Doc) =
    remoteRepository.deleteFile(doc.id)
      .flatMap {
        if (it.isSuccessful || it.is404) {
          indexService.uploadIgnoringDoc(doc)
        } else {
          Single.just(it)
        }
      }
      .mapIfSuccessful { doc }

}