package app.envelop.ui.common.loading

import android.app.Activity
import android.app.AlertDialog
import android.view.WindowManager
import app.envelop.R
import app.envelop.ui.common.LoadingState
import javax.inject.Inject

class LoadingManager
@Inject constructor(
  private val activity: Activity
) {

  private var dialog: AlertDialog? = null

  private fun show(messageRes: Int) {
    dialog?.let { hide() }
    dialog = AlertDialog.Builder(activity)
      .setView(
        LoadingView(activity).also {
          it.setMessage(messageRes)
        }
      )
      .setCancelable(false)
      .show().also { dialog ->
        // Set the correct width for the dialog
        dialog.window?.let { window ->
          window.attributes = WindowManager.LayoutParams().also {
            it.copyFrom(window.attributes)
            it.width = activity.resources.getDimension(R.dimen.progress_dialog_width).toInt()
          }
        }
      }
  }

  fun hide() {
    dialog?.dismiss()
    dialog = null
  }

  fun apply(state: LoadingState, messageRes: Int) {
    when (state) {
      LoadingState.Loading -> show(messageRes)
      LoadingState.Idle -> hide()
    }
  }
}