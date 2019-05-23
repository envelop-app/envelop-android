package app.envelop.common.di

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import app.envelop.ui.BaseActivity
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(
  private val activity: BaseActivity
) {

  @Provides
  fun baseActivity() = activity

  @Provides
  fun activity(): Activity = activity

  @Provides
  fun fragmentActivity(): FragmentActivity = activity

}