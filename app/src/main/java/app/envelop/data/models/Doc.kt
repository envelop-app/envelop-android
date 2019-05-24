package app.envelop.data.models

import java.util.*

data class Doc(
  val id: String,
  val url: String,
  val size: Long,
  val contentType: String?,
  val createdAt: Date
)