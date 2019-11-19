package app.envelop.data.models

import androidx.annotation.DrawableRes
import app.envelop.R

enum class FileType
constructor(
  @DrawableRes val iconRes: Int
) {
  Default(R.drawable.ic_type_file),
  Image(R.drawable.ic_type_image),
  Audio(R.drawable.ic_type_audio),
  Video(R.drawable.ic_type_video),
  Archive(R.drawable.ic_type_archive),
  PDF(R.drawable.ic_type_pdf);

  companion object {
    private val MAPPINGS
      get() = mapOf(
        Image to arrayOf("png", "gif", "jpg", "jpeg", "svg", "tif", "tiff", "ico"),
        Audio to arrayOf("wav", "aac", "mp3", "oga", "weba", "midi"),
        Video to arrayOf("avi", "mpeg", "mpg", "mp4", "ogv", "webm", "3gp", "mov"),
        Archive to arrayOf("zip", "rar", "tar", "gz", "7z", "bz", "bz2", "arc"),
        PDF to arrayOf("pdf")
      )

    fun fromContentType(type: String?) =
      type?.let {
        MAPPINGS.entries.firstOrNull {
          it.value.contains(type)
        }?.key
      } ?: Default
  }
}
