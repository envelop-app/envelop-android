package app.envelop.domain

import app.envelop.common.Optional
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.UploadRepository
import javax.inject.Inject

class GetDocService
@Inject constructor(
  private val docRepository: DocRepository,
  private val uploadRepository: UploadRepository
) {

  fun get(id: String) =
    docRepository
      .get(id)
      .map { Optional.create(it.firstOrNull()) }
      .toObservable()

  fun getUpload(docId: String) =
    uploadRepository
      .getByDocId(docId)
      .map { Optional.create(it.firstOrNull()) }
      .toObservable()

}