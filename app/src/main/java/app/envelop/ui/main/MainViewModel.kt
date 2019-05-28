package app.envelop.ui.main

import android.net.Uri
import app.envelop.common.Optional
import app.envelop.data.models.User
import app.envelop.domain.AuthService
import app.envelop.domain.UploadService
import app.envelop.ui.BaseViewModel
import app.envelop.ui.common.Click
import app.envelop.ui.common.Finish
import app.envelop.ui.common.click
import app.envelop.ui.common.finish
import app.envelop.ui.upload.UploadActivity
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class MainViewModel
@Inject constructor(
  authService: AuthService,
  uploadService: UploadService
) : BaseViewModel() {

  private val logoutClicks = PublishSubject.create<Click>()
  private val uploadFileReceived = PublishSubject.create<Uri>()

  private val user = BehaviorSubject.create<User>()
  private val openUpload = BehaviorSubject.create<UploadActivity.Extras>()
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
      .subscribe { finishToLogin.finish(Finish.Result.Canceled) }
      .addTo(disposables)

    logoutClicks
      .flatMapCompletable { authService.logout() }
      .subscribe()
      .addTo(disposables)

    uploadFileReceived
      .subscribe {
        openUpload.onNext(UploadActivity.Extras(it))
      }
  }

  // Inputs

  fun logoutClick() = logoutClicks.click()
  fun uploadFileReceived(value: Uri) = uploadFileReceived.onNext(value)

  // Outputs

  fun user() = user.hide()!!
  fun openUpload() = openUpload.hide()!!
  fun finishToLogin() = finishToLogin.hide()!!
}