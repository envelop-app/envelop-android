package app.envelop.ui.main

import app.envelop.common.Optional
import app.envelop.data.models.User
import app.envelop.domain.AuthService
import app.envelop.ui.BaseViewModel
import app.envelop.ui.common.Click
import app.envelop.ui.common.Finish
import app.envelop.ui.common.click
import app.envelop.ui.common.finish
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class MainViewModel
@Inject constructor(
  authService: AuthService
) : BaseViewModel() {

  private val logoutClicks = PublishSubject.create<Click>()

  private val user = BehaviorSubject.create<User>()
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

  }

  // Inputs

  fun logoutClick() = logoutClicks.click()

  // Outputs

  fun user() = user.hide()!!

  fun finishToLogin() = finishToLogin.hide()!!

}