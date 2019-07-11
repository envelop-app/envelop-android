package app.envelop.domain

import app.envelop.common.FileHandler
import app.envelop.common.doIfSuccessful
import app.envelop.data.models.UploadPart
import app.envelop.data.models.UploadState
import app.envelop.data.models.UploadWithDoc
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.RemoteRepository
import app.envelop.data.repositories.UploadRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadService
@Inject constructor(
  private val remoteRepository: RemoteRepository,
  private val docRepository: DocRepository,
  private val uploadRepository: UploadRepository,
  private val fileHandler: FileHandler,
  private val updateDocRemotely: UpdateDocRemotely
) {

  private val disposables = CompositeDisposable()
  private val state = BehaviorSubject.createDefault<UploadState>(UploadState.Idle)

  fun state() = state.hide()

  fun startUpload() {
    if (state.value !is UploadState.Idle) return

    updateUploadState()
      .filter { it is UploadState.Uploading }
      .flatMapCompletable {
        when (it) {
          is UploadState.Uploading -> uploadNextFile(it.nextUpload)
          is UploadState.Idle -> Completable.complete()
        }
      }
      .repeatUntil { state.value is UploadState.Idle }
      .subscribe({}, {
        Timber.e(it, "Upload error")
        state.onNext(UploadState.Idle)
      })
      .addTo(disposables)
  }

  fun stopUpload() {
    state.onNext(UploadState.Idle)
    disposables.clear()
  }

  private fun getUploadState() =
    uploadRepository
      .getAll()
      .map { list -> list.mapNotNull { it.build() } }
      .toObservable()
      .take(1)
      .map { filesToUpload ->
        if (filesToUpload.isEmpty()) {
          UploadState.Idle
        } else {
          UploadState.Uploading(
            filesToUpload.size,
            filesToUpload.first()
          )
        }
      }

  private fun updateUploadState() =
    getUploadState()
      .doOnNext(state::onNext)

  private fun uploadNextFile(item: UploadWithDoc) =
    Observable
      .fromIterable(item.missingParts)
      .concatMapSingle { uploadPart ->
        uploadPart(uploadPart)
          .flatMap {
            if (it.isError) throw UploadPartError(it.throwable())
            markPartAsUploaded(item, uploadPart.docPart.part)
              .toSingleDefault(uploadPart)
          }
      }
      .flatMap { updateUploadState() }
      .ignoreElements()
      .andThen(markFileAsUploaded(item))

  private fun uploadPart(part: UploadPart) =
    Single
      .fromCallable {
        fileHandler.localFileToByteArray(part.fileUri, part.partStart, part.partSize)
      }
      .flatMap {
        remoteRepository.uploadByteArray(part.destinationUrl, it)
      }

  private fun markPartAsUploaded(item: UploadWithDoc, part: Int) =
    uploadRepository
      .get(item.upload.id)
      .toObservable()
      .take(1)
      .filter { it.isNotEmpty() }
      .map { it.first() }
      .doOnNext {
        uploadRepository.save(
          it.copy(partsUploaded = it.partsUploaded + part)
        )
      }
      .ignoreElements()

  private fun markFileAsUploaded(item: UploadWithDoc) =
    Single
      .just(item.doc.copy(uploaded = true))
      .doOnSuccess(docRepository::save)
      .flatMap(updateDocRemotely::update)
      .doIfSuccessful {
        fileHandler.deleteLocalFile(item.upload.fileUri)
        uploadRepository.delete(item.upload)
      }
      .ignoreElement()

  class UploadPartError(throwable: Throwable) : Exception(throwable)

}