package app.envelop.ui.main

import app.envelop.common.Optional
import app.envelop.data.models.Doc
import app.envelop.domain.DeleteDocService
import app.envelop.domain.DocLinkBuilder
import app.envelop.domain.GetDocService
import app.envelop.ui.BaseViewModel
import app.envelop.ui.common.*
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class DocMenuViewModel
@Inject constructor(
  getDocService: GetDocService,
  deleteDocService: DeleteDocService,
  docLinkBuilder: DocLinkBuilder
) : BaseViewModel() {

  private val docIdReceived = BehaviorSubject.create<String>()
  private val deleteClicks = PublishSubject.create<Click>()
  private val deleteConfirmClicks = PublishSubject.create<Click>()

  private val doc = BehaviorSubject.create<Doc>()
  private val link = BehaviorSubject.create<String>()
  private val openDeleteConfirm = PublishSubject.create<Open>()
  private val openCannotDeleteConfirm = PublishSubject.create<Open>()
  private val isDeleting = BehaviorSubject.create<LoadingState>()
  private val errors = BehaviorSubject.create<Error>()
  private val finish = BehaviorSubject.create<Finish>()

  init {
    docIdReceived
      .flatMap { getDocService.get(it) }
      .filter { it is Optional.Some }
      .map { it.element()!! }
      .subscribe(doc::onNext)
      .addTo(disposables)

    doc
      .flatMapSingle { docLinkBuilder.build(it) }
      .subscribe(link::onNext)
      .addTo(disposables)

    deleteClicks
      .flatMap { doc.take(1) }
      .subscribe {
        if (it.canEdit) {
          openDeleteConfirm.open()
        } else {
          openCannotDeleteConfirm.open()
        }
      }
      .addTo(disposables)

    deleteConfirmClicks
      .doOnNext { isDeleting.loading() }
      .flatMap { doc.take(1) }
      .flatMapSingle { deleteDocService.markAsDeleted(it) }
      .subscribe {
        isDeleting.idle()
        if (it.isSuccessful) {
          finish.finish()
        } else {
          errors.onNext(Error.DeleteError)
        }
      }
      .addTo(disposables)
  }

  // Inputs

  fun docIdReceived(value: String) = docIdReceived.onNext(value)
  fun deleteClicked() = deleteClicks.click()
  fun deleteConfirmClicked() = deleteConfirmClicks.click()

  // Outputs

  fun doc() = doc.hide()!!
  fun link() = link.hide()!!
  fun openDeleteConfirm() = openDeleteConfirm.hide()!!
  fun openCannotDeleteConfirm() = openCannotDeleteConfirm.hide()!!
  fun isDeleting() = isDeleting.hide()!!
  fun errors() = errors.hide()!!
  fun finish() = finish.hide()!!

  sealed class Error {
    object DeleteError : Error()
  }

}
