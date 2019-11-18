package app.envelop.data.models

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName

data class Index(
  @SerializedName("files")
  val jsonArray: JsonArray = JsonArray()
) {

  constructor(docs: List<Doc>) : this(
    docs
      .map { it.toJsonObject().json }
      .let { jsonList ->
        val array = JsonArray(jsonList.size)
        jsonList.forEach { array.add(it) }
        array
      }
  )

  val docs get() = jsonArray.mapNotNull { Doc.build(it.asJsonObject) }

}

data class UnsanitizedIndex(
  @SerializedName("files")
  val jsonArray: JsonArray = JsonArray()
)