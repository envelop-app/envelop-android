package app.envelop.ui.donate

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import app.envelop.R
import app.envelop.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_donate.*
import kotlinx.android.synthetic.main.shared_appbar.*

class DonateActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_donate)
    toolbar.enableNavigation()
    donateCrypto.setOnClickListener { openDonateCrypto() }
    donateStacks.setOnClickListener { openDonateStacksLink() }
  }

  private fun openDonateCrypto() {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.donate_crypto_link)))
    startActivity(intent)
  }

  private fun openDonateStacksLink() {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.donate_stack_link)))
    startActivity(intent)
  }

  companion object {
    fun getIntent(context: Context) = Intent(context, DonateActivity::class.java)
  }
}
