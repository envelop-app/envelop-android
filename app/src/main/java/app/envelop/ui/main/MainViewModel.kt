package app.envelop.ui.main

import android.net.Uri
import app.envelop.common.Optional
import app.envelop.data.models.User
import app.envelop.domain.AuthService
import app.envelop.domain.UploadService
import app.envelop.ui.BaseViewModel
import app.envelop.ui.common.*
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class MainViewModel
@Inject constructor(
  authService: AuthService,
  uploadService: UploadService
) : BaseViewModel() {

  private val logoutClicks = PublishSubject.create<Click>()
  private val uploadFileReceived = PublishSubject.create<Uri>()

  private val user = BehaviorSubject.create<User>()
  private val isUploading = BehaviorSubject.create<LoadingState>()
  private val error = PublishSubject.create<Error>()
  private val finishToLogin = BehaviorSubject.create<Finish>()

  init {

    authService
      .user()
      .filter { it is Optional.Some }
      .map { it.element()!! }
      .subscribe(user::onNext)
      .addTo(disposables)

    authService
      .user()
      .filter { it is Optional.None }
      .subscribe { finishToLogin.finish() }
      .addTo(disposables)

    logoutClicks
      .flatMapCompletable { authService.logout() }
      .subscribe()
      .addTo(disposables)

    uploadFileReceived
      .doOnNext { isUploading.loading() }
      .flatMapSingle { uploadService.upload(it) }
      .subscribe {
        if (it.isSuccessful) {
          Timber.i("File uploaded: %s", it.result())
        } else {
          Timber.e(it.throwable())
          error.onNext(Error.UploadError)
        }
        isUploading.idle()
      }
  }

  // Inputs

  fun logoutClick() = logoutClicks.click()
  fun uploadFileReceived(value: Uri) = uploadFileReceived.onNext(value)

  // Outputs

  fun user() = user.hide()!!
  fun isUploading() = isUploading.hide()!!
  fun error() = error.hide()!!
  fun finishToLogin() = finishToLogin.hide()!!

  sealed class Error {
    object UploadError : Error()
  }
}