package app.envelop.common.di

import app.envelop.ui.common.Toolbar
import app.envelop.ui.doc.DocActivity
import app.envelop.ui.login.LoginActivity
import app.envelop.ui.main.MainActivity
import app.envelop.ui.upload.UploadActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(
  modules = [
    ActivityModule::class
  ]
)
interface ActivityComponent {

  fun viewModelProvider(): ActivityViewModelProvider

  // Views

  fun inject(toolbar: Toolbar)

  // Activities

  fun inject(mainActivity: MainActivity)
  fun inject(loginActivity: LoginActivity)
  fun inject(uploadActivity: UploadActivity)
  fun inject(docActivity: DocActivity)
}