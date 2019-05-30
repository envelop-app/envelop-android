package app.envelop.data.repositories

import android.content.ContentResolver
import android.net.Uri
import app.envelop.common.Operation
import app.envelop.common.Optional
import app.envelop.data.models.Doc
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.model.DeleteFileOptions
import org.blockstack.android.sdk.model.GetFileOptions
import org.blockstack.android.sdk.model.PutFileOptions
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

class RemoteRepository
@Inject constructor(
  private val blockstackProvider: Provider<BlockstackSession>,
  private val contentResolver: ContentResolver,
  private val gson: Gson
) {

  private val blockstack by lazy {
    blockstackProvider.get()
  }

  fun <T : Any> getJson(fileName: String, klass: KClass<T>, encrypted: Boolean) =
    Single
      .create<Operation<Optional<T>>> { emitter ->
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
      }
      .subscribeOn(AndroidSchedulers.mainThread())

  fun uploadDoc(doc: Doc, fileUri: Uri) =
    Single
      .create<Operation<Doc>> { emitter ->
        fileUri
          .toByteArray()
          ?.let { content ->
            blockstack.putFile(
              doc.url,
              content,
              PutFileOptions(encrypt = false, contentType = doc.contentType)
            ) { result ->
              if (result.hasValue) {
                emitter.onSuccess(Operation.success(doc))
              } else {
                emitter.onSuccess(Operation.error(UploadError("Error uploading ${doc.url}: ${result.error}")))
              }
            }
          } ?: emitter.onSuccess(Operation.error(UploadError("Error reading URI contents")))
      }
      .subscribeOn(AndroidSchedulers.mainThread())

  fun uploadJson(fileName: String, content: Any, encrypted: Boolean) =
    Single
      .create<Operation<Unit>> { emitter ->
        blockstack.putFile(
          fileName,
          gson.toJson(content),
          PutFileOptions(encrypt = encrypted, contentType = "application/json")
        ) { result ->
          if (result.hasValue) {
            emitter.onSuccess(Operation.success())
          } else {
            emitter.onSuccess(Operation.error(UploadError("Error uploading $fileName: ${result.error}")))
          }
        }
      }
      .subscribeOn(AndroidSchedulers.mainThread())

  fun deleteFile(fileName: String) =
    Single
      .create<Operation<Unit>> { emitter ->
        blockstack.deleteFile(fileName, DeleteFileOptions()) {
          if (it.hasErrors) {
            if (it.error?.startsWith("FileNotFound") == true) {
              emitter.onSuccess(Operation.success())
            } else {
              Timber.e(it.error)
              emitter.onSuccess(Operation.error(DeleteError("Error deleting $fileName: ${it.error}")))
            }
          } else {
            emitter.onSuccess(Operation.success())
          }
        }
      }
      .subscribeOn(AndroidSchedulers.mainThread())

  private fun Uri.toByteArray() =
    contentResolver.openInputStream(this)
      ?.use { inputStream ->

        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)

        var len = inputStream.read(buffer)
        while (len != -1) {
          byteBuffer.write(buffer, 0, len)
          len = inputStream.read(buffer)
        }

        byteBuffer.toByteArray()
      }

  class GetError(message: String?) : Exception(message)
  class UploadError(message: String?) : Exception(message)
  class DeleteError(message: String?) : Exception(message)

}