package app.envelop.data.models

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.envelop.data.security.Pbkdf2AesEncryptionSpec

@Entity(
  indices = [
    Index(value = ["docId"], unique = true)
  ]
)
data class Upload(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val docId: String = "",
  val fileUriPath: String = "",
  val partsUploaded: List<Int> = emptyList(),
  val partSize: Long = 0
) {

  val fileUri get() = Uri.parse(fileUriPath)!!

}

data class UploadWithDoc(
  val upload: Upload,
  val doc: Doc
) {

  val name get() = doc.name
  val progress get() = Progress(partsUploadedCount, totalParts)
  private val partsUploadedCount get() = upload.partsUploaded.size
  private val totalParts get() = doc.calculateParts(upload.partSize)

  val passcode get() = doc.passcode!!
  val baseEncryptionSpec get() = doc.encryptionSpec!!

  val missingParts
    get() =
      (0 until totalParts)
        .filterNot { upload.partsUploaded.contains(it) }
        .map { part ->
          val spec =
            (baseEncryptionSpec as Pbkdf2AesEncryptionSpec).copy(iv = doc.partIVs!![part])
          UploadPart(
            part = part,
            fileUri = Uri.parse(upload.fileUriPath),
            baseUrl = doc.url,
            partSize = upload.partSize,
            passcode = passcode,
            encryptionSpec = spec,
            onlyOnePart = totalParts == 1
          )
        }

}

data class UploadPart(
  val part: Int,
  val fileUri: Uri,
  val baseUrl: String,
  val partSize: Long,
  val passcode: String,
  val encryptionSpec: Pbkdf2AesEncryptionSpec,
  val onlyOnePart: Boolean
) {

  val partStart get() = part * partSize

  val destinationUrl
    get() = if (onlyOnePart) {
      baseUrl
    } else {
      "$baseUrl.part$part"
    }

}

sealed class UploadState {

  data class Uploading(
    val fileCount: Int,
    val nextUpload: UploadWithDoc
  ) : UploadState()

  object Idle : UploadState()

}
