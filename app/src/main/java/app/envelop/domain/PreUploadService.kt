package app.envelop.domain

import android.content.Context
import android.net.Uri
import app.envelop.background.UploadBackgroundService
import app.envelop.common.*
import app.envelop.common.rx.observeOnIO
import app.envelop.data.models.Doc
import app.envelop.data.models.Upload
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.RemoteRepository
import app.envelop.data.repositories.UploadRepository
import io.reactivex.Single
import javax.inject.Inject

class PreUploadService
@Inject constructor(
  private val context: Context,
  private val remoteRepository: RemoteRepository,
  private val docBuilder: DocBuilder,
  private val docRepository: DocRepository,
  private val uploadRepository: UploadRepository,
  private val indexService: IndexService,
  private val fileHandler: FileHandler
) {

  fun prepareUpload(fileUri: Uri): Single<Operation<Doc>> =
    docBuilder
      .build(fileUri)
      .doIfSuccessful { docRepository.save(it) }
      .flatMapIfSuccessful { updateRemotely(it) }
      .flatMapIfSuccessful { doc ->
        fileHandler
          .saveFileLocally(fileUri)
          .doIfSuccessful { localFileUri -> uploadRepository.save(doc.toUpload(localFileUri)) }
          .mapIfSuccessful { doc }
      }
      .doIfSuccessful { startBackgroundService() }

  fun startBackgroundIfNeeded() =
    uploadRepository
      .count()
      .take(1)
      .filter { it > 0 }
      .doOnNext { startBackgroundService() }
      .ignoreElements()

  private fun updateRemotely(doc: Doc) =
    uploadDocJson(doc)
      .flatMap { indexService.upload().toSingleDefault(it) }

  private fun uploadDocJson(doc: Doc) =
    remoteRepository.uploadJson(doc.id, doc, false)

  private fun startBackgroundService() {
    context.startService(UploadBackgroundService.getIntent(context))
  }

  private fun Doc.toUpload(fileUri: Uri) =
    Upload(
      docId = id,
      fileUriPath = fileUri.toString(),
      partSize = DocBuilder.FILE_PART_SIZE
    )

  class Error(message: String?) : Exception(message)

}