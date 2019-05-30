package app.envelop.data.models

import com.google.gson.annotations.SerializedName

data class Index(
  @SerializedName("files")
  val docs: List<Doc>
)