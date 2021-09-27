package app.envelop.ui.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import app.envelop.BuildConfig
import app.envelop.R
import app.envelop.common.rx.observeOnUI
import app.envelop.data.models.Doc
import app.envelop.ui.BaseActivity
import app.envelop.ui.common.DocActions
import app.envelop.ui.common.Insets.addSystemWindowInsetToPadding
import app.envelop.ui.common.MessageManager
import app.envelop.ui.common.clicksThrottled
import app.envelop.ui.common.loading.LoadingManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.view_doc_menu.view.*
import javax.inject.Inject

class DocMenuView
@JvmOverloads
constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  @Inject
  lateinit var viewModel: DocMenuViewModel
  @Inject
  lateinit var messageManager: MessageManager
  @Inject
  lateinit var loadingManager: LoadingManager
  @Inject
  lateinit var docActions: DocActions

  private var alertDialog: Dialog? = null

  private val activity get() = context as BaseActivity
  private val behaviour get() = ((layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? BottomSheetBehavior<View>)

  init {
    activity.component.inject(this)
    inflate(context, R.layout.view_doc_menu, this)
    addSystemWindowInsetToPadding(bottom = true)

    copyLink
      .clicksThrottled()
      .flatMap { viewModel.link().take(1) }
      .bindToLifecycle(activity)
      .observeOnUI()
      .subscribe {
        hide()
        docActions.copyLink(it)
      }

    share
      .clicksThrottled()
      .flatMap { viewModel.link().take(1) }
      .bindToLifecycle(activity)
      .observeOnUI()
      .subscribe {
        hide()
        docActions.share(it)
      }

    openLink
      .clicksThrottled()
      .flatMap { viewModel.link().take(1) }
      .bindToLifecycle(activity)
      .observeOnUI()
      .subscribe {
        hide()
        docActions.open(it)
      }

    delete
      .clicksThrottled()
      .bindToLifecycle(activity)
      .observeOnUI()
      .subscribe { viewModel.deleteClicked() }

    viewModel
      .doc()
      .bindToLifecycle(activity)
      .observeOnUI()
      .subscribe { setDocInfo(it) }

    viewModel
      .openDeleteConfirm()
      .bindToLifecycle(activity)
      .observeOnUI()
      .subscribe { openDeleteConfirm() }

    viewModel
      .openCannotDeleteConfirm()
      .bindToLifecycle(activity)
      .observeOnUI()
      .subscribe { openCannotDeleteAlert() }

    viewModel
      .isDeleting()
      .bindToLifecycle(activity)
      .observeOnUI()
      .subscribe { loadingManager.apply(it, R.string.doc_deleting) }

    viewModel
      .errors()
      .bindToLifecycle(activity)
      .observeOnUI()
      .subscribe {
        messageManager.showError(
          when (it) {
            DocMenuViewModel.Error.DeleteError -> R.string.doc_delete_error
          }
        )
      }

    viewModel
      .finish()
      .bindToLifecycle(activity)
      .observeOnUI()
      .subscribe { hide() }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    behaviour?.state = BottomSheetBehavior.STATE_HIDDEN
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    loadingManager.hide()
  }

  fun isOpen() = behaviour?.state == BottomSheetBehavior.STATE_EXPANDED

  fun showDoc(docId: String) {
    viewModel.docIdReceived(docId)
    behaviour?.state = BottomSheetBehavior.STATE_EXPANDED
  }

  fun hide() {
    behaviour?.state = BottomSheetBehavior.STATE_HIDDEN
  }

  private fun setDocInfo(doc: Doc) {
    icon.contentDescription = doc.contentType
    icon.setImageResource(doc.fileType.iconRes)
    name.text = doc.name
    size.text = doc.humanSize
    behaviour?.state = BottomSheetBehavior.STATE_EXPANDED
    requestLayout() // Reposition bottom sheet according to the new view height
  }

  private fun openDeleteConfirm() {
    hide()
    alertDialog = AlertDialog.Builder(activity)
      .setTitle(R.string.confirm)
      .setMessage(R.string.doc_delete_message)
      .setNegativeButton(R.string.cancel, null)
      .setPositiveButton(R.string.doc_delete) { _, _ ->
        viewModel.deleteConfirmClicked()
      }
      .show()
  }

  private fun openCannotDeleteAlert() {
    alertDialog = AlertDialog.Builder(activity)
      .setTitle(R.string.doc_cannot_delete_title)
      .setMessage(R.string.doc_cannot_delete_message)
      .setNegativeButton(R.string.cancel, null)
      .setPositiveButton(R.string.update) { _, _ -> openAppInStore() }
      .show()
  }

  private fun openAppInStore() =
    activity.startActivity(
      Intent(Intent.ACTION_VIEW).also {
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        it.data =
          Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID.replace(".debug", "")}")
      }
    )
}
