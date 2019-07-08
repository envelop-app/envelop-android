package app.envelop.data.models

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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

  val fileUri get() = Uri.parse(fileUriPath)

}

data class UploadWithDoc(
  val upload: Upload,
  val doc: Doc
) {

  val name get() = doc.name
  val progress get() = partsUploadedCount.toFloat() / totalParts.toFloat()
  val partsUploadedCount get() = upload.partsUploaded.size
  val totalParts get() = doc.calculateParts(upload.partSize)

  val missingParts
    get() =
      (1..totalParts)
        .filterNot { upload.partsUploaded.contains(it) }
        .map { part ->
          UploadPart(
            fileUri = Uri.parse(upload.fileUriPath),
            part = part,
            partSize = upload.partSize,
            baseDestinationUrl = doc.url,
            onlyOnePart = totalParts == 1
          )
        }

}

data class UploadPart(
  val fileUri: Uri,
  val part: Int,
  val partSize: Long,
  val baseDestinationUrl: String,
  val onlyOnePart: Boolean
) {

  val partStart get() = (part - 1) * partSize

  val destinationUrl
    get() = if (onlyOnePart) {
      baseDestinationUrl
    } else {
      "$baseDestinationUrl.part$part"
    }

}

sealed class UploadState {

  data class Uploading(
    val fileCount: Int,
    val nextUpload: UploadWithDoc
  ) : UploadState()

  object Idle : UploadState()

}
