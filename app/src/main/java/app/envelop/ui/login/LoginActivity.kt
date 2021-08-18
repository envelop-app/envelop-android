package app.envelop.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.view.isVisible
import app.envelop.R
import app.envelop.common.rx.observeOnUI
import app.envelop.domain.LoginService
import app.envelop.ui.BaseActivity
import app.envelop.ui.common.Insets.addSystemWindowInsetToPadding
import app.envelop.ui.common.MessageManager
import app.envelop.ui.common.clicksThrottled
import app.envelop.ui.common.loading.LoadingManager
import app.envelop.ui.main.MainActivity
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.partial_banner.*
import javax.inject.Inject

@SuppressLint("CheckResult")
class LoginActivity : BaseActivity() {

  @Inject
  lateinit var loginService: LoginService

  @Inject
  lateinit var loadingManager: LoadingManager

  @Inject
  lateinit var messageManager: MessageManager

  private val viewModel by lazy {
    component.viewModelProvider()[LoginViewModel::class.java].also {
      it.loginService = loginService
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    component.inject(this)
    setContentView(R.layout.activity_login)

    container.addSystemWindowInsetToPadding(top = true, bottom = true)

    bannerBtn1
      .clicksThrottled()
      .bindToLifecycle(this)
      .subscribe {
        startActivity(
          Intent(
            Intent.ACTION_VIEW,
            Uri.parse(getString(R.string.learn_more_url))
          )
        )
      }

    bannerBtn2
      .clicksThrottled()
      .bindToLifecycle(this)
      .subscribe { banner.isVisible = false }

    login
      .clicksThrottled()
      .bindToLifecycle(this)
      .subscribe { viewModel.loginClick() }

    viewModel
      .isLoggingIn()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        loadingManager.apply(it, R.string.login_progress)
      }

    viewModel
      .errors()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        messageManager.showError(
          when (it) {
            LoginViewModel.Error.UsernameMissing -> R.string.login_username_error
            else -> R.string.login_error
          }
        )
      }

    viewModel
      .finishToMain()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        startActivity(MainActivity.getIntent(this))
        finish(it)
      }

    if (intent?.action == Intent.ACTION_VIEW) {
      viewModel.authDataReceived(intent.data?.getQueryParameter("authResponse"))
    }
  }

  override fun onStop() {
    super.onStop()
    loadingManager.hide()
  }

  companion object {
    fun getIntent(context: Context) = Intent(context, LoginActivity::class.java)
  }

}
