package app.envelop.domain

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import app.envelop.common.EnvelopSpec
import app.envelop.common.Operation
import app.envelop.data.models.Doc
import app.envelop.data.security.HashGenerator
import app.envelop.data.security.IVGenerator
import app.envelop.data.security.Pbkdf2AesEncryptionSpec
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class DocBuilder
@Inject constructor(
  private val userService: UserService,
  private val hashGenerator: HashGenerator,
  private val docIdGenerator: DocIdGenerator,
  private val ivGenerator: IVGenerator,
  private val contentResolver: ContentResolver
) {

  fun build(fileUri: Uri) =
    userService
      .userSingle()
      .map { user ->
        val fileInfo = getFileInfo(fileUri)
        val numParts = Doc.calculateNumParts(fileInfo.size, EnvelopSpec.FILE_PART_SIZE)
        val id = generateDocId()
        Operation.success(
          Doc(
            id = id,
            name = fileInfo.name,
            url = generateFileUrl(),
            size = fileInfo.size,
            contentType = fileInfo.extension,
            createdAt = Date(),
            uploaded = false,
            username = user.username,
            numParts = Doc.calculateNumParts(fileInfo.size, EnvelopSpec.FILE_PART_SIZE),
            passcode = generatePasscode(),
            version = Doc.CURRENT_VERSION,
            partIVs = ivGenerator.generateListInBase64(numParts, Pbkdf2AesEncryptionSpec.IV_SIZE),
            encryptionSpec = Pbkdf2AesEncryptionSpec(
              salt = id,
              iv = ivGenerator.generateInBase64(Pbkdf2AesEncryptionSpec.IV_SIZE)
            )
          )
        )
      }
      .onErrorReturn { Operation.error(Error("Could not access file and build doc", it)) }
      .subscribeOn(Schedulers.io())

  private fun getFileInfo(fileUri: Uri) =
    contentResolver
      .query(fileUri, null, null, null, null)
      .use { cursor ->
        cursor!!.moveToFirst()
        var name =
          cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            ?.ifBlank { null }
            ?: generateGenericFileName()

        val size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE))
        val extension = name.getExtension() ?: fileUri.getExtension()

        // Avoid file names without extension
        if (name.getExtension() == null && !extension.isNullOrBlank()) {
          name = "$name.$extension"
        }

        if (size == 0L) throw Error("Empty file")

        FileInfo(
          name,
          size,
          extension
        )
      }

  private fun generateGenericFileName() = "file-${Random().nextInt(1000)}"
  private fun generateFileUrl() = UUID.randomUUID().toString()
  private fun generateDocId() = docIdGenerator.generate()
  private fun generatePasscode() = hashGenerator.generate(EnvelopSpec.PASSCODE_LENGTH)

  private fun String.getExtension() =
    if (contains(".")) split(".").last() else null

  private fun Uri.getExtension() =
    MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(this))

  private class FileInfo(
    val name: String,
    val size: Long,
    val extension: String?
  )

  class Error(message: String, cause: Throwable? = null) : Exception(message, cause)

}