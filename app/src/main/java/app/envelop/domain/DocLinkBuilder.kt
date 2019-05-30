package app.envelop.domain

import android.content.res.Resources
import app.envelop.R
import app.envelop.common.Optional
import app.envelop.data.models.Doc
import app.envelop.data.repositories.UserRepository
import javax.inject.Inject

class DocLinkBuilder
@Inject constructor(
  private val resources: Resources,
  private val userRepository: UserRepository
) {

  fun build(doc: Doc) =
    userRepository
      .user()
      .filter { it is Optional.Some }
      .take(1)
      .singleOrError()
      .map { user ->
        resources.getString(R.string.doc_url, user.element()?.username, doc.id)
      }

}