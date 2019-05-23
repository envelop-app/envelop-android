package app.envelop.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.envelop.R
import app.envelop.common.rx.observeOnUI
import app.envelop.ui.BaseActivity
import app.envelop.ui.common.clicksThrottled
import app.envelop.ui.login.LoginActivity
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_loading.*

class MainActivity : BaseActivity() {

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

    viewModel
      .user()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { user.text = it.displayName }

    viewModel
      .finishToLogin()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        startActivity(LoginActivity.getIntent(this))
        finish()
      }
  }

  companion object {
    fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
  }
}
