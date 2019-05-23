package app.envelop.ui.common

import android.app.Activity
import android.content.res.Resources
import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class ErrorManager
@Inject constructor(
  private val activity: Activity,
  private val resources: Resources
) {

  fun show(@StringRes errorRes: Int) =
    show(resources.getString(errorRes))

  fun show(error: String) =
    Snackbar
      .make(getRootView(), error, Snackbar.LENGTH_LONG)
      .show()

  private fun getRootView() = activity.findViewById<View>(android.R.id.content)

}