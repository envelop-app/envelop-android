package app.envelop.data.repositories

import androidx.room.*
import app.envelop.data.models.Upload
import io.reactivex.Flowable

@Dao
interface UploadRepository {

  @Query("SELECT * FROM Upload WHERE Upload.id = :id LIMIT 1")
  fun get(id: Long): Flowable<List<Upload>>

  @Query("SELECT * FROM Upload WHERE Upload.docId = :docId LIMIT 1")
  fun getByDocId(docId: String): Flowable<List<Upload>>

  @Query("SELECT * FROM Upload ORDER BY Upload.id ASC")
  fun getAll(): Flowable<List<Upload>>

  @Query("SELECT COUNT(Upload.id) FROM Upload")
  fun count(): Flowable<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun save(upload: Upload)

  @Delete
  fun delete(upload: Upload)

  @Query("DELETE FROM Upload WHERE docId = :id")
  fun deleteByDocId(id: String)

  @Query("DELETE FROM Upload")
  fun deleteAll()

}