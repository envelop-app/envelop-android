package app.envelop.domain

import android.content.res.Resources
import app.envelop.R
import app.envelop.data.models.Doc
import app.envelop.data.models.User
import javax.inject.Inject

class DocLinkBuilder
@Inject constructor(
  private val resources: Resources
) {

  fun build(doc: Doc): String {
    val usernameShort = User.usernameShort(doc.username)
    return doc.passcode
      ?.let { pass -> resources.getString(R.string.doc_url, usernameShort, doc.id, pass) }
      ?: resources.getString(R.string.doc_url_old, usernameShort, doc.id)
  }

}