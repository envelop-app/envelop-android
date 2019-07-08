package app.envelop.ui.share

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class InterceptView
@JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
    return true
  }
}