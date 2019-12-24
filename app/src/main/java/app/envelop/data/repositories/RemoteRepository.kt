package app.envelop.data.repositories

import app.envelop.common.Optional
import app.envelop.common.mapIfSuccessful
import app.envelop.common.rx.observeOnUI
import app.envelop.common.toOperation
import com.google.gson.Gson
import kotlinx.coroutines.rx2.rxSingle
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

  private val blockstack by lazy { blockstackProvider.get() }

  fun <T : Any> getJson(fileName: String, klass: KClass<T>, encrypted: Boolean) =
    rxSingle {
      val result = blockstack.getFile(fileName, GetFileOptions(decrypt = encrypted))
      if (!result.hasErrors) {
        Optional.create(
          result.value?.let { contents -> gson.fromJson(contents.toString(), klass.java) }
        )
      } else {
        Timber.w(result.error?.toString())
        throw GetError("Error getting $fileName: ${result.error}")
      }
    }.toOperation()

  fun uploadByteArray(url: String, data: ByteArray, encrypted: Boolean) =
    rxSingle {
      val result = blockstack.putFile(
        url,
        data,
        PutFileOptions(encrypt = encrypted, contentType = "application/octet-stream")
      )
      if (!result.hasValue) {
        throw UploadError("Error uploading ${url}: ${result.error}")
      }
    }.toOperation()

  fun <T : Any> uploadJson(fileName: String, content: T, encrypted: Boolean) =
    rxSingle {
      val result = blockstack.putFile(
        fileName,
        gson.toJson(content),
        PutFileOptions(encrypt = encrypted, contentType = "application/json")
      )
      if (result.hasValue) {
        content
      } else {
        throw UploadError("Error uploading $fileName: ${result.error}")
      }
    }.toOperation()

  fun deleteFile(fileName: String) =
    rxSingle {
      val it = blockstack.deleteFile(fileName, DeleteFileOptions())
      if (it.hasErrors && it.error?.message?.startsWith("FileNotFound") != true) {
        Timber.w(it.error?.toString())
        throw DeleteError(it.error?.toString())
      }
    }.toOperation()

  private fun getFilesList() =
    rxSingle {
      val list = mutableListOf<String>()
      blockstack.listFiles { result ->
        result.value?.let { list.add(it) }
        true
      }
      list
    }.toOperation()

  fun getFilesList(prefix: String) =
    getFilesList()
      .mapIfSuccessful { list -> list.filter { it.startsWith(prefix) } }

  @Suppress("unused") // Useful for debugging
  fun printListFiles() {
    getFilesList()
      .observeOnUI()
      .subscribe({
        if (it.isSuccessful) it.result().forEach { file -> Timber.d("File: $file") }
      }, {})
  }

  class GetError(message: String?) : Exception(message)
  class UploadError(message: String?) : Exception(message)
  class DeleteError(message: String?) : Exception(message)
}
