package app.envelop.data.repositories

import app.envelop.common.Operation
import app.envelop.common.Optional
import app.envelop.common.mapIfSuccessful
import app.envelop.common.rx.observeOnIO
import app.envelop.common.rx.observeOnUI
import com.google.gson.Gson
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.SingleEmitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.model.DeleteFileOptions
import org.blockstack.android.sdk.model.GetFileOptions
import org.blockstack.android.sdk.model.PutFileOptions
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import kotlin.reflect.KClass

class RemoteRepository
@Inject constructor(
    private val blockstackProvider: Provider<BlockstackSession>,
    @Named("blockstack") private val blockstackScheduler: Scheduler,
    private val gson: Gson
) {

    private val blockstack by lazy { blockstackProvider.get() }

    fun <T : Any> getJson(fileName: String, klass: KClass<T>, encrypted: Boolean) =
        createSingleForCall<Optional<T>> { emitter ->
            CoroutineScope(Dispatchers.IO).launch {
                val it = blockstack.getFile(fileName, GetFileOptions(decrypt = encrypted))
                if (!it.hasErrors) {
                    emitter.onSuccess(
                        Operation.success(
                            Optional.create(
                                it.value?.let { contents ->
                                    gson.fromJson(
                                        contents.toString(),
                                        klass.java
                                    )
                                }
                            )
                        )
                    )
                } else {
                    Timber.w(it.error!!.message)
                    emitter.onSuccess(
                        Operation.error(GetError("Error getting $fileName: ${it.error}"))
                    )
                }
            }
        }


    fun uploadByteArray(url: String, data: ByteArray, encrypted: Boolean) =
        createSingleForCall<Unit> { emitter ->
            CoroutineScope(Dispatchers.IO).launch {
                val result = blockstack.putFile(
                    url,
                    data,
                    PutFileOptions(encrypt = encrypted, contentType = "application/octet-stream")
                )
                if (result.hasValue) {
                    emitter.onSuccess(Operation.success(Unit))
                } else {
                    emitter.onSuccess(Operation.error(UploadError("Error uploading ${url}: ${result.error}")))
                }
            }
        }

    fun <T> uploadJson(fileName: String, content: T, encrypted: Boolean) =
        createSingleForCall<T> { emitter ->
            CoroutineScope(Dispatchers.IO).launch {

                val result = blockstack.putFile(
                    fileName,
                    gson.toJson(content),
                    PutFileOptions(encrypt = encrypted, contentType = "application/json")
                )
                if (result.hasValue) {
                    emitter.onSuccess(Operation.success(content))
                } else {
                    emitter.onSuccess(Operation.error(UploadError("Error uploading $fileName: ${result.error}")))
                }
            }
        }

    fun deleteFile(fileName: String) =
        createSingleForCall<Unit> { emitter ->
            CoroutineScope(Dispatchers.IO).launch {

                val it = blockstack.deleteFile(fileName, DeleteFileOptions())
                if (it.hasErrors) {
                    if (it.error!!.message.startsWith("FileNotFound") == true) {
                        emitter.onSuccess(Operation.success())
                    } else {
                        Timber.w(it.error!!.message)
                        emitter.onSuccess(Operation.error(DeleteError(it.error!!.message)))
                    }
                } else {
                    emitter.onSuccess(Operation.success())
                }
            }
        }

    fun getFilesList() =
        createSingleForCall<List<String>> { emitter ->
            CoroutineScope(Dispatchers.IO).launch {

                val list = mutableListOf<String>()
                blockstack.listFiles { result ->
                    result.value?.let { list.add(it) }
                    true
                }
                emitter.onSuccess(Operation.success(list))
            }
        }

    fun getFilesList(prefix: String) =
        getFilesList()
            .mapIfSuccessful { list -> list.filter { it.startsWith(prefix) } }

    fun printListFiles() {
        getFilesList()
            .observeOnUI()
            .subscribe({
                if (it.isSuccessful) it.result().forEach { file -> Timber.d("File: $file") }
            }, {})
    }

    private fun <T> createSingleForCall(call: ((SingleEmitter<Operation<T>>) -> Unit)) =
        Single
            .create<Operation<T>> { emitter ->
                try {
                    call.invoke(emitter)
                } catch (t: Throwable) {
                    emitter.onSuccess(Operation.error(t))
                }
            }
            .subscribeOn(blockstackScheduler)
            .observeOnIO()

    class GetError(message: String?) : Exception(message)
    class UploadError(message: String?) : Exception(message)
    class DeleteError(message: String?) : Exception(message)

}