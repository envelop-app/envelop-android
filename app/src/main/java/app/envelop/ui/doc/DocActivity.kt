package app.envelop.ui.doc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.envelop.R
import app.envelop.common.rx.observeOnUI
import app.envelop.data.models.Doc
import app.envelop.ui.BaseActivity
import app.envelop.ui.common.MessageManager
import app.envelop.ui.common.clicksThrottled
import app.envelop.ui.common.loading.LoadingManager
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.activity_doc.*
import kotlinx.android.synthetic.main.shared_appbar.*
import javax.inject.Inject

class DocActivity : BaseActivity() {

  @Inject
  lateinit var messageManager: MessageManager
  @Inject
  lateinit var loadingManager: LoadingManager

  private val viewModel by lazy {
    component.viewModelProvider()[DocViewModel::class.java]
  }

  private val docIdReceived by lazy {
    intent?.getStringExtra(EXTRA_DOC_ID)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    component.inject(this)
    setContentView(R.layout.activity_doc)
    toolbar.enableNavigation(R.drawable.ic_close, R.string.close)

    viewModel
      .doc()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { name.text = it.name }

    copyLink
      .clicksThrottled()
      .flatMap { viewModel.link().take(1) }
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe(this::copyLink)

    share
      .clicksThrottled()
      .flatMap { viewModel.link().take(1) }
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe(this::share)

    delete
      .clicksThrottled()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { viewModel.deleteClicked() }

    viewModel
      .isDeleting()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { loadingManager.apply(it, R.string.doc_deleting) }

    viewModel
      .errors()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        messageManager.showError(
          when (it) {
            DocViewModel.Error.DeleteError -> R.string.doc_delete_error
          }
        )
      }

    viewModel
      .finish()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { finish(it) }

    docIdReceived?.let {
      viewModel.docIdReceived(it)
    } ?: finish()
  }

  override fun onStop() {
    super.onStop()
    loadingManager.hide()
  }

  private fun copyLink(link: String) {
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
      .primaryClip = ClipData.newPlainText(link, link)
    messageManager.showNotice(app.envelop.R.string.doc_copy_link_done)
  }

  private fun share(link: String) {
    startActivity(
      Intent.createChooser(
        Intent(Intent.ACTION_SEND).also {
          it.type = "text/plain"
          it.putExtra(Intent.EXTRA_TEXT, link)
        },
        getString(app.envelop.R.string.doc_share_chooser)
      )
    )
  }

  companion object {
    private const val EXTRA_DOC_ID = "doc_id"

    fun getIntent(context: Context, extras: Extras) =
      Intent(context, DocActivity::class.java).also {
        it.putExtra(EXTRA_DOC_ID, extras.doc.id)
      }
  }

  data class Extras(
    val doc: Doc
  )
}