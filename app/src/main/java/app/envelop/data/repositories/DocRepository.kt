package app.envelop.data.repositories

import androidx.room.*
import app.envelop.data.models.Doc
import io.reactivex.Flowable

@Dao
interface DocRepository {

  @Query("SELECT * FROM Doc ORDER BY createdAt DESC")
  fun list(): Flowable<List<Doc>>

  @Query("SELECT * FROM Doc WHERE deleted = 0 OR deleted IS NULL ORDER BY createdAt DESC")
  fun listVisible(): Flowable<List<Doc>>

  @Query("SELECT * FROM Doc WHERE deleted = 1 ORDER BY createdAt DESC")
  fun listDeleted(): Flowable<List<Doc>>

  @Query("SELECT COUNT(Doc.id) FROM Doc WHERE deleted = 1")
  fun countDeleted(): Flowable<Int>

  @Query("SELECT * FROM Doc WHERE id = :id LIMIT 1")
  fun get(id: String): Flowable<List<Doc>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun save(doc: Doc)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun save(docs: List<Doc>)

  @Transaction
  fun delete(doc: Doc) {
    deleteDoc(doc)
    deleteUpload(doc.id)
  }

  @Delete
  fun deleteDoc(doc: Doc)

  @Query("DELETE FROM Upload WHERE docId = :id")
  fun deleteUpload(id: String)

  @Query("DELETE FROM Doc")
  fun deleteAll()

  @Query("DELETE FROM Doc WHERE id NOT IN (:exceptIds)")
  fun deleteAllExcept(exceptIds: List<String>)

  @Transaction
  fun replace(docs: List<Doc>) {
    deleteAllExcept(docs.map { it.id })
    save(docs)
  }

}