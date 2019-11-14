package app.envelop.ui.faq

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.core.view.isVisible
import app.envelop.R
import app.envelop.ui.BaseActivity
import app.envelop.ui.common.MessageManager
import kotlinx.android.synthetic.main.activity_faq.*
import kotlinx.android.synthetic.main.shared_appbar.*
import javax.inject.Inject

class FaqActivity : BaseActivity() {

  @Inject
  lateinit var messageManager: MessageManager
  private var hadError = false

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    component.inject(this)
    setContentView(R.layout.activity_faq)
    toolbar.setTitle(R.string.faq_title)
    toolbar.enableNavigation()

    faq.settings.javaScriptEnabled = true
    faq.loadUrl(getString(R.string.faq_url))
    faq.webChromeClient = object : WebChromeClient() {
      override fun onProgressChanged(view: WebView?, newProgress: Int) {
        progressBar.progress = newProgress
        super.onProgressChanged(view, newProgress)
      }
    }
    faq.webViewClient = object : WebViewClient() {
      override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
      ) {
        hadError = true
        error?.let {
          messageManager.showError(R.string.host_not_found)
          super.onReceivedError(view, request, error)
        }
        faq.isVisible = false
        progressBar.isVisible = false
      }

      override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url.toString()
        return if (url.startsWith(getString(R.string.http)) || url.startsWith(getString(R.string.https))) {
          openBrowser(request?.url)
          true
        } else if (url.startsWith(getString(R.string.mail_to))) {
          openEmail(url.substring(url.indexOf(":") + 1))
          true
        } else
          false
      }

      override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (!hadError) {
          faq.isVisible = true
          progressBar.isVisible = true
        }
      }
    }
  }

  private fun openEmail(email: String) {
    val mailer = Intent(Intent.ACTION_SEND)
    mailer.type = getString(R.string.text)
    mailer.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    startActivity(Intent.createChooser(mailer, getString(R.string.send_email)))
  }

  private fun openBrowser(url: Uri?) {
    url?.let {
      val intent = Intent(Intent.ACTION_VIEW, url)
      startActivity(intent)
    }
  }

  companion object {

    fun getIntent(context: Context) = Intent(context, FaqActivity::class.java)
  }
}
