package app.envelop.ui.common

import android.app.Activity
import android.content.res.Resources
import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class MessageManager
@Inject constructor(
  private val activity: Activity,
  private val resources: Resources
) {

  fun showNotice(@StringRes messageRes: Int) =
    showNotice(resources.getString(messageRes))

  private fun showNotice(message: String) =
    Snackbar
      .make(getRootView(), message, Snackbar.LENGTH_LONG)
      .show()

  fun showError(@StringRes errorRes: Int) =
    showError(resources.getString(errorRes))

  private fun showError(error: String) =
    Snackbar
      .make(getRootView(), error, Snackbar.LENGTH_LONG)
      .show()

  private fun getRootView() = activity.findViewById<View>(android.R.id.content)

}