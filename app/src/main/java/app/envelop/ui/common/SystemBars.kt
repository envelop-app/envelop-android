package app.envelop.ui.common

import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import app.envelop.R

object SystemBars {
  fun Window.setSystemBarsStyle(context: Context) {
    decorView.systemUiVisibility =
      getFullScreenModeFlags()
    navigationBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      ContextCompat.getColor(context, R.color.transparent)
    } else {
      ContextCompat.getColor(context, R.color.primaryTransparent)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      navigationBarDividerColor = ContextCompat.getColor(context, R.color.transparent)
    }
  }

  private fun getFullScreenModeFlags() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
    } else {
      (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }
}
