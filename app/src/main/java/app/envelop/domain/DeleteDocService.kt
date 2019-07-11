package app.envelop.domain

import app.envelop.common.FileHandler
import app.envelop.common.Operation
import app.envelop.common.doIfError
import app.envelop.common.rx.observeOnIO
import app.envelop.data.models.Doc
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.RemoteRepository
import app.envelop.data.repositories.UploadRepository
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DeleteDocService
@Inject constructor(
  private val docRepository: DocRepository,
  private val uploadRepository: UploadRepository,
  private val remoteRepository: RemoteRepository,
  private val updateDocRemotely: UpdateDocRemotely,
  private val fileHandler: FileHandler
) {

  fun markAsDeleted(doc: Doc) =
    Single
      .fromCallable { doc.copy(deleted = true) }
      .subscribeOn(Schedulers.io())
      .observeOnIO()
      .doOnSuccess { docRepository.save(it) }
      .flatMap { updateDocRemotely.update(it) }
      .doIfError { docRepository.save(doc.copy(deleted = false)) }

  fun deletePending() =
    docRepository
      .countDeleted()
      .toObservable()
      .filter { it > 0 }
      .flatMap { getFilesToDelete() }
      .filter { it.isNotEmpty() }
      .map { it.first() }
      .concatMapSingle { delete(it) }
      .doIfError { Timber.e(it, "Delete error") }
      .ignoreElements()

  private fun delete(doc: Doc) =
    remoteRepository
      .getFilesList(prefix = doc.url)
      .doOnSuccess { if (it.isError) throw DeleteError(it.throwable()) }
      .flatMapObservable { Observable.fromIterable(it.result()) }
      .delay(DELETE_THROTTLE, TimeUnit.MILLISECONDS)
      .concatMapSingle { remoteRepository.deleteFile(it) }
      // If we can't delete one part and it's not a 404 (already deleted), break the chain
      .doOnNext { if (!it.isSuccessful && !it.is404) throw DeleteError(it.throwable()) }
      .ignoreElements()
      .andThen(deleteLocalUploadFileIfNeeded(doc))
      .doOnComplete { docRepository.delete(doc) }
      .andThen(updateDocRemotely.delete(doc))
      .doIfError { docRepository.save(doc) } // Restore the doc file is needed
      .onErrorReturn { Operation.error(it) }

  fun deleteLocalUploadFileIfNeeded(doc: Doc) =
    uploadRepository
      .getByDocId(doc.id)
      .take(1)
      .toObservable()
      .doOnNext { if (it.isNotEmpty()) fileHandler.deleteLocalFile(it.first().fileUri) }
      .ignoreElements()

  private fun getFilesToDelete() =
    docRepository.listDeleted().toObservable().take(1)

  class DeleteError(throwable: Throwable? = null) : Exception(throwable)

  companion object {
    private const val DELETE_THROTTLE = 2000L // ms
  }

}