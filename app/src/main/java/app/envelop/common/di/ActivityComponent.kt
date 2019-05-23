package app.envelop.common.di

import app.envelop.data.BlockstackModule
import app.envelop.ui.login.LoginActivity
import app.envelop.ui.main.MainActivity
import dagger.Subcomponent
import org.blockstack.android.sdk.BlockstackSession

@PerActivity
@Subcomponent(
  modules = [
    ActivityModule::class
  ]
)
interface ActivityComponent {

  fun viewModelProvider(): ActivityViewModelProvider

  // Activities

  fun inject(mainActivity: MainActivity)
  fun inject(loginActivity: LoginActivity)

}