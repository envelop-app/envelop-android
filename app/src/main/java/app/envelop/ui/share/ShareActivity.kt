package app.envelop.ui.share

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar
import app.envelop.R
import app.envelop.common.rx.observeOnUI
import app.envelop.data.models.Doc
import app.envelop.ui.BaseActivity
import app.envelop.ui.common.DocActions
import app.envelop.ui.common.clicksThrottled
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.activity_share.*
import kotlinx.android.synthetic.main.shared_appbar.*
import javax.inject.Inject

class ShareActivity : BaseActivity() {

  @Inject
  lateinit var docActions: DocActions

  private val viewModel by lazy {
    component.viewModelProvider()[ShareViewModel::class.java]
  }

  private val docIdReceived by lazy {
    intent?.getStringExtra(EXTRA_DOC_ID)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    component.inject(this)
    setContentView(R.layout.activity_share)
    toolbar.enableNavigation(R.drawable.ic_close, R.string.close)

    viewModel
      .doc()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        stateText.setText(if (it.uploaded) R.string.uploaded else R.string.uploading)
        icon.contentDescription = it.contentType
        icon.setImageResource(it.fileType.iconRes)
        name.text = it.name
      }

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

    uploadProgress.max = 100
    viewModel
      .progress()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        ProgressBarAnimation(uploadProgress, it.element()?.percentage ?: 100)
          .also { anim ->
            anim.duration = 300
            uploadProgress.startAnimation(anim)
          }
      }

    viewModel
      .link()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { link.setText(it.replace(Regex("https://"), "")) }

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
      Intent(context, ShareActivity::class.java).also {
        it.putExtra(EXTRA_DOC_ID, extras.doc.id)
      }
  }

  data class Extras(
    val doc: Doc
  )

  class ProgressBarAnimation(
    private val progressBar: ProgressBar,
    private val newValue: Int
  ) : Animation() {

    private val oldValue = progressBar.progress

    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
      super.applyTransformation(interpolatedTime, t)
      val value = oldValue + (newValue - oldValue) * interpolatedTime
      progressBar.progress = value.toInt()
    }
  }
}