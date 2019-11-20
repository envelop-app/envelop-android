package app.envelop.domain

import app.envelop.common.FileHandler
import app.envelop.common.Operation
import app.envelop.common.doIfError
import app.envelop.common.rx.observeOnIO
import app.envelop.data.models.Doc
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.RemoteRepository
import app.envelop.data.repositories.UploadRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
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
      .flatMap { docRepository.save(it).toSingleDefault(it) }
      .flatMap { updateDocRemotely.update(it) }
      .flatMap { op ->
        if (op.isError) {
          doc.copy(deleted = false).let { docRepository.save(it).toSingleDefault(op) }
        } else Single.just(op)
      }

  fun deletePending(): Completable =
    docRepository
      .countDeleted()
      .filter { it > 0 }
      .flatMap { getFilesToDelete() }
      .distinctUntilChanged()
      .filter { it.isNotEmpty() }
      .map { it.first() }
      .concatMapSingle { delete(it) }
      .doIfError { Timber.w(it, "Delete error") }
      .ignoreElements()

  private fun delete(doc: Doc) =
    remoteRepository
      .getFilesList(prefix = doc.url)
      .doOnSuccess { if (it.isError) throw DeleteError(it.throwable()) }
      .flatMapObservable { Observable.fromIterable(it.result()) }
      // We need to throttle the deletes to avoid doing too many requests
      .wait(DELETE_THROTTLE, TimeUnit.MILLISECONDS)
      .concatMapSingle { remoteRepository.deleteFile(it) }
      // If we can't delete one part and it's not a 404 (already deleted), break the chain
      .doOnNext { if (!it.isSuccessful && !it.is404) throw DeleteError(it.throwable()) }
      .ignoreElements()
      .andThen(deleteLocalUploadFileIfNeeded(doc))
      .andThen(docRepository.delete(doc))
      .andThen(updateDocRemotely.delete(doc))
      .onErrorReturn { Operation.error(it) }

  private fun deleteLocalUploadFileIfNeeded(doc: Doc): Completable =
    uploadRepository
      .getByDocId(doc.id)
      .take(1)
      .toObservable()
      .doOnNext { if (it.isNotEmpty()) fileHandler.deleteLocalFile(it.first().fileUri) }
      .ignoreElements()

  private fun getFilesToDelete() =
    docRepository.listDeleted().take(1)

  private fun <T> Observable<T>.wait(delay: Long, unit: TimeUnit): Observable<T> =
    zipWith(Observable.interval(delay, unit), BiFunction<T, Long, T> { item, _ -> item })

  class DeleteError(throwable: Throwable? = null) : Exception(throwable)

  companion object {
    private const val DELETE_THROTTLE = 1000L // ms
  }

}