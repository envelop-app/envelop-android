package app.envelop.ui.main

import android.content.Context
import android.text.format.DateUtils.*
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
    icon.setImageResource(doc.fileType.iconRes)
    name.text = doc.name
    size.text = doc.humanSize
    uploadDate.text = if (doc.uploadedNonNull) {
      doc.createdAt.toRelativeString()
    } else {
      resources.getString(R.string.uploading)
    }
  }

  @CallbackProp
  fun setClickListener(listener: OnClickListener?) {
    getChildAt(0).setOnClickListener(listener)
  }

  private fun Date.toRelativeString() =
    getRelativeDateTimeString(context, time, MINUTE_IN_MILLIS, DAY_IN_MILLIS, FORMAT_ABBREV_ALL)
      .split(",")
      .first()
      .replace(Regex("(\\.|\\sago)"), "")

}