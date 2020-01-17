package app.envelop.ui.main

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import app.envelop.R
import app.envelop.data.models.Doc
import app.envelop.ui.BaseActivity
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
  lateinit var getDate: GetFormattedDateString

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
      getDate.toDateStringFormatted(doc.createdAt)
    } else {
      resources.getString(R.string.uploading)
    }
    getChildAt(0).setOnClickListener {
      openMenu(doc)
    }
  }

  private fun openMenu(doc: Doc) {
    DocMenuFragment
      .newInstance(doc)
      .show((context as AppCompatActivity).supportFragmentManager, FRAGMENT_DOC_MENU_TAG)
  }

  companion object {
    private const val FRAGMENT_DOC_MENU_TAG = "doc_menu"
  }

}
