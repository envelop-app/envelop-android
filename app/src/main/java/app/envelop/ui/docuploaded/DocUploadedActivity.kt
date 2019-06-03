package app.envelop.ui.docuploaded

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.envelop.R
import app.envelop.common.rx.observeOnUI
import app.envelop.data.models.Doc
import app.envelop.ui.BaseActivity
import app.envelop.ui.common.DocActions
import app.envelop.ui.common.clicksThrottled
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.activity_doc.*
import kotlinx.android.synthetic.main.shared_appbar.*
import javax.inject.Inject

class DocUploadedActivity : BaseActivity() {

  @Inject
  lateinit var docActions: DocActions

  private val viewModel by lazy {
    component.viewModelProvider()[DocUploadedViewModel::class.java]
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
      .subscribe(docActions::copyLink)

    share
      .clicksThrottled()
      .flatMap { viewModel.link().take(1) }
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe(docActions::share)

    viewModel
      .finish()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { finish(it) }

    docIdReceived?.let {
      viewModel.docIdReceived(it)
    } ?: finish()
  }

  companion object {
    private const val EXTRA_DOC_ID = "doc_id"

    fun getIntent(context: Context, extras: Extras) =
      Intent(context, DocUploadedActivity::class.java).also {
        it.putExtra(EXTRA_DOC_ID, extras.doc.id)
      }
  }

  data class Extras(
    val doc: Doc
  )
}