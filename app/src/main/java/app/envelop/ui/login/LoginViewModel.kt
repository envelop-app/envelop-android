package app.envelop.ui.login

import app.envelop.domain.LoginService
import app.envelop.ui.BaseViewModel
import app.envelop.ui.common.*
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel
@Inject constructor() : BaseViewModel() {

  lateinit var loginService: LoginService // this must be provided by the activity

  private val loginClicks = PublishSubject.create<Click>()
  private val authDataReceived = PublishSubject.create<String>()

  private val isLoggingIn = BehaviorSubject.create<LoadingState>()
  private val errors = PublishSubject.create<Error>()
  private val finishToMain = PublishSubject.create<Finish>()

  init {

    loginClicks
      .doOnNext { isLoggingIn.loading() }
      .flatMapSingle { loginService.login() }
      .subscribe {
        if (it.isError) {
          Timber.e(it.throwable())
          errors.onNext(Error.LoginError)
        }
        isLoggingIn.idle()
      }
      .addTo(disposables)

    authDataReceived
      .doOnNext { isLoggingIn.loading() }
      .flatMapSingle { loginService.finishLogin(it) }
      .subscribe {
        isLoggingIn.idle()
        if (it.isSuccessful) {
          finishToMain.finish(Finish.Result.Ok)
        } else {
          Timber.e(it.throwable())
          errors.onNext(Error.LoginError)
        }
      }
      .addTo(disposables)

  }

  override fun onCleared() {
    loginService
      .cleanUp()
      .subscribe()
      .addTo(disposables)
    super.onCleared()
  }

  // Inputs

  fun loginClick() = loginClicks.click()
  fun authDataReceived(value: String?) = authDataReceived.onNext(value ?: "")

  // Outputs

  fun isLoggingIn() = isLoggingIn.hide()!!
  fun errors() = errors.hide()!!
  fun finishToMain() = finishToMain.hide()!!

  sealed class Error {
    object LoginError : Error()
  }
}