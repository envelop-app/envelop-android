package app.envelop.ui.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import app.envelop.R
import app.envelop.common.rx.observeOnUI
import app.envelop.data.models.Doc
import app.envelop.ui.BaseActivity
import app.envelop.ui.common.DocActions
import app.envelop.ui.common.MessageManager
import app.envelop.ui.common.clicksThrottled
import app.envelop.ui.common.loading.LoadingManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.view_doc_menu.*
import javax.inject.Inject

class DocMenuFragment : BottomSheetDialogFragment() {

  @Inject
  lateinit var viewModel: DocMenuViewModel
  @Inject
  lateinit var messageManager: MessageManager
  @Inject
  lateinit var loadingManager: LoadingManager
  @Inject
  lateinit var docActions: DocActions

  private var deleteDialog: Dialog? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
    inflater.inflate(R.layout.view_doc_menu, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    (activity as BaseActivity).component.inject(this)

    copyLink
      .clicksThrottled()
      .flatMap { viewModel.link().take(1) }
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        dismiss()
        docActions.copyLink(it)
      }

    share
      .clicksThrottled()
      .flatMap { viewModel.link().take(1) }
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        dismiss()
        docActions.share(it)
      }

    openLink
      .clicksThrottled()
      .flatMap { viewModel.link().take(1) }
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe {
        dismiss()
        docActions.open(it)
      }

    delete
      .clicksThrottled()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { openDeleteConfirm() }

    viewModel
      .isDeleting()
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { loadingManager.apply(it, R.string.doc_deleting) }

    viewModel
      .errors()
      .bindToLifecycle(this)
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
      .bindToLifecycle(this)
      .observeOnUI()
      .subscribe { dismiss() }

    arguments?.getParcelable<Doc>(EXTRA_DOC)?.let {
      viewModel.docReceived(it)
      setDocInfo(it)
    } ?: dismiss()
  }

  override fun onDestroy() {
    super.onDestroy()
    loadingManager.hide()
  }

  private fun setDocInfo(it: Doc) {
    icon.contentDescription = it.contentType
    icon.setImageResource(it.fileType.iconRes)
    name.text = it.name
    size.text = it.humanSize
  }

  private fun openDeleteConfirm() {
    activity?.let {
      deleteDialog = AlertDialog.Builder(it)
        .setTitle(R.string.doc_delete_title)
        .setMessage(R.string.doc_delete_message)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.doc_delete) { _, _ -> viewModel.deleteClicked() }
        .show()
    }
  }

  companion object {
    private const val EXTRA_DOC = "doc"
    fun newInstance(doc: Doc) =
      DocMenuFragment().also {
        it.arguments = Bundle().also { args ->
          args.putParcelable(EXTRA_DOC, doc)
        }
      }
  }

}