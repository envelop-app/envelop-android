package app.envelop.domain

import app.envelop.common.Operation
import app.envelop.common.flatMapIfSuccessful
import app.envelop.common.mapIfSuccessful
import app.envelop.data.models.Doc
import app.envelop.data.repositories.RemoteRepository
import app.envelop.data.security.EncryptedDataWrapper
import app.envelop.data.security.Encrypter
import app.envelop.data.security.EncrypterProvider
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class UpdateDocRemotely
@Inject constructor(
  private val indexService: IndexService,
  private val remoteRepository: RemoteRepository,
  private val encrypterProvider: EncrypterProvider,
  private val gson: Gson
) {

  fun update(doc: Doc): Single<Operation<Doc>> =
    upload(doc)
      .flatMapIfSuccessful { indexService.uploadKeepingDoc(doc) }
      .mapIfSuccessful { doc }

  fun delete(doc: Doc) =
    remoteRepository.deleteFile(doc.id)
      .flatMap {
        if (it.isSuccessful || it.is404) {
          indexService.uploadIgnoringDoc(doc)
        } else {
          Single.just(it)
        }
      }
      .mapIfSuccessful { doc }

  private fun upload(doc: Doc) =
    encrypterProvider.get(doc.encryptionSpec)
      ?.let { encryptAndUpload(it, doc) }
      ?: remoteRepository.uploadJson(doc.id, doc, false)

  private fun encryptAndUpload(encrypter: Encrypter, doc: Doc) =
    Single
      .fromCallable { gson.toJson(doc.toJsonObject().json) }
      .map { encrypter.encryptToBase64(it.toByteArray(), doc.encryptionSpec!!, doc.passcode!!) }
      .flatMap {
        remoteRepository.uploadJson(
          doc.id,
          EncryptedDataWrapper(
            payload = it.data,
            encryption = doc.encryptionSpecToJsonObject()!!.json
          ),
          false
        )
      }
      .mapIfSuccessful { doc }
      .subscribeOn(Schedulers.io())

}
