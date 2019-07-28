package app.envelop.data

import android.content.Context
import app.envelop.App
import app.envelop.data.models.Index
import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.reactivex.Completable
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
  private val gson: Gson
) {

  private val indexSubject = BehaviorSubject.create<Index>()

  fun get() =
    Completable
      .fromAction {
        if (!indexSubject.hasValue()) indexSubject.onNext(loadIndex())
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
    try {
      context.openFileInput(indexFileName).use {
        gson.fromJson(FileReader(it.fd), Index::class.java)
      }
    } catch (exception: FileNotFoundException) {
      Index()
    } catch (exception: JsonParseException) {
      Timber.w(exception)
      Index()
    }

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