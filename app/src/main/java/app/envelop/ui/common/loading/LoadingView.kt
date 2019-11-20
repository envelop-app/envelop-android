package app.envelop.ui.common.loading

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import app.envelop.R
import kotlinx.android.synthetic.main.view_loading.view.*

class LoadingView(context: Context) : FrameLayout(context) {

  init {
    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    View.inflate(context, R.layout.view_loading, this)
  }

  fun setMessage(messageRes: Int) = loadingMessage.setText(messageRes)
}