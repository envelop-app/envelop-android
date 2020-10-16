package app.envelop.ui.login

import app.envelop.common.Operation
import app.envelop.domain.LoginService
import app.envelop.ui.common.Finish
import app.envelop.ui.common.LoadingState
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test


class LoginViewModelTest {

  val loginService = mock<LoginService>()
  val loginViewModel = LoginViewModel().also {
    it.loginService = loginService
  }

  @Test
  fun isLoggingInLoadingState() {

    val valueStream = loginViewModel.isLoggingIn().test()
    loginViewModel.loginClick()

    assertEquals(
      valueStream.values()[valueStream.values().size - 2],
      LoadingState.Loading
    )

    assertEquals(
      valueStream.values().last(),
      LoadingState.Idle
    )
  }

  @Test
  fun errorsAuth() {
    whenever(loginService.finishLogin(any())).doReturn(
      Single.just(Operation.error(LoginService.UsernameMissing("")))
    )

    val errorStream = loginViewModel.errors().test()
    loginViewModel.authDataReceived("")

    errorStream.assertValue(LoginViewModel.Error.UsernameMissing)
  }

  @Test
  fun finishToMain() {
    whenever(loginService.finishLogin(any())).doReturn(Single.just(Operation()))

    val finishStream = loginViewModel.finishToMain().test()
    loginViewModel.authDataReceived("")

    val finishResult = finishStream.values().first()
    assertEquals(finishResult.result, Finish.Result.Ok)
  }
}
