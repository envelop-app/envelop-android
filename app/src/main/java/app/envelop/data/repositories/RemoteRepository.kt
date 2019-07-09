package app.envelop.data.repositories

import app.envelop.common.FileHandler
import app.envelop.common.Operation
import app.envelop.common.Optional
import app.envelop.common.rx.observeOnIO
import app.envelop.data.models.Doc
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.model.DeleteFileOptions
import org.blockstack.android.sdk.model.GetFileOptions
import org.blockstack.android.sdk.model.PutFileOptions
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

class RemoteRepository
@Inject constructor(
  private val blockstackProvider: Provider<BlockstackSession>,
  private val gson: Gson
) {

  private val blockstack by lazy {
    blockstackProvider.get()
  }

  fun <T : Any> getJson(fileName: String, klass: KClass<T>, encrypted: Boolean) =
    Single
      .create<Operation<Optional<T>>> { emitter ->
        try {
          blockstack.getFile(fileName, GetFileOptions(decrypt = encrypted)) {
            if (!it.hasErrors) {
              emitter.onSuccess(
                Operation.success(
                  Optional.create(
                    it.value?.let { contents -> gson.fromJson(contents.toString(), klass.java) }
                  )
                )
              )
            } else {
              Timber.e(it.error)
              emitter.onSuccess(
                Operation.error(GetError("Error getting $fileName: ${it.error}"))
              )
            }
          }
        } catch (t: Throwable) {
          emitter.onSuccess(Operation.error(t))
        }
      }
      .subscribeOn(AndroidSchedulers.mainThread())
      .observeOnIO()

  fun uploadByteArray(url: String, data: ByteArray) =
    Single
      .create<Operation<Unit>> { emitter ->
        try {
          blockstack.putFile(
            url,
            data,
            PutFileOptions(encrypt = false, contentType = "application/octet-stream")
          ) { result ->
            if (result.hasValue) {
              emitter.onSuccess(Operation.success(Unit))
            } else {
              emitter.onSuccess(Operation.error(UploadError("Error uploading ${url}: ${result.error}")))
            }
          }
        } catch (t: Throwable) {
          emitter.onSuccess(Operation.error(t))
        }
      }
      .subscribeOn(AndroidSchedulers.mainThread())
      .observeOnIO()

  fun <T> uploadJson(fileName: String, content: T, encrypted: Boolean) =
    Single
      .create<Operation<T>> { emitter ->
        try {
          blockstack.putFile(
            fileName,
            gson.toJson(content),
            PutFileOptions(encrypt = encrypted, contentType = "application/json")
          ) { result ->
            if (result.hasValue) {
              emitter.onSuccess(Operation.success(content))
            } else {
              emitter.onSuccess(Operation.error(UploadError("Error uploading $fileName: ${result.error}")))
            }
          }
        } catch (t: Throwable) {
          emitter.onSuccess(Operation.error(t))
        }
      }
      .subscribeOn(AndroidSchedulers.mainThread())
      .observeOnIO()

  fun deleteFile(fileName: String) =
    Single
      .create<Operation<Unit>> { emitter ->
        try {
          blockstack.deleteFile(fileName, DeleteFileOptions()) {
            if (it.hasErrors) {
              if (it.error?.startsWith("FileNotFound") == true) {
                emitter.onSuccess(Operation.success())
              } else {
                Timber.e(it.error)
                emitter.onSuccess(Operation.error(DeleteError(it.error)))
              }
            } else {
              emitter.onSuccess(Operation.success())
            }
          }
        } catch (t: Throwable) {
          emitter.onSuccess(Operation.error(t))
        }
      }
      .subscribeOn(AndroidSchedulers.mainThread())
      .observeOnIO()

  class GetError(message: String?) : Exception(message)
  class UploadError(message: String?) : Exception(message)
  class DeleteError(message: String?) : Exception(message)

}