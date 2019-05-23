package app.envelop.common.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.envelop.ui.login.LoginViewModel
import app.envelop.ui.main.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

  @Binds
  abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

  @Binds
  @IntoMap
  @ViewModelKey(MainViewModel::class)
  abstract fun mainViewModel(viewModel: MainViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(LoginViewModel::class)
  abstract fun loginViewModel(viewModel: LoginViewModel): ViewModel

}