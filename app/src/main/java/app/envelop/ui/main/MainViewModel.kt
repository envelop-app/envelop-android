package app.envelop.ui.main

import android.net.Uri
import app.envelop.common.Optional
import app.envelop.data.models.Doc
import app.envelop.data.models.User
import app.envelop.domain.IndexService
import app.envelop.domain.LogoutService
import app.envelop.domain.UserService
import app.envelop.ui.BaseViewModel
import app.envelop.ui.common.*
import app.envelop.ui.upload.UploadActivity
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class MainViewModel
@Inject constructor(
  userService: UserService,
  logoutService: LogoutService,
  indexService: IndexService
) : BaseViewModel() {

  private val logoutClicks = PublishSubject.create<Click>()
  private val uploadFileReceived = PublishSubject.create<Uri>()
  private val refreshes = PublishSubject.create<Refresh>()

  private val user = BehaviorSubject.create<User>()
  private val docs = BehaviorSubject.create<List<Doc>>()
  private val isEmptyVisible = BehaviorSubject.createDefault<VisibleState>(VisibleState.Hidden)
  private val isUploadButtonVisible = BehaviorSubject.createDefault<VisibleState>(VisibleState.Visible)
  private val isRefreshing = BehaviorSubject.create<LoadingState>()
  private val errors = PublishSubject.create<Error>()
  private val openUpload = BehaviorSubject.create<UploadActivity.Extras>()
  private val finishToLogin = BehaviorSubject.create<Finish>()

  init {

    userService
      .user()
      .filter { it is Optional.Some }
      .map { it.element()!! }
      .subscribe(user::onNext)
      .addTo(disposables)

    userService
      .user()
      .filter { it is Optional.None }
      .subscribe { finishToLogin.finish(Finish.Result.Canceled) }
      .addTo(disposables)

    val ifLoggedIn = user.take(1)

    ifLoggedIn
      .flatMap { logoutClicks }
      .flatMapCompletable { logoutService.logout() }
      .subscribe()
      .addTo(disposables)

    ifLoggedIn
      .flatMap { uploadFileReceived }
      .subscribe {
        openUpload.onNext(UploadActivity.Extras(it))
      }
      .addTo(disposables)

    ifLoggedIn
      .flatMap { refreshes }
      .startWith(Refresh)
      .doOnNext { isRefreshing.loading() }
      .flatMapSingle { indexService.download() }
      .subscribe {
        isRefreshing.idle()
        if (it.isError) {
          Timber.e(it.throwable())
          errors.onNext(Error.RefreshError)
        }
      }
      .addTo(disposables)

    ifLoggedIn
      .flatMap { indexService.get() }
      .subscribe {
        docs.onNext(it)
        isEmptyVisible.next(it.isEmpty())
        isUploadButtonVisible.next(it.isNotEmpty())
      }
      .addTo(disposables)
  }

  // Inputs

  fun logoutClick() = logoutClicks.click()
  fun refresh() = refreshes.refresh()
  fun uploadFileReceived(value: Uri) = uploadFileReceived.onNext(value)

  // Outputs

  fun user() = user.hide()!!
  fun docs() = docs.hide()!!
  fun isEmptyVisible() = isEmptyVisible.hide()!!
  fun isUploadButtonVisible() = isUploadButtonVisible.hide()!!
  fun isRefreshing() = isRefreshing.hide()!!
  fun errors() = errors.hide()!!
  fun openUpload() = openUpload.hide()!!
  fun finishToLogin() = finishToLogin.hide()!!

  sealed class Error {
    object RefreshError : Error()
  }
}