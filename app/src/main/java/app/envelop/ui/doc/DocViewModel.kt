package app.envelop.ui.doc

import app.envelop.common.Optional
import app.envelop.data.models.Doc
import app.envelop.domain.DocLinkBuilder
import app.envelop.domain.DocService
import app.envelop.ui.BaseViewModel
import app.envelop.ui.common.*
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class DocViewModel
@Inject constructor(
  docService: DocService,
  docLinkBuilder: DocLinkBuilder
) : BaseViewModel() {

  private val docIdReceived = PublishSubject.create<String>()
  private val deleteClicks = PublishSubject.create<Click>()

  private val doc = BehaviorSubject.create<Doc>()
  private val link = BehaviorSubject.create<String>()
  private val isDeleting = BehaviorSubject.create<LoadingState>()
  private val errors = BehaviorSubject.create<Error>()
  private val finish = BehaviorSubject.create<Finish>()

  init {
    docIdReceived
      .flatMap { docService.get(it) }
      .subscribe {
        when (it) {
          is Optional.Some -> doc.onNext(it.element)
          is Optional.None -> finish.finish()
        }
      }
      .addTo(disposables)

    doc
      .flatMapSingle { docLinkBuilder.build(it) }
      .subscribe(link::onNext)
      .addTo(disposables)

    deleteClicks
      .doOnNext { isDeleting.loading() }
      .flatMap { doc.take(1) }
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

  fun docIdReceived(value: String) = docIdReceived.onNext(value)
  fun deleteClicked() = deleteClicks.click()

  // Outputs

  fun doc() = doc.hide()!!
  fun link() = link.hide()!!
  fun isDeleting() = isDeleting.hide()!!
  fun errors() = errors.hide()!!
  fun finish() = finish.hide()!!

  sealed class Error {
    object DeleteError : Error()
  }

}
