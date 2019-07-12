package app.envelop.domain

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import app.envelop.common.Operation
import app.envelop.data.models.Doc
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class DocBuilder
@Inject constructor(
  private val hashGenerator: HashGenerator,
  private val contentResolver: ContentResolver
) {

  private val error by lazy {
    Operation.error<Doc>(Error("Could not access file and build doc"))
  }

  fun build(fileUri: Uri) =
    Single
      .fromCallable {
        contentResolver
          .query(fileUri, null, null, null, null)
          ?.use { cursor ->
            cursor.moveToFirst()
            var name =
              cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                ?.ifBlank { null }
                ?: "file-${Random().nextInt(1000)}"
            val size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE))
            val extension = name.getExtension() ?: fileUri.getExtension()

            // Avoid file names without extension
            if (name.getExtension() == null && !extension.isNullOrBlank()) {
              name = "$name.$extension"
            }

            if (size == 0L) return@use Operation.error<Doc>(Error("Empty file"))

            Operation.success(
              Doc(
                id = generateShortId(),
                url = "${generateSecretId()}/$name",
                size = size,
                contentType = extension,
                createdAt = Date(),
                uploaded = false,
                numParts = Doc.calculateNumParts(size, FILE_PART_SIZE)
              )
            )
          }
          ?: error
      }
      .onErrorReturn { error }
      .subscribeOn(Schedulers.io())

  private fun generateSecretId() = hashGenerator.generate(SECRET_HASH_SIZE)
  private fun generateShortId() = hashGenerator.generate(SHORT_HASH_SIZE)

  private fun String.getExtension() =
    if (contains(".")) {
      split(".").last()
    } else null

  private fun Uri.getExtension() =
    MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(this))

  companion object {
    private const val SECRET_HASH_SIZE = 24
    private const val SHORT_HASH_SIZE = 6
    const val FILE_PART_SIZE = 5_000_000L
  }

  class Error(message: String) : Exception(message)

}