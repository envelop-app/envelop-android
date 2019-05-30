package app.envelop.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import app.envelop.R
import app.envelop.common.rx.observeOnUI
import app.envelop.data.models.Doc
import app.envelop.ui.BaseActivity
import app.envelop.ui.common.LoadingState
import app.envelop.ui.common.clicksThrottled
import app.envelop.ui.doc.DocActivity
import app.envelop.ui.login.LoginActivity
import app.envelop.ui.upload.UploadActivity
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

  private val viewModel by lazy {
    component.viewModelProvider()[MainViewModel::class.java]
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    component.inject(this)
    setContentView(R.layout.activity_main)


    upload
      .clicksThrottled()
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
      .subscribe { title = it.displayName }

    viewModel
      .docs()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe(this::setListModels)

    viewModel
      .isRefreshing()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { refresh.isRefreshing = it is LoadingState.Loading }

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

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main, menu)

    menu
      .findItem(R.id.logout)
      .clicksThrottled()
      .bindToLifecycle(this)
      .subscribe { viewModel.logoutClick() }

    return true
  }

  private fun setListModels(docs: List<Doc>) {
    list.withModels {
      docs.forEach { doc ->
        docItemView {
          id(doc.id)
          item(doc)
          clickListener(View.OnClickListener { openDoc(doc) })
        }
      }
    }
  }

  private fun openFileIntent() {
    startActivityForResult(
      Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "*/*"
      },
      REQUEST_FILE
    )
  }

  private fun openDoc(doc: Doc) {
    startActivity(DocActivity.getIntent(this, DocActivity.Extras(doc)))
  }

  companion object {
    private const val REQUEST_FILE = 1001

    fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
  }
}
