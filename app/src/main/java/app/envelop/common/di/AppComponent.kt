package app.envelop.common.di

import app.envelop.data.BlockstackModule
import app.envelop.data.PreferencesModule
import app.envelop.domain.AuthService
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
  modules = [
    AppModule::class, PreferencesModule::class, ViewModelModule::class, BlockstackModule::class
  ]
)
interface AppComponent {

  fun plus(activityModule: ActivityModule): ActivityComponent

}