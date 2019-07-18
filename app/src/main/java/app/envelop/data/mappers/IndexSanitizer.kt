package app.envelop.data.mappers

import app.envelop.data.models.Doc
import app.envelop.data.models.Index
import app.envelop.data.models.UnsanitizedIndex
import app.envelop.domain.UserService
import javax.inject.Inject

class IndexSanitizer
@Inject constructor(
  private val userService: UserService
) {

  fun sanitize(unsanitizedIndex: UnsanitizedIndex) =
    userService
      .userSingle()
      .map { user ->
        Index(
          unsanitizedIndex
            .jsonArray
            .map {
              it.asJsonObject.also { docJson ->
                if (!docJson.has("username") || docJson.get("username").isJsonNull) {
                  docJson.addProperty("username", user.username)
                }
              }
            }
            .map { Doc.build(it) }
        )
      }

}