package app.envelop.ui.upload

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import app.envelop.R
import app.envelop.common.rx.observeOnUI
import app.envelop.ui.BaseActivity
import app.envelop.ui.common.MessageManager
import app.envelop.ui.common.loading.LoadingManager
import app.envelop.ui.docuploaded.DocUploadedActivity
import app.envelop.ui.login.LoginActivity
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindToLifecycle
import javax.inject.Inject

class UploadActivity : BaseActivity() {

  @Inject
  lateinit var loadingManager: LoadingManager
  @Inject
  lateinit var messageManager: MessageManager

  private val viewModel by lazy {
    component.viewModelProvider()[UploadViewModel::class.java]
  }

  private val uriReceived by lazy {
    intent?.getParcelableExtra<Parcelable>(
      if (intent?.action == Intent.ACTION_SEND) {
        Intent.EXTRA_STREAM
      } else {
        EXTRA_FILE_URI
      }
    ) as? Uri?
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    component.inject(this)
    setContentView(R.layout.activity_upload)

    viewModel
      .isUploading()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { loadingManager.apply(it, R.string.uploading) }

    viewModel
      .error()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        messageManager.showError(
          when (it) {
            UploadViewModel.Error.UploadError -> R.string.upload_error
          }
        )
      }

    viewModel
      .openLogin()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { startActivity(LoginActivity.getIntent(this)) }

    viewModel
      .openDoc()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { startActivity(DocUploadedActivity.getIntent(this, it)) }

    viewModel
      .finish()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { finish(it) }

    uriReceived?.let {
      viewModel.fileToUploadReceived(it)
    } ?: finish()
  }

  override fun onStop() {
    super.onStop()
    loadingManager.hide()
  }

  companion object {
    private const val EXTRA_FILE_URI = "file_uri"

    fun getIntent(context: Context, extras: Extras) =
      Intent(context, UploadActivity::class.java).also {
        it.putExtra(EXTRA_FILE_URI, extras.fileUri)
      }
  }

  data class Extras(
    val fileUri: Uri
  )
}
