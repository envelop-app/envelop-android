package app.envelop.ui.main

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import app.envelop.BuildConfig
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

  private var alertDialog: Dialog? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View =
    inflater.inflate(R.layout.view_doc_menu, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    (activity as BaseActivity).component.inject(this)

    (activity as BaseActivity)
      .lifecycle
      .addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
          dismiss()
        }
      })

    copyLink
      .clicksThrottled()
      .flatMap { viewModel.link().take(1) }
      .bindToLifecycle(activity as BaseActivity)
      .observeOnUI()
      .subscribe {
        dismiss()
        docActions.copyLink(it)
      }

    share
      .clicksThrottled()
      .flatMap { viewModel.link().take(1) }
      .bindToLifecycle(activity as BaseActivity)
      .observeOnUI()
      .subscribe {
        dismiss()
        docActions.share(it)
      }

    openLink
      .clicksThrottled()
      .flatMap { viewModel.link().take(1) }
      .bindToLifecycle(activity as BaseActivity)
      .observeOnUI()
      .subscribe {
        dismiss()
        docActions.open(it)
      }

    delete
      .clicksThrottled()
      .bindToLifecycle(activity as BaseActivity)
      .observeOnUI()
      .subscribe { viewModel.deleteClicked() }

    viewModel
      .doc()
      .bindToLifecycle(activity as BaseActivity)
      .observeOnUI()
      .subscribe { setDocInfo(it) }

    viewModel
      .openDeleteConfirm()
      .bindToLifecycle(activity as BaseActivity)
      .observeOnUI()
      .subscribe { openDeleteConfirm() }

    viewModel
      .openCannotDeleteConfirm()
      .bindToLifecycle(activity as BaseActivity)
      .observeOnUI()
      .subscribe { openCannotDeleteAlert() }

    viewModel
      .isDeleting()
      .bindToLifecycle(activity as BaseActivity)
      .observeOnUI()
      .subscribe { loadingManager.apply(it, R.string.doc_deleting) }

    viewModel
      .errors()
      .bindToLifecycle(activity as BaseActivity)
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
      .bindToLifecycle(activity as BaseActivity)
      .observeOnUI()
      .subscribe { dismiss() }

    arguments?.getString(EXTRA_DOC_ID)?.let {
      viewModel.docIdReceived(it)
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
      alertDialog = AlertDialog.Builder(it)
        .setTitle(R.string.confirm)
        .setMessage(R.string.doc_delete_message)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.doc_delete) { _, _ -> viewModel.deleteConfirmClicked() }
        .show()
    }
  }

  private fun openCannotDeleteAlert() {
    activity?.let {
      alertDialog = AlertDialog.Builder(it)
        .setTitle(R.string.doc_cannot_delete_title)
        .setMessage(R.string.doc_cannot_delete_message)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.update) { _, _ -> openAppInStore() }
        .show()
    }
  }

  private fun openAppInStore() =
    activity?.startActivity(
      Intent(Intent.ACTION_VIEW).also {
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        it.data =
          Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID.replace(".debug", "")}")
      }
    )

  companion object {
    private const val EXTRA_DOC_ID = "doc_id"
    fun newInstance(doc: Doc) =
      DocMenuFragment().also {
        it.arguments = Bundle().also { args ->
          args.putString(EXTRA_DOC_ID, doc.id)
        }
      }
  }

}