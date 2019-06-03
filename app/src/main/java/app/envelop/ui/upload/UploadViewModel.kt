package app.envelop.ui.upload

import android.net.Uri
import app.envelop.common.Optional
import app.envelop.domain.UploadService
import app.envelop.domain.UserService
import app.envelop.ui.BaseViewModel
import app.envelop.ui.common.*
import app.envelop.ui.common.Finish.Result.Canceled
import app.envelop.ui.common.Finish.Result.Ok
import app.envelop.ui.docuploaded.DocUploadedActivity
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class UploadViewModel
@Inject constructor(
  userService: UserService,
  uploadService: UploadService
) : BaseViewModel() {

  private val fileToUploadReceived = PublishSubject.create<Uri>()

  private val isUploading = BehaviorSubject.create<LoadingState>()
  private val error = PublishSubject.create<Error>()
  private val openLogin = PublishSubject.create<Open>()
  private val openDoc = PublishSubject.create<DocUploadedActivity.Extras>()
  private val finish = PublishSubject.create<Finish>()

  init {

    userService
      .user()
      .filter { it is Optional.None }
      .subscribe {
        openLogin.open()
        finish.finish(Canceled)
      }
      .addTo(disposables)

    fileToUploadReceived
      .doOnNext { isUploading.loading() }
      .flatMapSingle { uploadService.upload(it) }
      .subscribe {
        isUploading.idle()
        if (it.isSuccessful) {
          openDoc.onNext(DocUploadedActivity.Extras(it.result()))
          finish.finish(Ok)
        } else {
          Timber.e(it.throwable())
          error.onNext(Error.UploadError)
          finish.finish(Canceled)
        }
      }
  }

  // Inputs

  fun fileToUploadReceived(value: Uri) = fileToUploadReceived.onNext(value)

  // Outputs

  fun isUploading() = isUploading.hide()!!
  fun error() = error.hide()!!
  fun openLogin() = openLogin.hide()!!
  fun openDoc() = openDoc.hide()!!
  fun finish() = finish.hide()!!

  sealed class Error {
    object UploadError : Error()
  }
}