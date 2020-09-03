@file:Suppress("DEPRECATION")
package app.envelop.ui.common

import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import app.envelop.R

object SystemBars {

  private val fullScreenModeFlags: Int
    get() {
      var flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
      }
      return flags
    }

  fun Window.setSystemBarsStyle(context: Context) {
    decorView.systemUiVisibility = fullScreenModeFlags
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      navigationBarColor = ContextCompat.getColor(context, R.color.transparent)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      navigationBarDividerColor = ContextCompat.getColor(context, R.color.transparent)
    }
  }
}
