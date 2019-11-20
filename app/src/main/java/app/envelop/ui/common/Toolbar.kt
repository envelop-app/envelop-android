package app.envelop.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.Menu
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import app.envelop.R
import app.envelop.ui.BaseActivity
import com.jakewharton.rxbinding3.appcompat.itemClicks
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.view_toolbar.view.*

class Toolbar
@JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  val menu: Menu get() = innerToolbar.menu

  private val activity get() = (context as BaseActivity)
  private val itemClicks = PublishSubject.create<Int>()

  init {
    activity.component.inject(this)
    View.inflate(context, R.layout.view_toolbar, this)
    innerToolbar.title = ""
    setTitle(activity.title.toString())
  }

  fun setTitle(@StringRes titleRes: Int) {
    title.setText(titleRes)
  }

  private fun setTitle(titleStr: String) {
    title.text = titleStr
  }

  fun enableNavigation(
    @DrawableRes iconRes: Int = R.drawable.ic_back,
    @StringRes descriptionRes: Int = R.string.back,
    callback: () -> Unit = { activity.onBackPressed() }
  ) {
    innerToolbar.navigationIcon = ResourcesCompat.getDrawable(resources, iconRes, context.theme)
    innerToolbar.navigationContentDescription = resources.getString(descriptionRes)
    innerToolbar.setNavigationOnClickListener { callback.invoke() }
  }

  fun setupMenu(@MenuRes menuRes: Int) {
    activity.menuInflater.inflate(menuRes, innerToolbar.menu)

    innerToolbar
      .itemClicks()
      .map { it.itemId }
      .subscribe(itemClicks::onNext)
  }

  fun itemClicks(itemId: Int? = null): Observable<Int> =
    itemClicks.filter { itemId == null || it == itemId }

  @Suppress("unused")
  fun itemClicksThrottled(itemId: Int? = null) =
    itemClicks(itemId).throttleForClicks()

}

