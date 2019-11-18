package app.envelop.ui.common

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import app.envelop.R
import javax.inject.Inject

class DocActions
@Inject constructor(
  private val activity: Activity,
  private val messageManager: MessageManager
) {

  fun copyLink(link: String) {
    (activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
      .setPrimaryClip(ClipData.newPlainText(link, link))
    messageManager.showNotice(R.string.doc_copy_link_done)
  }

  fun share(link: String) {
    activity.startActivity(
      Intent.createChooser(
        Intent(Intent.ACTION_SEND).also {
          it.type = "text/plain"
          it.putExtra(Intent.EXTRA_TEXT, link)
        },
        activity.getString(R.string.doc_share_chooser)
      )
    )
  }

  fun open(link: String) {
    activity.startActivity(
      Intent(Intent.ACTION_VIEW).also {
        it.data = Uri.parse(link)
      }
    )
  }

}