package app.envelop.ui.main

import android.content.Context
import android.text.format.DateUtils
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.util.AttributeSet
import android.widget.FrameLayout
import app.envelop.R
import app.envelop.data.models.Doc
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.item_doc.view.*
import java.util.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class DocItemView
@JvmOverloads
constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.item_doc, this)
  }

  @ModelProp
  fun setItem(doc: Doc) {
    icon.contentDescription = doc.contentType
    name.text = doc.name
    size.text = doc.humanSize
    uploadDate.text = doc.createdAt.toRelativeString()
  }

  @CallbackProp
  fun setClickListener(listener: OnClickListener?) {
    setOnClickListener(listener)
  }

  private fun Date.toRelativeString() =
    DateUtils
      .getRelativeTimeSpanString(time, System.currentTimeMillis(), MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL)
      .replace(Regex("\\sago"), "")

}