package app.envelop.ui.main

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import app.envelop.R
import app.envelop.common.rx.observeOnUI
import app.envelop.data.models.Doc
import app.envelop.ui.BaseActivity
import app.envelop.ui.common.LoadingState
import app.envelop.ui.common.MessageManager
import app.envelop.ui.common.clicksThrottled
import app.envelop.ui.common.setVisible
import app.envelop.ui.faq.FaqActivity
import app.envelop.ui.donate.DonateActivity
import app.envelop.ui.login.LoginActivity
import app.envelop.ui.upload.UploadActivity
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindToLifecycle
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.shared_appbar.*
import javax.inject.Inject

class MainActivity : BaseActivity() {

  @Inject
  lateinit var messageManager: MessageManager

  private val viewModel by lazy {
    component.viewModelProvider()[MainViewModel::class.java]
  }

  private var logoutDialog: Dialog? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    component.inject(this)
    setContentView(R.layout.activity_main)
    toolbar.setTitle(R.string.main_title)
    toolbar.setupMenu(R.menu.main)

    toolbar
      .itemClicks(R.id.feedback)
      .bindToLifecycle(this)
      .subscribe { openFeedback() }

    toolbar
      .itemClicks(R.id.faq)
      .bindToLifecycle(this)
      .subscribe { openFaq() }

    toolbar
      .itemClicks(R.id.donate)
      .bindToLifecycle(this)
      .subscribe { openDonate() }

    toolbar
      .itemClicks(R.id.logout)
      .bindToLifecycle(this)
      .subscribe { openLogoutConfirm() }

    Observable
      .merge(
        upload.clicksThrottled(),
        empty.clicksThrottled()
      )
      .bindToLifecycle(this)
      .subscribe { openFileIntent() }

    refresh
      .refreshes()
      .bindToLifecycle(this)
      .subscribe { viewModel.refresh() }

    results
      .filter { it.requestCode == REQUEST_FILE && it.resultCode == Activity.RESULT_OK }
      .bindToLifecycle(this)
      .subscribe { result ->
        result.intent?.data?.let {
          viewModel.uploadFileReceived(it)
        }
      }

    viewModel
      .user()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { toolbar.menu.findItem(R.id.user).title = it.displayName }

    viewModel
      .docs()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe(this::setListModels)

    viewModel
      .isEmptyVisible()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe(empty::setVisible)

    viewModel
      .isUploadButtonVisible()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe(upload::setVisible)

    viewModel
      .isRefreshing()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { refresh.isRefreshing = it is LoadingState.Loading }

    viewModel
      .errors()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        messageManager.showError(
          when (it) {
            MainViewModel.Error.RefreshError -> R.string.main_refresh_error
          }
        )
      }

    viewModel
      .openUpload()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { startActivity(UploadActivity.getIntent(this, it)) }

    viewModel
      .finishToLogin()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        startActivity(LoginActivity.getIntent(this))
        finish(it)
      }
  }

  override fun onDestroy() {
    super.onDestroy()
    logoutDialog?.dismiss()
    logoutDialog = null
  }

  private fun setListModels(docs: List<Doc>) {
    list.withModels {
      // Invisible top view to make sure new items are visible when added
      dummyItemView {
        id("top")
      }
      docs.forEach { doc ->
        docItemView {
          id(doc.id)
          item(doc)
        }
      }
    }
  }

  private fun openFeedback() {
    startActivity(
      Intent.createChooser(
        Intent(Intent.ACTION_SENDTO).also {
          it.data = Uri.parse("mailto:${getString(R.string.feedback_email)}")
          it.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.main_feedback_email_subject))
        },
        getString(R.string.main_feedback)
      )
    )
  }

  private fun openFaq() {
    startActivity(FaqActivity.getIntent(this))
  }
  
  private fun openDonate() {
    startActivity(DonateActivity.getIntent(this))
  }

  private fun openLogoutConfirm() {
    logoutDialog?.dismiss()
    logoutDialog = AlertDialog.Builder(this)
      .setTitle(R.string.confirm)
      .setNegativeButton(R.string.cancel, null)
      .setPositiveButton(R.string.logout) { _, _ -> viewModel.logoutClick() }
      .show()
  }

  private fun openFileIntent() {
    startActivityForResult(
      Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "*/*"
        addCategory(Intent.CATEGORY_OPENABLE)
      },
      REQUEST_FILE
    )
  }

  companion object {
    private const val REQUEST_FILE = 1001

    fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
  }
}
