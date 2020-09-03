@file:Suppress("DEPRECATION")
package app.envelop.ui.common

import android.view.View
import android.view.ViewGroup
import androidx.core.view.*

object Insets {
  fun View.addSystemWindowInsetToMargin(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false
  ) {
    val (initialLeft, initialTop, initialRight, initialBottom) =
      listOf(marginLeft, marginTop, marginRight, marginBottom)

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
      view.updateLayoutParams {
        (this as? ViewGroup.MarginLayoutParams)?.let {
          updateMargins(
            left = initialLeft + (if (left) insets.systemWindowInsetLeft else 0),
            top = initialTop + (if (top) insets.systemWindowInsetTop else 0),
            right = initialRight + (if (right) insets.systemWindowInsetRight else 0),
            bottom = initialBottom + (if (bottom) insets.systemWindowInsetBottom else 0)
          )
        }
      }
      insets
    }
  }

  fun View.addSystemWindowInsetToPadding(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false
  ) {
    val (initialLeft, initialTop, initialRight, initialBottom) =
      listOf(paddingLeft, paddingTop, paddingRight, paddingBottom)

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
      view.updatePadding(
        left = initialLeft + (if (left) insets.systemWindowInsetLeft else 0),
        top = initialTop + (if (top) insets.systemWindowInsetTop else 0),
        right = initialRight + (if (right) insets.systemWindowInsetRight else 0),
        bottom = initialBottom + (if (bottom) insets.systemWindowInsetBottom else 0)
      )
      insets
    }
  }
}
