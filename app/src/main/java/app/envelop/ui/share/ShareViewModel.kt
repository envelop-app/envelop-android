package app.envelop.ui.share

import app.envelop.common.Optional
import app.envelop.data.models.Doc
import app.envelop.domain.DocLinkBuilder
import app.envelop.domain.DocService
import app.envelop.ui.BaseViewModel
import app.envelop.ui.common.Finish
import app.envelop.ui.common.finish
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class ShareViewModel
@Inject constructor(
  docService: DocService,
  docLinkBuilder: DocLinkBuilder
) : BaseViewModel() {

  private val docIdReceived = PublishSubject.create<String>()

  private val doc = BehaviorSubject.create<Doc>()
  private val link = BehaviorSubject.create<String>()
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
  }

  // Inputs

  fun docIdReceived(value: String) = docIdReceived.onNext(value)

  // Outputs

  fun doc() = doc.hide()!!
  fun link() = link.hide()!!
  fun finish() = finish.hide()!!

}
