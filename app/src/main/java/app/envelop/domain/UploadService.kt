package app.envelop.domain

import app.envelop.common.FileHandler
import app.envelop.common.doIfSuccessful
import app.envelop.data.models.UploadPart
import app.envelop.data.models.UploadState
import app.envelop.data.models.UploadWithDoc
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.RemoteRepository
import app.envelop.data.repositories.UploadRepository
import app.envelop.data.security.EncrypterProvider
import app.envelop.data.security.EncryptionKey
import app.envelop.data.security.KeyGenerator
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadService
@Inject constructor(
  private val remoteRepository: RemoteRepository,
  private val docRepository: DocRepository,
  private val uploadRepository: UploadRepository,
  private val fileHandler: FileHandler,
  private val updateDocRemotely: UpdateDocRemotely,
  private val encrypterProvider: EncrypterProvider,
  private val keyGenerator: KeyGenerator
) {

  private val disposables = CompositeDisposable()
  private val state = BehaviorSubject.createDefault<UploadState>(UploadState.Idle)

  fun state(): Observable<UploadState> = state.hide()

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
        Timber.w(it, "Upload error")
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
      .toObservable()
      .take(1)
      .flatMap { filesToUpload ->
        if (filesToUpload.isEmpty()) {
          Observable.just(UploadState.Idle)
        } else {
          val firstUpload = filesToUpload.first()
          docRepository
            .get(firstUpload.docId)
            .take(1)
            .map {
              it.element()?.let { doc ->
                UploadState.Uploading(
                  filesToUpload.size,
                  UploadWithDoc(firstUpload, doc)
                )
              } ?: UploadState.Idle
            }
        }
      }

  private fun updateUploadState() =
    getUploadState()
      .doOnNext(state::onNext)

  private fun uploadNextFile(item: UploadWithDoc) =
    Single
      .fromCallable { keyGenerator.generate(item.baseEncryptionSpec, item.passcode) }
      .flatMapObservable { key ->
        Observable
          .fromIterable(item.missingParts)
          .concatMapSingle { uploadPart ->
            uploadPart(uploadPart, key)
              .flatMap {
                if (it.isError) throw UploadPartError(it.throwable())
                markPartAsUploaded(item, uploadPart.part)
                  .toSingleDefault(uploadPart)
              }
          }
          .flatMap { updateUploadState() }
      }
      .ignoreElements()
      .andThen(markFileAsUploaded(item))

  private fun uploadPart(part: UploadPart, key: EncryptionKey) =
    Single
      .fromCallable {
        fileHandler.localFileToByteArray(part.fileUri, part.partStart, part.partSize)
      }
      .flatMap { partData ->
        encryptAndUploadPart(part, key, partData)
      }

  private fun encryptAndUploadPart(part: UploadPart, key: EncryptionKey, partData: ByteArray) =
    remoteRepository.uploadByteArray(
      part.destinationUrl,
      encrypterProvider.getOrError(part.encryptionSpec).encrypt(partData, part.encryptionSpec, key).data,
      false
    )

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
      .flatMap { docRepository.save(it).toSingleDefault(it) }
      .flatMap(updateDocRemotely::update)
      .doIfSuccessful {
        fileHandler.deleteLocalFile(item.upload.fileUri)
        uploadRepository.delete(item.upload)
      }
      .ignoreElement()

  class UploadPartError(throwable: Throwable) : Exception(throwable)

}