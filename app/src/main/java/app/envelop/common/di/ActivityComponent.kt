package app.envelop.common.di

import app.envelop.ui.common.Toolbar
import app.envelop.ui.faq.FaqActivity
import app.envelop.ui.share.ShareActivity
import app.envelop.ui.login.LoginActivity
import app.envelop.ui.main.DocItemView
import app.envelop.ui.main.DocMenuFragment
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

  fun inject(docMenuFragment: DocMenuFragment)
  fun inject(toolbar: Toolbar)

  // Activities

  fun inject(faqActivity: FaqActivity)
  fun inject(loginActivity: LoginActivity)
  fun inject(mainActivity: MainActivity)
  fun inject(shareActivity: ShareActivity)
  fun inject(uploadActivity: UploadActivity)
  fun inject(docItemView: DocItemView)
}
