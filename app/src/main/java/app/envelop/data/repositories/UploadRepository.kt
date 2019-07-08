package app.envelop.data.repositories

import androidx.room.*
import app.envelop.data.models.Doc
import app.envelop.data.models.Upload
import app.envelop.data.models.UploadWithDoc
import io.reactivex.Flowable

@Dao
interface UploadRepository {

  @Query("SELECT * FROM Upload WHERE Upload.id = :id LIMIT 1")
  fun get(id: Long): Flowable<List<Upload>>

  @Query("SELECT * FROM Upload ORDER BY Upload.id ASC")
  @Transaction
  fun getAll(): Flowable<List<UploadWithDocQuery>>

  @Query("SELECT COUNT(Upload.id) FROM Upload")
  fun count(): Flowable<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun save(upload: Upload)

  @Delete
  fun delete(upload: Upload)

  @Query("DELETE FROM Upload")
  fun deleteAll()

  // Query objects

  data class UploadWithDocQuery(
    @Embedded val upload: Upload
  ) {
    @Relation(parentColumn = "docId", entityColumn = "id")
    var docList: List<Doc>? = null
    val doc get() = docList?.firstOrNull()

    fun build() = doc?.let { UploadWithDoc(upload, it) }
  }

}