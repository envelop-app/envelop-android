package app.envelop.ui.main

import app.envelop.data.models.Doc
import app.envelop.domain.DocLinkBuilder
import app.envelop.domain.DocService
import app.envelop.ui.BaseViewModel
import app.envelop.ui.common.*
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class DocMenuViewModel
@Inject constructor(
  docService: DocService,
  docLinkBuilder: DocLinkBuilder
) : BaseViewModel() {

  private val docReceived = BehaviorSubject.create<Doc>()
  private val deleteClicks = PublishSubject.create<Click>()

  private val link = BehaviorSubject.create<String>()
  private val isDeleting = BehaviorSubject.create<LoadingState>()
  private val errors = BehaviorSubject.create<Error>()
  private val finish = BehaviorSubject.create<Finish>()

  init {
    docReceived
      .flatMapSingle { docLinkBuilder.build(it) }
      .subscribe(link::onNext)
      .addTo(disposables)

    deleteClicks
      .doOnNext { isDeleting.loading() }
      .flatMap { docReceived.take(1) }
      .flatMapSingle { docService.delete(it) }
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

  fun docReceived(value: Doc) = docReceived.onNext(value)
  fun deleteClicked() = deleteClicks.click()

  // Outputs

  fun link() = link.hide()!!
  fun isDeleting() = isDeleting.hide()!!
  fun errors() = errors.hide()!!
  fun finish() = finish.hide()!!

  sealed class Error {
    object DeleteError : Error()
  }

}
