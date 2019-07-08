package app.envelop.domain

import app.envelop.common.doIfSuccessful
import app.envelop.data.models.Index
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.RemoteRepository
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class IndexService
@Inject constructor(
  private val docRepository: DocRepository,
  private val remoteRepository: RemoteRepository
) {

  fun download() =
    remoteRepository
      .getJson(INDEX_FILE_NAME, Index::class, true)
      .doIfSuccessful {
        docRepository.replace(it.element()?.docs ?: emptyList())
      }

  fun upload() =
    docRepository
      .getAll()
      .firstOrError()
      .map { Index(it) }
      .flatMap {
        remoteRepository
          .uploadJson(INDEX_FILE_NAME, it, true)
      }
      .ignoreElement()
      .subscribeOn(Schedulers.io())

  fun get() =
    docRepository.getAll().toObservable()

  companion object {
    private const val INDEX_FILE_NAME = "index"
  }

}