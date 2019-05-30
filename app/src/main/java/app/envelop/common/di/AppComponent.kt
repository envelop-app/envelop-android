package app.envelop.common.di

import app.envelop.data.BlockstackModule
import app.envelop.data.DatabaseModule
import app.envelop.data.PreferencesModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
  modules = [
    AppModule::class, PreferencesModule::class, ViewModelModule::class, BlockstackModule::class, DatabaseModule::class
  ]
)
interface AppComponent {

  fun plus(activityModule: ActivityModule): ActivityComponent

}