package app.envelop.ui.main

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import app.envelop.R
import app.envelop.data.models.Doc
import app.envelop.ui.BaseActivity
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.item_doc.view.*
import javax.inject.Inject

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class DocItemView
@JvmOverloads
constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  @Inject
  lateinit var getDate: FormatRelativeDate

  init {
    (context as BaseActivity).component.inject(this)
    inflate(context, R.layout.item_doc, this)
  }

  @ModelProp
  fun setItem(doc: Doc) {
    icon.contentDescription = doc.contentType
    icon.setImageResource(doc.fileType.iconRes)
    name.text = doc.name
    size.text = doc.humanSize
    uploadDate.text = if (doc.uploaded) {
      getDate.format(doc.createdAt)
    } else {
      resources.getString(R.string.uploading)
    }
  }

  @CallbackProp
  fun setClickListener(listener: (() -> Unit)?) {
    getChildAt(0).setOnClickListener {
      listener?.invoke()
    }
  }
}
