package app.envelop.common

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import kotlin.math.min

class FileHandler
@Inject constructor(
  private val context: Context,
  private val contentResolver: ContentResolver
) {

  fun saveFileLocally(fileUri: Uri) =
    Single
      .fromCallable {
        val filename = generateFileName()
        contentResolver.openInputStream(fileUri)?.use { input ->
          context.openFileOutput(filename, Context.MODE_PRIVATE).use { output ->
            copy(input, output)
          }
        }
        context.getFileStreamPath(filename).toUri()
      }
      .toOperation()
      .subscribeOn(Schedulers.io())

  fun deleteLocalFile(fileUri: Uri) {
    context.deleteFile(fileUri.lastPathSegment)
  }

  fun localFileToByteArray(fileUri: String, start: Long = 0, maxLength: Long = Long.MAX_VALUE) =
    context.openFileInput(Uri.parse(fileUri).lastPathSegment)
      ?.use { inputStream ->

        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024L
        val buffer = ByteArray(bufferSize.toInt())
        var transferredLength = 0L

        inputStream.skip(start)

        var len: Int
        while (true) {
          len = inputStream.read(buffer)
          len = min(len.toLong(), maxLength - transferredLength).toInt()
          if (len < 1) break
          byteBuffer.write(buffer, 0, len)
          transferredLength += len
        }

        byteBuffer.toByteArray()
      }

  private fun generateFileName() = "file_${System.currentTimeMillis()}"

  private fun copy(input: InputStream, output: FileOutputStream) {
    val buffer = ByteArray(4 * 1024) // or other buffer size
    var read: Int
    while (true) {
      read = input.read(buffer)
      if (read == -1) break
      output.write(buffer, 0, read)
    }
    output.flush()
  }

}
