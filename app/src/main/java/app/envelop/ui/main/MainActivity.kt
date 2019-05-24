package app.envelop.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.envelop.R
import app.envelop.common.rx.observeOnUI
import app.envelop.ui.BaseActivity
import app.envelop.ui.common.ErrorManager
import app.envelop.ui.common.clicksThrottled
import app.envelop.ui.common.loading.LoadingManager
import app.envelop.ui.login.LoginActivity
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class MainActivity : BaseActivity() {

  @Inject
  lateinit var loadingManager: LoadingManager
  @Inject
  lateinit var errorManager: ErrorManager

  private val viewModel by lazy {
    component.viewModelProvider()[MainViewModel::class.java]
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    component.inject(this)
    setContentView(R.layout.activity_main)

    logout
      .clicksThrottled()
      .bindToLifecycle(this)
      .subscribe { viewModel.logoutClick() }

    upload
      .clicksThrottled()
      .bindToLifecycle(this)
      .subscribe { openFileIntent() }

    results
      .filter { it.requestCode == REQUEST_FILE && it.resultCode == Activity.RESULT_OK }
      .bindToLifecycle(this)
      .subscribe {
        it.intent?.data?.let {
          viewModel.uploadFileReceived(it)
        }
      }

    viewModel
      .user()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { user.text = it.displayName }

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
        errorManager.show(
          when (it) {
            MainViewModel.Error.UploadError -> R.string.upload_error
          }
        )
      }

    viewModel
      .finishToLogin()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        startActivity(LoginActivity.getIntent(this))
        finish()
      }
  }

  override fun onStop() {
    super.onStop()
    loadingManager.hide()
  }

  private fun openFileIntent() {
    startActivityForResult(
      Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "*/*"
      },
      REQUEST_FILE
    )
  }

  companion object {
    private const val REQUEST_FILE = 1001

    fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
  }
}
