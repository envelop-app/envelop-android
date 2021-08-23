package app.envelop.ui.upload

import android.annotation.SuppressLint
import android.net.Uri
import app.envelop.common.Optional
import app.envelop.domain.PreUploadService
import app.envelop.domain.UserService
import app.envelop.ui.BaseViewModel
import app.envelop.ui.common.*
import app.envelop.ui.common.Finish.Result.Canceled
import app.envelop.ui.common.Finish.Result.Ok
import app.envelop.ui.share.ShareActivity
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("CheckResult")
class UploadViewModel
@Inject constructor(
  userService: UserService,
  preUploadService: PreUploadService
) : BaseViewModel() {

  private val fileToUploadReceived = PublishSubject.create<Uri>()

  private val isPreparingUpload = BehaviorSubject.create<LoadingState>()
  private val error = PublishSubject.create<Error>()
  private val openLogin = PublishSubject.create<Open>()
  private val openDoc = PublishSubject.create<ShareActivity.Extras>()
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
      .doOnNext { isPreparingUpload.loading() }
      .flatMapSingle { preUploadService.prepareUpload(it) }
      .subscribe {
        isPreparingUpload.idle()
        if (it.isSuccessful) {
          openDoc.onNext(ShareActivity.Extras(it.result()))
          finish.finish(Ok)
        } else {
          Timber.w(it.throwable())
          error.onNext(Error.UploadError)
          finish.finish(Canceled)
        }
      }
  }

  // Inputs

  fun fileToUploadReceived(value: Uri) = fileToUploadReceived.onNext(value)

  // Outputs

  fun isPreparingUpload() = isPreparingUpload.hide()!!
  fun error() = error.hide()!!
  fun openLogin() = openLogin.hide()!!
  fun openDoc() = openDoc.hide()!!
  fun finish() = finish.hide()!!

  sealed class Error {
    object UploadError : Error()
  }
}
