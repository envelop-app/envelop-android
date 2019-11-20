package app.envelop.data

import android.content.Context
import app.envelop.App
import app.envelop.data.mappers.IndexSanitizer
import app.envelop.data.models.Index
import app.envelop.data.models.UnsanitizedIndex
import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IndexDatabase
@Inject constructor(
  private val appMode: App.Mode,
  private val context: Context,
  private val gson: Gson,
  private val indexSanitizer: IndexSanitizer
) {

  private val indexSubject = BehaviorSubject.create<Index>()

  fun get(): Observable<Index> =
    Completable
      .defer {
        if (!indexSubject.hasValue()) {
          loadIndex().doOnSuccess { indexSubject.onNext(it) }.ignoreElement()
        } else {
          Completable.complete()
        }
      }
      .andThen(indexSubject.hide())
      .subscribeOn(Schedulers.io())

  fun save(index: Index) =
    Completable
      .fromAction {
        storeIndex(index)
        indexSubject.onNext(index)
      }
      .subscribeOn(Schedulers.io())

  fun delete() =
    Completable
      .fromAction {
        context.deleteFile(indexFileName)
        indexSubject.onNext(Index())
      }
      .subscribeOn(Schedulers.io())

  private fun loadIndex() =
    Single
      .fromCallable {
        try {
          context.openFileInput(indexFileName).use {
            gson.fromJson(FileReader(it.fd), UnsanitizedIndex::class.java) ?: UnsanitizedIndex()
          }
        } catch (exception: FileNotFoundException) {
          UnsanitizedIndex()
        } catch (exception: JsonParseException) {
          Timber.w(exception)
          UnsanitizedIndex()
        }
      }
      .flatMap { indexSanitizer.sanitize(it) }

  private fun storeIndex(index: Index) =
    context.openFileOutput(indexFileName, Context.MODE_PRIVATE).use {
      OutputStreamWriter(it).use { writer ->
        writer.write(gson.toJson(index))
      }
    }

  private val indexFileName
    get() =
      when (appMode) {
        App.Mode.Normal -> BASE_INDEX_FILE
        App.Mode.Test -> "$BASE_INDEX_FILE.test"
      }

  companion object {
    private const val BASE_INDEX_FILE = "index.db"
  }
}