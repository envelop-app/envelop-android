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

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    component.inject(this)
    setContentView(R.layout.activity_faq)
    toolbar.enableNavigation()
    faq.settings.javaScriptEnabled = true
    faq.loadUrl(getString(R.string.faq_url))
    faq.webChromeClient = CustomChromeClient()
    faq.webViewClient = CustomWebViewClient()
  }

  inner class CustomChromeClient : WebChromeClient() {
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
      progressBar.progress = newProgress
      super.onProgressChanged(view, newProgress)
    }
  }

  inner class CustomWebViewClient : WebViewClient() {
    private var hadErrorLoadingWebView = false

    override fun onReceivedError(
      view: WebView,
      request: WebResourceRequest,
      error: WebResourceError
    ) {
      hadErrorLoadingWebView = true
      messageManager.showError(R.string.faq_host_not_found)
      faq.isVisible = false
      progressBar.isVisible = false
      super.onReceivedError(view, request, error)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
      openBrowser(request.url)
      return true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
      super.onPageFinished(view, url)
      if (!hadErrorLoadingWebView) {
        faq.isVisible = true
        progressBar.isVisible = true
      }
    }

    private fun openBrowser(url: Uri) {
      val intent = Intent(Intent.ACTION_VIEW, url)
      startActivity(intent)
    }
  }

  companion object {
    fun getIntent(context: Context) = Intent(context, FaqActivity::class.java)
  }
}
