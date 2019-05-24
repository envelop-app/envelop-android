package app.envelop.domain

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import app.envelop.common.Operation
import app.envelop.common.Optional
import app.envelop.data.models.Doc
import app.envelop.domain.upload.HashGenerator
import com.google.gson.Gson
import io.reactivex.Single
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.model.PutFileOptions
import java.io.ByteArrayOutputStream
import java.util.*
import javax.inject.Inject

class UploadService
@Inject constructor(
  private val blockstack: BlockstackSession,
  private val contentResolver: ContentResolver,
  private val gson: Gson,
  private val hashGenerator: HashGenerator
) {

  fun upload(fileUri: Uri) =
    buildDoc(fileUri)
      .flatMap {
        if (it is Optional.Some) {
          uploadFile(fileUri, it.element)
        } else {
          Single.just(Operation.error(Error("Could not access file and build doc")))
        }
      }
      .flatMap {
        if (it.isSuccessful) {
          uploadDoc(it.result())
        } else {
          Single.just(it)
        }
      }

  private fun uploadFile(fileUri: Uri, doc: Doc) =
    Single.create<Operation<Doc>> { emitter ->
      fileUri
        .toByteArray()
        ?.let { content ->
          blockstack.putFile(
            doc.url,
            content,
            PutFileOptions(encrypt = false, contentType = doc.contentType)
          ) { result ->
            if (result.hasValue) {
              emitter.onSuccess(Operation.success(doc))
            } else {
              emitter.onSuccess(Operation.error(Error("Error uploading file: ${result.error}")))
            }
          }
        } ?: emitter.onSuccess(Operation.error(Error("Error reading URI contents")))
    }

  private fun uploadDoc(doc: Doc) =
    Single.create<Operation<Doc>> { emitter ->
      blockstack.putFile(
        doc.id,
        gson.toJson(doc),
        PutFileOptions(encrypt = false, contentType = "application/json")
      ) { result ->
        if (result.hasValue) {
          emitter.onSuccess(Operation.success(doc))
        } else {
          emitter.onSuccess(Operation.error(Error("Error uploading doc: ${result.error}")))
        }
      }
    }

  private fun generateSecretId() = hashGenerator.generate(SECRET_HASH_SIZE)
  private fun generateShortId() = hashGenerator.generate(SHORT_HASH_SIZE)

  private fun Uri.contentType() =
    MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(this))

  private

  fun Uri.toByteArray() =
    contentResolver.openInputStream(this)
      ?.use { inputStream ->

        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)

        var len = inputStream.read(buffer)
        while (len != -1) {
          byteBuffer.write(buffer, 0, len)
          len = inputStream.read(buffer)
        }

        byteBuffer.toByteArray()
      }

  private fun buildDoc(fileUri: Uri) =
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


  class Error(message: String?) : Exception(message)

  companion object {
    private const val SECRET_HASH_SIZE = 24
    private const val SHORT_HASH_SIZE = 6
  }

}