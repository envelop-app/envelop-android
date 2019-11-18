package app.envelop.domain

import android.content.Context
import android.net.Uri
import app.envelop.background.UploadBackgroundService
import app.envelop.common.*
import app.envelop.data.models.Doc
import app.envelop.data.models.Upload
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.UploadRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class PreUploadService
@Inject constructor(
  private val context: Context,
  private val docBuilder: DocBuilder,
  private val docRepository: DocRepository,
  private val uploadRepository: UploadRepository,
  private val fileHandler: FileHandler,
  private val updateDocRemotely: UpdateDocRemotely
) {

  fun prepareUpload(fileUri: Uri): Single<Operation<Doc>> =
    docBuilder
      .build(fileUri)
      .flatMapIfSuccessful { docRepository.save(it).toSingleDefault(Operation.success(it)) }
      .flatMapIfSuccessful { updateDocRemotely.update(it) }
      .flatMapIfSuccessful { doc ->
        fileHandler
          .saveFileLocally(fileUri)
          .doIfSuccessful { localFileUri -> uploadRepository.save(doc.toUpload(localFileUri)) }
          .mapIfSuccessful { doc }
      }
      .doIfSuccessful { startBackgroundService() }

  fun startBackgroundIfNeeded(): Completable =
    uploadRepository
      .count()
      .take(1)
      .filter { it > 0 }
      .doOnNext { startBackgroundService() }
      .ignoreElements()

  private fun startBackgroundService() {
    context.startService(UploadBackgroundService.getIntent(context))
  }

  private fun Doc.toUpload(fileUri: Uri) =
    Upload(
      docId = id,
      fileUriPath = fileUri.toString(),
      partSize = EnvelopSpec.FILE_PART_SIZE
    )

  class Error(message: String?) : Exception(message)

}