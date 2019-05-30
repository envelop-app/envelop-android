package app.envelop.domain

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import app.envelop.common.Optional
import app.envelop.data.models.Doc
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

class DocBuilder
@Inject constructor(
  private val hashGenerator: HashGenerator,
  private val contentResolver: ContentResolver
) {

  fun build(fileUri: Uri) =
    Single.fromCallable {
      Optional.create(
        contentResolver
          .query(fileUri, null, null, null, null)
          ?.use { cursor ->
            cursor.moveToFirst();
            val name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            val size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE))

            Doc(
              id = generateShortId(),
              url = "${generateSecretId()}/$name",
              size = size,
              contentType = fileUri.contentType(),
              createdAt = Date()
            )
          }
      )
    }

  private fun generateSecretId() = hashGenerator.generate(SECRET_HASH_SIZE)
  private fun generateShortId() = hashGenerator.generate(SHORT_HASH_SIZE)

  private fun Uri.contentType() =
    MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(this))

  companion object {
    private const val SECRET_HASH_SIZE = 24
    private const val SHORT_HASH_SIZE = 6
  }

}