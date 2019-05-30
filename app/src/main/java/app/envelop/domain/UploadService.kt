package app.envelop.domain

import android.net.Uri
import app.envelop.common.Operation
import app.envelop.common.Optional
import app.envelop.common.rx.observeOnIO
import app.envelop.data.models.Doc
import app.envelop.data.repositories.RemoteRepository
import app.envelop.data.repositories.DocRepository
import io.reactivex.Single
import javax.inject.Inject

class UploadService
@Inject constructor(
  private val remoteRepository: RemoteRepository,
  private val docBuilder: DocBuilder,
  private val docRepository: DocRepository,
  private val indexService: IndexService
) {

  fun upload(fileUri: Uri) =
    docBuilder.build(fileUri)
      .flatMap { uploadDocFile(it, fileUri) }
      .flatMap { uploadDocJson(it) }
      .observeOnIO()
      .doOnSuccess { saveDoc(it) }
      .flatMap { indexService.upload().toSingleDefault(it) }

  private fun uploadDocFile(doc: Optional<Doc>, fileUri: Uri) =
    if (doc is Optional.Some) {
      remoteRepository.uploadDoc(doc.element, fileUri)
    } else {
      Single.just(Operation.error(Error("Could not access file and build doc")))
    }

  private fun uploadDocJson(doc: Operation<Doc>) =
    if (doc.isSuccessful) {
      remoteRepository
        .uploadJson(doc.result().id, doc.result(), false)
        .map { doc }
    } else {
      Single.just(doc)
    }

  private fun saveDoc(doc: Operation<Doc>) {
    if (doc.isSuccessful) {
      docRepository.save(doc.result())
    }
  }

  class Error(message: String?) : Exception(message)

}