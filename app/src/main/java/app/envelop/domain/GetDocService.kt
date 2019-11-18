package app.envelop.domain

import app.envelop.common.Optional
import app.envelop.data.models.Upload
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.UploadRepository
import io.reactivex.Observable
import javax.inject.Inject

class GetDocService
@Inject constructor(
  private val docRepository: DocRepository,
  private val uploadRepository: UploadRepository
) {

  fun get(id: String) =
    docRepository
      .get(id)

  fun getUpload(docId: String): Observable<Optional<Upload>> =
    uploadRepository
      .getByDocId(docId)
      .map { Optional.create(it.firstOrNull()) }
      .toObservable()

}