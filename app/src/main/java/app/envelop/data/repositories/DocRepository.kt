package app.envelop.data.repositories

import app.envelop.common.Optional
import app.envelop.data.IndexDatabase
import app.envelop.data.models.Doc
import app.envelop.data.models.Index
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocRepository
@Inject constructor(
  private val indexDb: IndexDatabase,
  private val uploadRepository: UploadRepository
) {

  fun list(): Observable<List<Doc>> =
    loadDocs()

  fun listVisible(): Observable<List<Doc>> =
    loadDocs().map { list -> list.filter { !it.deleted } }

  fun listDeleted(): Observable<List<Doc>> =
    loadDocs().map { list -> list.filter { it.deleted } }

  fun countDeleted(): Observable<Int> =
    loadDocs().map { list -> list.count { it.deleted } }

  fun get(id: String): Observable<Optional<Doc>> =
    loadDocs().map { list -> Optional.create(list.firstOrNull { it.id == id }) }

  fun save(doc: Doc) =
    loadDocs()
      .take(1)
      .singleOrError()
      .map { list -> list.filter { it.id != doc.id } + doc }
      .saveDocs()

  fun delete(doc: Doc): Completable =
    deleteDoc(doc)
      .doOnComplete { uploadRepository.deleteByDocId(doc.id) }

  private fun deleteDoc(doc: Doc) =
    loadDocs()
      .take(1)
      .singleOrError()
      .map { list -> list.filter { it.id != doc.id } }
      .saveDocs()

  fun deleteAll() =
    indexDb.delete()

  fun replace(docs: List<Doc>) =
    Single
      .fromCallable { docs }
      .saveDocs()

  private fun loadDocs() =
    indexDb
      .get()
      .map { it.docs }

  private fun Single<List<Doc>>.saveDocs() =
    flatMapCompletable { list ->
      indexDb.save(
        Index(
          list.sortedBy { it.createdAt }.reversed()
        )
      )
    }

}